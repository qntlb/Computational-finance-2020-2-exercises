package com.andreamazzon.exercise9;

import java.util.HashMap;
import java.util.Map;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelStandard;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelFromGivenMatrix;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
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
	 * It specifies and creates a Rebonato volatility structure, represented by a matrix,
	 * for the LIBOR Market Model. In particular, we have
	 * \sigma_i(t_j)=(a+b(T_i-t_j))\exp(-c(T_i-t_j))+d,
	 * for t_j < T_i,
	 * for four parameters a,b,c,d.
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @param simulationTimeDiscretization, the time discretization for the processes
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
				final double time = simulationTimeDiscretization.getTime(timeIndex);//t_j
				final double maturity = tenureStructureDiscretization.getTime(LIBORIndex);//T_i
				final double timeToMaturity = maturity - time;
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
	 * @param correlationDecayParam, parameter "a", a>0, for the correlation of the LIBORs:
	 * \rho_{i,j}(t)=\exp(-a\rho|T_-T_j|)
	 * @param a, the first term for the volatility structure
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
		 1) provide the time discretization for the processes
		 2) provide the time discretization of the tenure structure
		 3) provide the observed term structure of LIBOR rates, and if needed interpolate the
		 ones missing: in this way we obtain the initial values for the LIBOR processes
		 4) create the volatility structure, i.e., the terms sigma_i(t_j), where sigma_i is
		 the volatility of the i-th LIBOR.
	 	 5) create the correlation structure, i.e., define \rho_{i,j}(t), correlation
	 	 between LIBORs
		 6) combine all steps 1 − 5 into a LIBOR market model
		 7) create a Euler discretization of the model we defined in step 5
		 8) merge together LIBOR market model and Euler scheme
		 */

		// Step 1: create the time Discretization for the Monte Carlo simulation
		final TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(0.0,
				(int) (LIBORRateTimeHorizon / simulationTimeStep), simulationTimeStep);

		// Step 2: create the time discretization for the simulation of the processes
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
				givenForwards, // forwards
				LIBORPeriodLength
				);

		// Step 4, the volatility model: we only have to provide the matrix
		final double[][] volatility = createVolatilityStructure(a, b, c, d,
				timeDiscretization,
				LIBORPeriodDiscretization);

		final LIBORVolatilityModelFromGivenMatrix volatilityModel =
				new LIBORVolatilityModelFromGivenMatrix(timeDiscretization,
						LIBORPeriodDiscretization, volatility);
		/*
		  Step 5
		  Create a correlation model rho_{i,j} = exp(−a ∗ |T_i −T_j|)
		 */
		final LIBORCorrelationModelExponentialDecay correlationModel =
				new LIBORCorrelationModelExponentialDecay(
						timeDiscretization,
						LIBORPeriodDiscretization,
						LIBORPeriodDiscretization.getNumberOfTimes()-1,//no factor reduction for now
						correlationDecayParam);

		/*
		 Step 6
		 Combine volatility model and correlation model to a covariance model
		 */
		final LIBORCovarianceModelFromVolatilityAndCorrelation covarianceModel =
				new LIBORCovarianceModelFromVolatilityAndCorrelation(
						timeDiscretization,
						LIBORPeriodDiscretization,
						volatilityModel,
						correlationModel);

		// Set model properties
		final Map<String, String> properties = new HashMap<String, String >();

		// We choose the simulation measure
		properties.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT. name());

		// We choose a log normal model
		properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.LOGNORMAL.name());


		final LIBORMarketModel LIBORMarketModel = new LIBORMarketModelStandard(
				LIBORPeriodDiscretization,
				forwardCurve,
				covarianceModel
				);

		//Step 8: create an Euler discretization
		final BrownianMotion brownianMotion = new BrownianMotionFromMersenneRandomNumbers(
				timeDiscretization,
				LIBORPeriodDiscretization.getNumberOfTimes()-1,//no factor reduction for now
				numberOfPaths,
				1897 // seed
				);

		final EulerSchemeFromProcessModel process = new
				EulerSchemeFromProcessModel(LIBORMarketModel, brownianMotion);

		return new LIBORMonteCarloSimulationFromLIBORModel(process);

	}
}