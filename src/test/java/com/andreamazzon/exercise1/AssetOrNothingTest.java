package com.andreamazzon.exercise1;

import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This test class has a method testAssetOrNothingOption() that heuristically checks the relation by which the value of
 * the delta of a call option with Black-Scholes model is equal to the valuation of a portfolio holding 1/S0 asset-or-nothing
 * options of same maturity, with the same strike, of course written on the same underlying. Question: does this relation
 * hold in general or only for the Black-Scholes model?
 *
 * @author Andrea Mazzon
 *
 */
class AssetOrNothingTest {
	static final DecimalFormat FORMATTERPOSITIVE4 = new DecimalFormat("0.0000");

	@Test //note the junit @Test tag
	void testAssetOrNothingOption() throws CalculationException {

		//process parameters
		final double initialPrice = 100.0;
		final double volatility = 0.25; //the volatility of the underlying
		final double riskFreeRate = 0;

		//option parameters
		final double strike = 100.0;
		final double maturity = 1.0;

		//simulation parameter
		final int numberOfSimulations = 100000;//the number of paths simulated

		//time discretization parameters
		final double initialTime = 0;
		final int numberOfTimeSteps = 365;
		final double timeStep = maturity / numberOfTimeSteps;
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime,
				numberOfTimeSteps, timeStep);

		/*
		 * parameter for the test, see the method Assert.assertTrue. We want the absolute percentage difference between
		 * our Monte-Carlo approximation of the delta and its analytic value to be smaller than this tolerance.
		 */
		final double tolerance = 0.5;

		//have a look at this class!
		final double analyticValueOfTheDelta = AnalyticFormulas.blackScholesOptionDelta(
				initialPrice, riskFreeRate, volatility, maturity, strike);

		/*
		 * look at the class: it links together the model, i.e., the specification of the dynamics
		 * of the underlying, and the process, i.e., the discretization of the paths.
		 */
		final AssetModelMonteCarloSimulationModel bsModel = new MonteCarloBlackScholesModel(
				times, numberOfSimulations, initialPrice, riskFreeRate, volatility);
		final AbstractAssetMonteCarloProduct assetOrNothingOption = new AssetOrNothing(maturity, strike);

		//note the getValue method: where is getValue(MonteCarloSimulationModel model) implemented?
		final double monteCarloValue = assetOrNothingOption.getValue(bsModel)/initialPrice;

		final double absolutePercentageError = Math.abs(analyticValueOfTheDelta-monteCarloValue)/analyticValueOfTheDelta*100;

		Assert.assertEquals(0, absolutePercentageError, tolerance);

		System.out.println("B-S Monte Carlo value: " + FORMATTERPOSITIVE4.format(monteCarloValue)
		+ "\n" + "Analytical value: " + FORMATTERPOSITIVE4.format(analyticValueOfTheDelta) + "\n" + "Absolute percentage error: "
		+ FORMATTERPOSITIVE4.format(absolutePercentageError)+ "\n" );
	}

}
