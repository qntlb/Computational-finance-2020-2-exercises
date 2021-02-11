package com.andreamazzon.exercise11;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.andreamazzon.exercise11.LIBORMarketModelConstructionWithDynamicsAndMeasureSpecification.Dynamics;
import com.andreamazzon.exercise11.LIBORMarketModelConstructionWithDynamicsAndMeasureSpecification.Measure;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.TermStructureMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.Caplet;
import net.finmath.montecarlo.interestrate.products.Caplet.ValueUnit;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.plots.Plots;

/**
 * In this class we test the implementation of a LIBOR market model with normal and log-normal dynamics
 * looking at the prices and at the implied volatilities of a caplet, for different strikes. We have three methods
 * that we call in a very simple main.
 *
 * @author Andrea Mazzon
 *
 */
public class CapletTest {

	final int	numberOfPaths	= 50000;

	//parameters for the two time discretizations
	final double simulationTimeStep = 0.1;
	final double LIBORTimeStep = 0.5;
	final double LIBORRateTimeHorizon = 5;

	final double notional = 1000;


	//fixing times for the forwards: the forwards corresponding to other fixing times will be interpolated
	final double[] fixingForGivenForwards = { 0.5, 1.0, 2.0, 3.0};
	final double[] forwardsForCurve = { 0.05, 0.05, 0.05, 0.05};

	final double correlationDecayParameter = 0.5;

	final double a = 0.2, b = 0.1, c = 0.15, d = 0.3; //volatility structure

	//parameters to be given to the constructor of the finmath library Caplet class
	final double maturityOfTheCaplet = 4.0;
	final double periodLengthOfTheCaplet = LIBORTimeStep;

	//parameters for the tests and for the plots
	final double minStrike = 0.025, maxStrike = 0.1;

	final int dotSizeForPlot = 5;


	/**
	 * It plots the prices of a caplet based on a LIBOR market model, both when we consider normal and log-normal dynamics,
	 * for different strikes. Here we consider the simulation of the processes under the terminal measure.
	 *
	 * @throws CalculationException
	 */
	public void testWithPrices() throws CalculationException {

		String dynamics;//it will be used just for the plots

		//we first consider log-normal dynamics, and then normal dynamics
		for(final Dynamics typeOfDynamics : new Dynamics[] { Dynamics.NORMAL, Dynamics.LOGNORMAL}) {

			final TermStructureMonteCarloSimulationModel lmm =
					LIBORMarketModelConstructionWithDynamicsAndMeasureSpecification.createLIBORMarketModel(
							numberOfPaths,
							simulationTimeStep,
							LIBORTimeStep, //T_i-T_{i-1}, we suppose it to be fixed
							LIBORRateTimeHorizon, //T_n
							fixingForGivenForwards,
							forwardsForCurve,
							correlationDecayParameter, // decay of the correlation between LIBOR rates
							typeOfDynamics, //first log-normal dynamics then normal dynamics
							Measure.TERMINAL, //we specify that we consider the dynamics under the terminal measure
							a, b, c, d
							);

			final double numeraireAtGivenTime = lmm.getNumeraire(4.0).getAverage();
			System.out.println("The Numeraire in time t = 0 is " + numeraireAtGivenTime + " with measure " + typeOfDynamics);


			//we want two List<Double> objects for the method  Plots.createScatter: strikes and prices
			final List<Double> strikes = new ArrayList<Double>();
			final List<Double> prices = new ArrayList<Double>();

			//for any strike, we get the price of the caplet
			for(double strike = minStrike; strike <= maxStrike; strike += 0.0025) {
				//we use the Finmath library implementation
				final TermStructureMonteCarloProduct caplet = new Caplet(maturityOfTheCaplet, periodLengthOfTheCaplet, strike);
				//we get the price with usual getValue method (and we multiply by the notional)
				final double capletPrice = notional * caplet.getValue(lmm);
				//we add the new entry to the list
				strikes.add(strike);
				prices.add(capletPrice);
			}

			//now we want to plot the prices, with the appropriate titles according to the type of the dynamics

			if (typeOfDynamics == Dynamics.LOGNORMAL) {
				dynamics = "log-normal";
			} else {
				dynamics = "normal";
			}

			Plots.createScatter(strikes, prices, 0.0, maxStrike * 1.5, dotSizeForPlot)
			.setTitle("Caplet price using " + dynamics + " dynamics.")
			.setXAxisLabel("strike")
			.setYAxisLabel("price")
			.setYAxisNumberFormat(new DecimalFormat("0.00")).show();
		}
	}

