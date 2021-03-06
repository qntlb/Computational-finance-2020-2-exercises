package com.andreamazzon.exercise9;

import java.util.HashMap;
import java.util.Map;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.IndependentIncrements;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelStandard;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelFromGivenMatrix;
import net.finmath.montecarlo.model.ProcessModel;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class creates a LIBOR market model, basing on the classes of the Finmath library
 *
 * @author Andrea Mazzon
 *
 */
public class LIBORMarketModelConstruction {

	/**
	 * It specifies and creates a Rebonato volatility structure, represented by a matrix, for the LIBOR
	 * Market Model. In particular, we have
	 * dL_i(t_j)=\sigma_i(t_j)L_i(t_j)dW_i(t_j)
	 * with
	 * \sigma_i(t_j)=(a+b(T_i-t_j))\exp(-c(T_i-t_j))+d,
	 * for t_j < T_i,
	 * for four parameters a,b,c,d with b,c>0
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @param simulationTimeDiscretization, the time discretization for the evolution of the processes
	 * @param tenureStructureDiscretization, the tenure structure T_0 < T_1< ...<T_n
	 * @return the matrix that represents the volatility structure: volatility[i,j]=sigma_j(t_i)
	 */
	private static double[][] createVolatilityStructure(double a, double b,double c, double d,
			TimeDiscretization simulationTimeDiscretization,
			TimeDiscretization tenureStructureDiscretization) {
		//volatility[i,j]=sigma_j(t_i)
		final int numberOfSimulationTimes = simulationTimeDiscretization.getNumberOfTimeSteps();
		final int numberOfTenureStructureTimes = tenureStructureDiscretization.getNumberOfTimeSteps();
		final double[][] volatility = new double[numberOfSimulationTimes][numberOfTenureStructureTimes];

		for (int timeIndex = 0; timeIndex < numberOfSimulationTimes; timeIndex++) {
			for (int LIBORIndex = 0; LIBORIndex < numberOfTenureStructureTimes; LIBORIndex++) {
				final double currentTime = simulationTimeDiscretization.getTime(timeIndex);//t_j
				final double currentMaturity = tenureStructureDiscretization.getTime(LIBORIndex);//T_i
				final double timeToMaturity = currentMaturity - currentTime;
				double instVolatility;
				if (timeToMaturity <= 0) {
					instVolatility = 0; // This forward rate is already fixed, no volatility
				}
				else {
					instVolatility = d + (a + b * timeToMaturity)
							* Math.exp(-c * timeToMaturity);//\sigma_i(t)=(a+b(T_i-t))\exp(-c(T_i-t))+d
				}
				// Store
				volatility[timeIndex][LIBORIndex] = instVolatility;
			}
		}
		return volatility;
	}

