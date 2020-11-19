package com.andreamazzon.exercise1;

import java.text.DecimalFormat;
import java.util.Random;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class has a main method whose goal is to compute some statistics about the Monte-Carlo valuation of
 * a call option with underlying given by a Black-Scholes model: we use the implementation we have seen last time
 * (i.e., create of an object of type MonteCarloBlackScholesModel and give it to the getValue method of an object
 * of type EuropeanOption) to price the call option for a given simulation of the Black-Scholes process for a given seed,
 * and then we repeat the experiment changing the seed randomly. All the prices that we get in this way form an array of
 * doubles. This array gets then wrapped into a RandomVariable object, in order to compute average, variance, maximum
 * and minimum using the methods implemented in the Finmath library.
 *
 * @author Andrea Mazzon
 *
 */
public class StatisticsOfBlackScholesCall {
	static final DecimalFormat FORMATTERPOSITIVE4 = new DecimalFormat("0.0000");

	public static void main(String[] args) throws CalculationException {

		/*
		 * This is a Java class that we use here in order to get random integer numbers that will
		 * represent the seeds.
		 */
		final Random randomGenerator = new Random();
		final int numberOfPrices = 1000;

		/*
		 * it is supposed to contain all the option prices for a given seed. It gets then wrapped into
		 * a RandomVariable.
		 */
		final double[] vectorOfPrices = new double[numberOfPrices];

		//process parameters
		final double initialValue = 100.0;
		final double volatility = 0.25; //the volatility of the underlying
		final double riskFreeRate = 0;

		//option parameters
		final double strike = 100.0;
		final double maturity = 1.0;

		//simulation parameter
		final int numberOfSimulations = 10000;//the number of paths simulated

		//time discretization parameters
		final double initialTime = 0;
		final int numberOfTimeSteps = 100;
		final double timeStep = maturity / numberOfTimeSteps;
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime,
				numberOfTimeSteps, timeStep);

		//this first object has some default seed, given in the class
		final AssetModelMonteCarloSimulationModel bsModel = new MonteCarloBlackScholesModel(
				times, numberOfSimulations, initialValue, riskFreeRate, volatility);
		final AbstractAssetMonteCarloProduct europeanOption = new EuropeanOption(maturity, strike);

		//note the getValue method: where is getValue(MonteCarloSimulationModel model) implemented?
		final double monteCarloValue = europeanOption.getValue(bsModel);

		//first entry of the array: this Monte-Carlo price
		vectorOfPrices[0] = monteCarloValue;
		AssetModelMonteCarloSimulationModel bsModelWithModifiedDrift;

		//now we get all the prices for all the random seeds. We store them in the array.
		int seed;//it's better to create it once for all here, outside the for loop.
		for (int i = 1; i < numberOfPrices; i ++) {
			seed = randomGenerator.nextInt();//random int
			//note again the getCloneWithModifiedSeed: we don't have to bother constructing the object from scratch as before
			bsModelWithModifiedDrift =  bsModel.getCloneWithModifiedSeed(seed);
			vectorOfPrices[i] = europeanOption.getValue(bsModelWithModifiedDrift);//we store the price
		}

		//now we wrap the array into one object of type RandomVariable. There are multiple ways to do this, here you can see two
		//final RandomVariable priceRandomVariable = (new RandomVariableFromArrayFactory()).createRandomVariable(0.0, vectorOfPrices);
		//or:
		final RandomVariable priceRandomVariable = new RandomVariableFromDoubleArray(0.0, vectorOfPrices);

		//have a look at this class!
		final double analyticValue = AnalyticFormulas.blackScholesOptionValue(
				initialValue, riskFreeRate, volatility, maturity, strike);

		//at this point, we can get our statistics "for free"
		System.out.println("Analytic value= " + analyticValue);
		System.out.println("Average= " + priceRandomVariable.getAverage());
		System.out.println("Variance= " + priceRandomVariable.getVariance());
		System.out.println("Min= " + priceRandomVariable.getMin());
		System.out.println("Max= " + priceRandomVariable.getMax());
	}

}