	/**
	 * It plots the implied volatilities of a caplet based on a LIBOR market model, both when we consider normal and log-normal
	 * dynamics, for different strikes. Here we consider the simulation of the processes under the terminal measure.
	 *
	 * @throws CalculationException
	 */
	public void testWithImpliedVolatilities() throws CalculationException {
		String dynamics;//it will be used just for the plots

		//we first consider log-normal dynamics, and then normal dynamics
		for(final Dynamics typeOfDynamics : new Dynamics[] { Dynamics.NORMAL, Dynamics.LOGNORMAL}) {

			final TermStructureMonteCarloSimulationModel lmm =
					LIBORMarketModelConstructionWithDynamicsAndMeasureSpecification.createLIBORMarketModel(
							numberOfPaths,
							simulationTimeStep,
							LIBORTimeStep, //T_i-T_{i-1}, we suppose it to be fixed
							LIBORRateTimeHorizon, //T_n
							fixingForGivenForwards,
							forwardsForCurve,
							correlationDecayParameter, // decay of the correlation between LIBOR rates
							typeOfDynamics, //first log-normal dynamics then normal dynamics
							Measure.TERMINAL, //we specify that we consider the dynamics under the terminal measure
							a, b, c, d
							);

			//we want two List<Double> objects for the method  Plots.createScatter: strikes and implied volatilities
			final List<Double> strikes = new ArrayList<Double>();
			final List<Double> impliedVolatilities = new ArrayList<Double>();

			//for any strike, we get the implied volatility of the caplet
			for(double strike = minStrike; strike <= maxStrike; strike += 0.0025) {
				/*
				 * we use the Finmath library implementation. Here in particular we want to get the implied volatility, so the constructor
				 * is different.
				 */
				final TermStructureMonteCarloProduct capletForImpliedVolatility = new Caplet(maturityOfTheCaplet, periodLengthOfTheCaplet, strike,
						periodLengthOfTheCaplet /*what is called periodLengthOfTheCaplet*/, false /*is not a Floorlet*/,
						ValueUnit.LOGNORMALVOLATILITY /*we specify that we want the log-normal implied volatility: the volatility that the single
						LIBOR rate process would have under a Black model*/);

				//the getValue method than returns the implied volatility
				final double capletImpliedVolatility = capletForImpliedVolatility.getValue(lmm);

				strikes.add(strike);
				impliedVolatilities.add(capletImpliedVolatility);

			}

			//now we want to plot the implied volatilities, with the appropriate titles according to the type of the dynamics

			if (typeOfDynamics == Dynamics.LOGNORMAL) {
				dynamics = "log-normal";
			} else {
				dynamics = "normal";
			}

			Plots.createScatter(strikes, impliedVolatilities, 0.0, maxStrike * 1.5, dotSizeForPlot)
			.setTitle("Caplet implied volatility using " + dynamics + " dynamics.")
			.setXAxisLabel("strike")
			.setYAxisLabel("price")
			.setYAxisNumberFormat(new DecimalFormat("0.0%")).show();
		}
	}

	/**
	 * It plots the prices of a caplet based on a LIBOR market model, both when we consider the simulation of the processes under
	 * the terminal and the spot measure. Here we consider log-normal dynamics for teh processes.
	 *
	 * @throws CalculationException
	 */
	public void testForMeasure() throws CalculationException {

		String measure;//it will be used just for the plots

		//we first consider the terminal measure, and then the spot measure

		for(final Measure typeOfMeasure : new Measure[] { Measure.TERMINAL, Measure.SPOT}) {

			final TermStructureMonteCarloSimulationModel lmm =
					LIBORMarketModelConstructionWithDynamicsAndMeasureSpecification.createLIBORMarketModel(
							numberOfPaths,
							simulationTimeStep,
							LIBORTimeStep, //T_i-T_{i-1}, we suppose it to be fixed
							LIBORRateTimeHorizon, //T_n
							fixingForGivenForwards,
							forwardsForCurve,
							correlationDecayParameter, // decay of the correlation between LIBOR rates. Initially zero
							Dynamics.LOGNORMAL, //we specify that we want log-normal dynamics
							typeOfMeasure, //first we consider the terminal measure, then the spot measure
							a, b, c, d
							);

			//we want two List<Double> objects for the method  Plots.createScatter: strikes and prices
			final List<Double> strikes = new ArrayList<Double>();
			final List<Double> prices = new ArrayList<Double>();

			//for any strike, we get the price of the caplet
			for(double strike = 0.025; strike < 0.10; strike += 0.0025) {
				//we use the Finmath library implementation
				final TermStructureMonteCarloProduct caplet = new Caplet(maturityOfTheCaplet, periodLengthOfTheCaplet, strike);
				//we get the price with usual getValue method (and we multiply by the notional)
				final double capletPrice = notional * caplet.getValue(lmm);
				//we add the new entry to the list
				strikes.add(strike);
				prices.add(capletPrice);
			}

			//now we want to plot the prices, with the appropriate titles according to the measure considered

			if (typeOfMeasure == Measure.TERMINAL) {
				measure = "terminal";
			} else {
				measure = "spot";
			}

			final double numeraireAtGivenTime = lmm.getNumeraire(0.0).getAverage();
			System.out.println("The Numeraire in time t = 0 is " + numeraireAtGivenTime + " with measure " + measure);

			Plots.createScatter(strikes, prices, 0.0, 0.2, 5)
			.setTitle("Caplet price using the " + measure + " measure.")
			.setXAxisLabel("strike")
			.setYAxisLabel("price")
			.setYAxisNumberFormat(new DecimalFormat("0.00")).show();
		}
	}

	public static void main(final String[] args) throws CalculationException {
		(new CapletTest()).testWithPrices();
		(new CapletTest()).testWithImpliedVolatilities();
		(new CapletTest()).testForMeasure();
	}
}
