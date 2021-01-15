package com.andreamazzon.exercise9;


import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.Test;

import net.finmath.functions.AnalyticFormulas;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class tests the LIBOR Market Model implementation by evaluating a digital caplet:
 * the analytical prices are compared with the Monte Carlo ones.
 *
 * @author Andrea Mazzon
 *
 */
public class LMMDigitalCapletTest {

	private final static DecimalFormat formatterDouble = new DecimalFormat("0.00");

	private final static DecimalFormat formatterDeviation = new DecimalFormat("0.000%;");


	@Test
	public void testDigitalCaplet() throws Exception {

		final int numberOfPaths = 100000;
		final double simulationTimeStep = 0.1;
		//of course the two time discretization are not required to be the same!
		final double liborPeriodLength = 0.5;
		final double liborRateTimeHorizon = 10;
		final double correlationDecayParam = 0.5;//alpha, so that rho_{i,j}=\exp(-alpha|T_i-T_j|)
		final double strike = 0.05;
		final double notional = 10000;
		final double tolerance = 1E-1;
		final double a = 0.2, b = 0.1, c = 0.15, d = 0.3; //volatility structure
		final double[] fixingForForwards = { 0.5, 1.0, 3.0, 4.0, liborRateTimeHorizon }; // fixing for the forwards
		//times for teh forwards: the others will be interpolated
		final double[] forwardsForCurve = { 0.05, 0.05, 0.05, 0.05, 0.05 };

		//construct such an object using the method LIBORMarketModelConstruction.createLIBORMarketModel
		final LIBORModelMonteCarloSimulationModel myLiborMonteCarlo = null;

		System.out.println("Digital caplet prices:\n");
		System.out.println("Maturity      Simulation       Analytic       Abs Difference");

		for (int maturityIndex = 1; maturityIndex <= myLiborMonteCarlo.getNumberOfLibors()-1;
				maturityIndex ++) {

			/*
			 * GET EVERY MATURITY TIME T_i AND PERIOD END T_{i+1} FROM myLiborMonteCarlo
			 * (LOOK AT THE METHODS OF TEH FINAMTH LIBRARY YOU CAN USE)
			 */
			final double optionMaturity = 0;
			System.out.print(formatterDouble.format(optionMaturity ) + "        ");
			final double periodEnd = 0;

			//COMPUTE THE VALUE OF THE DIGITAL CAPLET FOR EVERY MATURITY TIME T_i.

			final double monteCarloPriceOfTheDigitalCaplet = 0;

			System.out.print(formatterDouble.format(monteCarloPriceOfTheDigitalCaplet) + "         ");

			//computation of the analytical value
			final double periodLength = periodEnd -optionMaturity;
			final int optionMaturityIndex = myLiborMonteCarlo.getTimeIndex(optionMaturity);
			final int liborIndex = myLiborMonteCarlo.getLiborPeriodIndex(optionMaturity);

			/*
			 * computation of the volatility: we want to compute sigma_i(t_j). Here
			 * i = liborIndex, j = optionMaturityIndex.
			 */
			final double volatility = Math.sqrt(
					/*
					 * getIntegratedLIBORCovariance() is defined in LIBORMarketModel: we need
					 * to downcast
					 */
					((LIBORMarketModel) myLiborMonteCarlo.getModel()).
					getIntegratedLIBORCovariance(new TimeDiscretizationFromArray(0.0,
							(int) (liborRateTimeHorizon / simulationTimeStep), simulationTimeStep))
					[optionMaturityIndex][liborIndex][liborIndex]/
					optionMaturity);

			//extract the Libor curve in order to get the analytical price
			final DiscountCurve discountFactors = myLiborMonteCarlo.getModel().getDiscountCurve();

			//extract the forward curve in order to get the analytical price
			final ForwardCurve forwards = myLiborMonteCarlo.getModel().getForwardRateCurve();

			final double forward = forwards.getForward(null, optionMaturity);//L(T_i,T_{i+1};0)
			final double discountFactor = discountFactors.getDiscountFactor(periodEnd);//P(T_{i+1};0)

			final double analyticPriceOfTheDigitalCaplet = notional*AnalyticFormulas.
					blackModelDgitialCapletValue( forward, volatility, periodLength,
							discountFactor , optionMaturity , strike);

			final double relativeDifference = Math.abs(monteCarloPriceOfTheDigitalCaplet - analyticPriceOfTheDigitalCaplet)/analyticPriceOfTheDigitalCaplet;

			System.out.print(formatterDouble.format(analyticPriceOfTheDigitalCaplet) + "         ");
			// Absolute deviation
			System.out.println(formatterDeviation.format(relativeDifference) );
			Assert.assertTrue(relativeDifference < tolerance);

		}
	}
}
