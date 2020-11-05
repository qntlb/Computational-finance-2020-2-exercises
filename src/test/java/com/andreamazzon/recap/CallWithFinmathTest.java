package com.andreamazzon.recap;


import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;


/**
 * This is a small test class, just to have a look about it works. It has a method taht is basically
 * the first part of the main method in CallWithFinmath. Note the use of the method Assert.assertTrue.
 *
 * @author Andrea Mazzon
 *
 */
class CallWithFinmathTest {

	static final DecimalFormat FORMATTERPOSITIVE4 = new DecimalFormat("0.0000");

	@Test
	void test() throws CalculationException {
		//process parameters
		final double initialPrice = 100.0;
		final double volatility = 0.25; //the volatility of the underlying
		final double riskFreeRate = 0;

		//option parameters
		final double strike = 100.0;
		final double maturity = 1.0;

		//simulation parameter
		final int numberOfSimulations = 1000000;//the number of paths simulated

		//time discretization parameters
		final double initialTime = 0;
		final int numberOfTimeSteps = 100;
		final double timeStep = maturity / numberOfTimeSteps;
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime,
				numberOfTimeSteps, timeStep);

		/*
		 * look at the class: it links together the model, i.e., the specification of the dynamics
		 * of the underlying, and the process, i.e., the discretization of the paths.
		 */
		final AssetModelMonteCarloSimulationModel bsModel = new MonteCarloBlackScholesModel(
				times, numberOfSimulations, initialPrice, riskFreeRate, volatility);
		final AbstractAssetMonteCarloProduct europeanOption = new EuropeanOption(maturity, strike);

		//have a look at this class!
		final double analyticValue = AnalyticFormulas.blackScholesOptionValue(
				initialPrice, riskFreeRate, volatility, maturity, strike);

		//note the getValue method: where is getValue(MonteCarloSimulationModel model) implemented?
		final double monteCarloValue = europeanOption.getValue(bsModel);

		final double absolutePercentageError = Math.abs(analyticValue-monteCarloValue)/analyticValue*100;

		System.out.println("B-S Monte Carlo value: " + FORMATTERPOSITIVE4.format(monteCarloValue)
		+ "\n" + "Analytical value: " + FORMATTERPOSITIVE4.format(analyticValue) + "\n" + "Absolute percentage error: "
		+ FORMATTERPOSITIVE4.format(absolutePercentageError)+ "\n" );

		Assert.assertTrue(absolutePercentageError< 0.5);
	}

}