	/**
	 * It simulates a LIBOR Market Model, by using the implementation of the Finmath library.
	 * @param numberOfPaths: number of simulations
	 * @param simulationTimeStep: the time step for the simulation of the LIBOR processes
	 * @param LIBORPeriodLength: the length of the interval between times of the tenure structure
	 * @param LIBORRateTimeHorizon: final LIBOR maturity
	 * @param fixingForGivenForwards: the times of the tenure structure where the initial
	 * forwards (also called LIBORs if you want, here we stick to the name of the Finmath library) are given
	 * @param givenForwards: the given initial forwards (from which the others are interpolated)
	 * @param correlationDecayParam, parameter \alpha>0, for the correlation of the LIBORs: in particular, we have
	 * dL_i(t_j)=\sigma_i(t_j)L_i(t_j)dW_i(t_j)
	 * with
	 * d<W_i,W_k>(t)= \rho_{i,k}(t)dt
	 *  where
	 * \rho_{i,j}(t)=\exp(-\alpha|T_i-T_k|)
	 * @param a, the first term for the volatility structure: the volatility in the SDEs above is given by
	 * \sigma_i(t_j)=(a+b(T_i-t_j))\exp(-c(T_i-t_j))+d,
	 * for t_j < T_i.
	 * @param b, the second term for the volatility structure
	 * @param c, the third term for the volatility structure
	 * @param d, the fourth term for the volatility structure
	 * @return an object implementing LIBORModelMonteCarloSimulationModel, i.e., representing the simulation of a LMM
	 * @throws CalculationException
	 */
	public static LIBORModelMonteCarloSimulationModel
	createLIBORMarketModel(int numberOfPaths,
			double simulationTimeStep,
			double LIBORPeriodLength, //T_i-T_{i-1}, we suppose it to be fixed
			double LIBORRateTimeHorizon, //T_n
			double[] fixingForGivenForwards,
			double[] givenForwards,
			double correlationDecayParam, // decay of the correlation between LIBOR rates
			double a, double b, double c, double d
			)
					throws CalculationException {
		/*
		 In order to simulate a LIBOR market model, we need to proceed along the following steps:
		 1) provide the time discretization for the evolution of the processes
		 2) provide the time discretization of the tenure structure
		 3) provide the observed term structure of the initial LIBOR rates (also called forwards, using the terminology
		 of the Finmath library) and if needed interpolate the ones missing: in this way we obtain the initial values
		 for the LIBOR processes
		 4) create the volatility structure, i.e., the terms sigma_i(t_j) in
		 	dL_i(t_j)=\sigma_i(t_j)L_i(t_j)dW_i(t_j)
	 	 5) create the correlation structure, i.e., define the terms \rho_{i,j}(t) such that
	 	 d<W_i,W_k>(t)= \rho_{i,k}(t)dt
		 6) combine all steps 1, 2, 4, 5 to create a covariance model
		 7) combine steps 2, 3, 6 to create the LIBOR model
		 8) create a Euler discretization of the model we defined in step 7, specifying the model itself and
		 a Brownian motion that uses the time discretization defined in step 1
		 9) give the Euler scheme to the constructor of LIBORMonteCarloSimulationFromLIBORModel, to create an object of
		 type LIBORModelMonteCarloSimulationModel
		 */

		// Step 1: create the time discretization for the simulation of the processes
		final TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(
				0.0, (int) (LIBORRateTimeHorizon / simulationTimeStep), simulationTimeStep);

		// Step 2: create the time discretization for the tenure structure (i.e., the dates T_1,..,T_n)
		final TimeDiscretization LIBORPeriodDiscretization = new
				TimeDiscretizationFromArray(0.0, (int) (LIBORRateTimeHorizon /LIBORPeriodLength), LIBORPeriodLength);

		/*
		  Step 3 Create the forward curve (initial values for the LIBOR market model). We suppose
		  not to have all the forwards: the others are interpolated using the specific method
		  of the Finmath library
		 */
		final ForwardCurve forwardCurve = ForwardCurveInterpolation.createForwardCurveFromForwards(
				"forwardCurve", // name of the curve
				fixingForGivenForwards, // fixings of the forward
				givenForwards, // the forwards we have
				LIBORPeriodLength
				);

		// Step 4, the volatility model: we only have to provide the matrix
		final double[][] volatility = createVolatilityStructure(a, b, c, d,
				timeDiscretization,
				LIBORPeriodDiscretization);

		final LIBORVolatilityModel volatilityModel =
				new LIBORVolatilityModelFromGivenMatrix(timeDiscretization,
						LIBORPeriodDiscretization, volatility);
		/*
		  Step 5
		  Create a correlation model rho_{i,j} = exp(−a ∗ |T_i −T_j|)
		 */
		final LIBORCorrelationModel correlationModel =
				new LIBORCorrelationModelExponentialDecay(
						timeDiscretization,
						LIBORPeriodDiscretization,
						LIBORPeriodDiscretization.getNumberOfTimes()-1,//no factor reduction for now
						correlationDecayParam);

		/*
		 Step 6
		 Combine volatility model and correlation model, together with the two time discretizations,
		 to get a covariance model
		 */
		final LIBORCovarianceModel covarianceModel =
				new LIBORCovarianceModelFromVolatilityAndCorrelation(
						timeDiscretization,
						LIBORPeriodDiscretization,
						volatilityModel,
						correlationModel);

		/*
		 Step 7
		 Combine the forward curve and the covariance model, together with the time discretization of the
		 tenure structure, to define the model
		 */
		final ProcessModel LIBORMarketModel = new LIBORMarketModelStandard(
				LIBORPeriodDiscretization,
				forwardCurve,
				covarianceModel
				);

		//Step 8: create an Euler scheme of the LIBOR model defined above
		final BrownianMotion brownianMotion = new BrownianMotionFromMersenneRandomNumbers(
				timeDiscretization,
				LIBORPeriodDiscretization.getNumberOfTimes()-1,//no factor reduction for now
				numberOfPaths,
				1897 // seed
				);

		final MonteCarloProcess process = new
				EulerSchemeFromProcessModel(LIBORMarketModel, brownianMotion);

		//Step 9: give the Euler scheme to the constructor of LIBORMonteCarloSimulationFromLIBORModel
		return new LIBORMonteCarloSimulationFromLIBORModel(process);

	}

