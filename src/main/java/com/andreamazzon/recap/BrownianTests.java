package com.andreamazzon.recap;

import java.text.DecimalFormat;

import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 *
 * This class gives an overview of the simulation of the Brownian Motion with the Finmath library, besides some
 * other functionalities.
 *
 * @author: Andrea Mazzon
 *
 */

public final class BrownianTests {

	/*
	 * note how to set some printing settings. We use objects of type DecimalFormat, which is a class that
	 * extends DecimalFormat. At the right, you see the constructor.
	 */
	static final DecimalFormat FORMATTERPOSITIVE2 = new DecimalFormat("0.00");
	static final DecimalFormat FORMATTERREAL4 = new DecimalFormat(" 0.0000;-0.0000");

	public static void main(final String[] args) {


		// The parameters
		final int numberOfPaths = 100000;//i.e., the number of simulated trajectories
		final int seed = 1897;//this is the seed that we need to generate the random numbers
		final double firstTime = 0.0;
		final double lastTime = 1.0;
		final double dt = 0.01;//the time step

		// Time discretization, number of steps deduced from lastTime and dt
		final TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(
				firstTime, //first time of the time discretization
				(int) (lastTime / dt),//number of times
				dt //time step
				);

		// We generate a 2-dimensional Brownian motion
		final BrownianMotion brownian = new BrownianMotionFromMersenneRandomNumbers(
				timeDiscretization, //the time discretization of the Brownian motion
				2, // number of independent Brownian motions that we generate: this is a 2-dimensional Brownian Motion
				numberOfPaths,//number of simulated paths
				seed //the seed that is needed to generate the Mersenne random numbers (see line 141 of BrownianMotionFromMersenneRandomNumbers)
				);

		System.out.println("Average, variance and other properties of a BrownianMotion."
				+ "\n Time step size (dt): " + dt + "  Number of path: " + numberOfPaths + "\n");
		System.out.println("      " + "\t" + "  int dW_1 " + "\t" + "int dW_1 dW_1" + "\t" + "int dW_1 dW_2" + "\t");
		System.out.println("time" + "\t" + " mean" + "\t" + " var" + "\t" + " mean" + "\t" + " var" + "\t" + " mean"
				+ "\t" + " var");

		final int numberOfTimes = timeDiscretization.getNumberOfTimes();

		//note: arrays of RandomVariable objects
		final RandomVariable[] firstBrownianMotionPath = new RandomVariable[numberOfTimes];
		final RandomVariable[] secondBrownianMotionPath = new RandomVariable[numberOfTimes];

		final RandomVariable[] firstQuadraticVariationPath = new RandomVariable[numberOfTimes];
		final RandomVariable[] quadraticCovariationPath = new RandomVariable[numberOfTimes];


		// We set B_0 = 0. Note that this is a deterministic constant
		firstBrownianMotionPath[0] = new RandomVariableFromDoubleArray(0.0 /*the time*/, 0.0 /*the value*/);
		secondBrownianMotionPath[0] = new RandomVariableFromDoubleArray(0.0 /*the time*/, 0.0 /*the value*/);
		firstQuadraticVariationPath[0] = new RandomVariableFromDoubleArray(0.0 /*the time*/, 0.0 /*the value*/);
		quadraticCovariationPath[0] = new RandomVariableFromDoubleArray(0.0 /*the time*/, 0.0 /*the value*/);

		/*
		 * here we create two objects of type RandomVariable, that during the for loop running in time
		 * will store all the increments one by one. This is better than creating a new RandomVariable
		 * every time.
		 */
		RandomVariable firstBrownianIncrement;
		RandomVariable secondBrownianIncrement;
		for (int timeIndex = 1; timeIndex < timeDiscretization.getNumberOfTimeSteps(); timeIndex++) {

			//we fix it: in this way we don't have to call the method all every time we need the increment
			firstBrownianIncrement = brownian.getBrownianIncrement(timeIndex, 0);
			secondBrownianIncrement = brownian.getBrownianIncrement(timeIndex, 1);

			// We get W(t+dt) from dW(t)

			//first path
			firstBrownianMotionPath[timeIndex] = firstBrownianMotionPath[timeIndex - 1]
					.add(firstBrownianIncrement);
			//second path
			secondBrownianMotionPath[timeIndex] = secondBrownianMotionPath[timeIndex - 1]
					.add(secondBrownianIncrement);

			// We compute the quadratic variation of the first path
			firstQuadraticVariationPath[timeIndex] = firstQuadraticVariationPath[timeIndex - 1]
					.add(firstBrownianIncrement.squared());

			// We compute the quadratic covariation of the two paths
			quadraticCovariationPath[timeIndex] = quadraticCovariationPath[timeIndex - 1]
					.add(firstBrownianIncrement.mult(secondBrownianIncrement));

			/*
			 * we print the results. Note the use of the NumberFormat.format method. And of course the
			 * getAverage() method of RandomVariable.
			 */
			System.out.println(FORMATTERPOSITIVE2.format(timeDiscretization.getTime(timeIndex)) + "\t"
					+ FORMATTERREAL4.format(firstBrownianMotionPath[timeIndex].getAverage()) + "\t"
					+ FORMATTERREAL4.format(firstBrownianMotionPath[timeIndex].getVariance()) + "\t"
					+ FORMATTERREAL4.format(firstQuadraticVariationPath[timeIndex].getAverage()) + "\t"
					+ FORMATTERREAL4.format(firstQuadraticVariationPath[timeIndex].getVariance()) + "\t"
					+ FORMATTERREAL4.format(quadraticCovariationPath[timeIndex].getAverage()) + "\t"
					+ FORMATTERREAL4.format(quadraticCovariationPath[timeIndex].getVariance()) + "\t" + "");
		}
		System.out.println("\n");

		/*
		 * if the tables above confuse you, one can just pick a given time and analyse
		 * statistic by statistic, since we stored everything in an array:
		 */
		final double time = 0.5;
		final int indexForTheGivenTime = timeDiscretization.getTimeIndexNearestGreaterOrEqual(time);


		// mean and variance of the Brownian motion at the given time
		System.out.println("Mean of the first Brownian Motion at time " + time + " : "
				+ firstBrownianMotionPath[indexForTheGivenTime].getAverage());
		System.out.println("Variance of the first Brownian Motion at time " + time + " : "
				+ secondBrownianMotionPath[indexForTheGivenTime].getVariance() + "\n");


		// mean and variance of the quadratic variation of the Brownian motion at the given time
		System.out.println("Mean of the Quadratic Variation of the first Brownian motion at time " + time + " : "
				+ firstQuadraticVariationPath[indexForTheGivenTime].getAverage());
		System.out.println("Variance of the Quadratic Variation of the first Brownian motion at time " + time + " : "
				+ firstQuadraticVariationPath[indexForTheGivenTime].getVariance()+ "\n");

		// mean and variance of the quadratic variation of the two Brownian motions at the given time
		System.out.println("Mean of the Quadratic Covariation of the two Brownian motions at time " + time + " : "
				+ quadraticCovariationPath[indexForTheGivenTime].getAverage());
		System.out.println("Variance of the Quadratic Covariation of the two Brownian motions at time " + time + " : "
				+ quadraticCovariationPath[indexForTheGivenTime].getVariance());
	}
}