	/**
	 * It returns a simulation of a LIBOR Market model which is the same of the simulation
	 * given as argument (i.e., also same realizations of the Brownian motions) with a
	 * different decay parameter.
	 *
	 * @param oldModel, the simulation of the model whose LIBORCorrelationModel we want to change
	 * @param correlation, the value of the decay parameter of the new LIBORCorrelationModel: this
	 * LIBORCorrelationModel will be indeed of type LIBORCorrelationModelExponentialDecay.
	 * @return the simulation of the model with the new LIBORCorrelationModel
	 * @throws CalculationException
	 */
	public static LIBORModelMonteCarloSimulationModel getCloneWithModifiedCorrelation(
			LIBORModelMonteCarloSimulationModel oldLIBORSimulation,
			double decayParameter)
					throws CalculationException
	{
		/*
		  Steps:
		  - check if the model (i.e. the TermStructureModel object) we get from the simulation (i.e., from
		  LIBORModelMonteCarloSimulationModel) is of type LIBORMarketModel;
		  - get this model as LIBORMarketModel (downcasting)
		  - get the LIBORCovarianceModel object from the LIBORMarketModel object (that's why we want it to be LIBORMarketModel)
		  - create a new LIBORCorrelationModel as a LIBORCorrelationModelExponentialDecay object with the given correlation
		  decay parameter, and the other arguments taken from the LIBORCovarianceModel object
		  - create a new LIBORCovarianceModel with the new LIBORCorrelationModel
		  - create a new LIBORMarketModel with the new LIBORCovarianceModel
		  - link the LIBORMarketModel with the BrownianMotion of the old simulation to get a MonteCarloProcess with the constructor
		  of EulerSchemeFromProcessModel
		  - pass this MonteCarloProcess to the constructor of LIBORMonteCarloSimulationFromLIBORModel to get a
		  LIBORModelMonteCarloSimulationModel.
		 */

		/*
		 * we check if oldLIBORSimulation.getModel() is of type LIBORMarketModel. If not, we know that we
		 * will get a class Exception few lines below. At least, if this is the case, here we print something
		 * in order to help the user understanding what's going wrong.
		 */
		if (!(oldLIBORSimulation.getModel() instanceof LIBORMarketModel)) {
			System.out.println("The model returned by oldLIBORSimulation must be of type LIBORMarketModel!");
		}
		/*
		 * we downcast: we want the model to be of type LIBORMarketModel, because then we want it to be able
		 * to call the method getCovarianceModel()
		 */
		final LIBORMarketModel model =  (LIBORMarketModel) oldLIBORSimulation.getModel();

		// covariance model: it is returned as an object of type LIBORCovarianceModel (the interface).
		final LIBORCovarianceModel oldCovarianceModel = model.getCovarianceModel();

		/*
		 new correlation model: constructor with the same fields of the old LIBORCovarianceModel, except for
		 the decay parameter
		 */
		final LIBORCorrelationModel newCorrelationModel = new
				LIBORCorrelationModelExponentialDecay(oldCovarianceModel.getTimeDiscretization(),
						oldCovarianceModel.getLiborPeriodDiscretization(),
						oldCovarianceModel.getNumberOfFactors(),
						decayParameter);

		final Map <String,Object> changeMap = new HashMap <String,Object>();
		//name of the field and new value
		changeMap.put("correlationModel", newCorrelationModel);
		//new covariance model
		final LIBORCovarianceModel newCovarianceModel= oldCovarianceModel.getCloneWithModifiedData(changeMap);

		//new LIBOR model
		final ProcessModel newLiborMarketModel = model.getCloneWithModifiedCovarianceModel(newCovarianceModel);

		final IndependentIncrements brownianMotion = oldLIBORSimulation.getBrownianMotion();

		final MonteCarloProcess newMonteCarloProcess = new EulerSchemeFromProcessModel(newLiborMarketModel,brownianMotion);
		/*
		 * new simulation: the model is linked with a clone of the Euler Scheme of the old
		 * simulation. Note: this is a clone and not the same object, since this would give
		 * a running time error.
		 */
		return new LIBORMonteCarloSimulationFromLIBORModel(newMonteCarloProcess);
	}
}