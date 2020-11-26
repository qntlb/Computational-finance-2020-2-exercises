package com.andreamazzon.exercise3;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.automaticdifferentiation.RandomVariableDifferentiable;
import net.finmath.montecarlo.automaticdifferentiation.backward.RandomVariableDifferentiableAAD;
import net.finmath.montecarlo.automaticdifferentiation.forward.RandomVariableDifferentiableAD;
import net.finmath.stochastic.RandomVariable;

/**
 * This test class checks the performance of the forward and backward automatic differentiation when computing the gradient
 * of a function of two arguments.
 * It uses the classes
 *
 * net.finmath.montecarlo.automaticdifferentiation.backward.RandomVariableDifferentiableAAD
 *
 * and
 *
 * net.finmath.montecarlo.automaticdifferentiation.backward.RandomVariableDifferentiableAD
 *
 * of the Finmath library.
 *
 * @author Andrea Mazzon
 *
 */
public class StochasticAutomaticDifferentiationTest {

	/*
	 * we use the Random() field in order to generate random sequences of doubles between 0 and 2, that will be the realizations
	 * of our random variables
	 */
	Random random = new Random();

	private final int numberOfRealizations = 20;

	//this will be the value of the first node of the tree
	RandomVariable xValue = constructAndReturnRandomVariable();

	//this will be the value of the second node of the tree
	RandomVariable yValue = constructAndReturnRandomVariable();

	/*
	 * FROM HERE TO THE DEFINITION OF THE FIRST METHOD, THE FIELDS OF THE CLASS WILL BE INITIALIZED WHEN WE
	 * RUN THE CLASS, AND THAT WILL BUILD OUR TREE
	 */

	// BACKWARD DIFFERENTIATION

	//first node of the tree (look at the constructor)
	RandomVariableDifferentiable xBackward = new RandomVariableDifferentiableAAD(xValue);

	//second node of the tree (look at the constructor)
	RandomVariableDifferentiable yBackward = new RandomVariableDifferentiableAAD(yValue);

	RandomVariableDifferentiable resultBackward = (RandomVariableDifferentiable)
			xBackward.squared().add((xBackward).mult(yBackward.squared())).exp();

	Map<Long, RandomVariable> backwardGradient;

	// ..and same thing for the forward differentiation
	RandomVariableDifferentiable xForward = new RandomVariableDifferentiableAD(xValue);
	RandomVariableDifferentiable yForward = new RandomVariableDifferentiableAD(yValue);

	RandomVariableDifferentiable resultForward = (RandomVariableDifferentiable)
			xForward.squared().add((xForward).mult(yForward.squared())).exp();

	Map<Long, RandomVariable> forwardGradient;


	RandomVariable analytic = xBackward.add(xBackward).add(yBackward.squared()).mult(resultBackward);


	/*
	 * This method construct a RandomVariable object with a number of (quasi) random doubles between 0 and 2. The number
	 * is specified by the field numberOfRealizations of the class
	 */
	private RandomVariable constructAndReturnRandomVariable() {
		final double[] realizationsRandomVariable = new double[numberOfRealizations];
		for (int i = 0; i < numberOfRealizations; i++) {
			//random.nextDouble() is a double between 0 and 1
			realizationsRandomVariable[i] = random.nextDouble()*2;
		}
		//remember: how to wrap an array of doubles into a RandomVariableFromDoubleArray object
		return new RandomVariableFromDoubleArray(0.0, realizationsRandomVariable);
	}

	/*
	 * this method returns the time needed to compute the gradient of our concatenation of basic functions,
	 * by using the class RandomVariableDifferentiableAAD. It also stores the computed gradient in the field
	 * backwardGradient of the class
	 */
	private long getBackwardGradient() {
		// a way to measure the execution time
		final long startTimeBackward = System.nanoTime();
		/*
		 * Get the gradient: method of the RandomVariableDifferentiable interface, which
		 * in this case is implemented in RandomVariableDifferentiableAAD.
		 */
		backwardGradient = resultBackward.getGradient();

		// time needed in the computation
		return System.nanoTime() - startTimeBackward;
	}

	/*
	 * this method returns the time needed to compute the gradient of our concatenation of basic functions,
	 * by using the class RandomVariableDifferentiableAD. It also stores the computed gradient in the field
	 * forwardGradient of the class
	 */
	private long getForwardGradient() {
		final long startTimeForward = System.nanoTime();
		// Get gradient, and compute the time needed to do it.
		forwardGradient = resultForward.getGradient();
		return System.nanoTime() - startTimeForward;
	}

	@Test // The Test annotation tells JUnit that the this method can be run as a test case
	public void testBackwardDifferentiation() {
		// get the gradient with the backward implementation
		backwardGradient = resultBackward.getGradient();

		/*
		 * We want to get the derivative with respect to x: then we get the value
		 * specified by the key which identifies the node with x.
		 */
		final RandomVariable backwarddx = backwardGradient.get(// get is a method of the Map interface: it gets the object specified by the key																			// a key
				xBackward.getID()/* getID method to get the key */);

		System.out.println("Average derivative with backward     : " + backwarddx.getAverage());
		System.out.println("Expected average derivative : " + analytic.getAverage());
		System.out.println();
		// assertEquals method to check if we have done the right computation
		for (int i = 0; i < numberOfRealizations; i++) {
			Assert.assertEquals(backwarddx.get(i), analytic.get(i), 1E-7);
		}
	}

	@Test
	public void testForwardDifferentiation() {
		// get the gradient with the forward implementation
		forwardGradient = resultForward.getGradient();

		// ..everything as before
		final RandomVariable forwarddx = forwardGradient.get(xForward.getID());

		System.out.println("Average derivative with forward    : " + forwarddx.getAverage());
		System.out.println("Expected average derivative : " + analytic.getAverage());
		System.out.println();

		for (int i = 0; i < numberOfRealizations; i++) {
			Assert.assertEquals(forwarddx.get(i), analytic.get(i), 1E-7);
		}
	}

	/*
	 * this test compares the forward and the backward implementation, and returns success is the backward is
	 * more efficient, which is actually what we would expect
	 */
	@Test
	public void testRepeatedly() {
		/*
		 * 1000 repetitions of the methods getBackwardGradient() and getForwardGradient(): we will compare the
		 * time needed to compute the
		 */
		final int numberOfRepetitions = 1000;
		//here we store the time needed
		final long[] timesBackward = new long[numberOfRepetitions];
		final long[] timesForward = new long[numberOfRepetitions];

		int count = 0;

		for (int i = 0; i < numberOfRepetitions; i++) {
			final long elapsedTimeBackward = getBackwardGradient();
			final long elapsedTimeForward = getForwardGradient();

			//we count the number of times that backward is better than forward
			if (elapsedTimeBackward < elapsedTimeForward) {
				count++;
			}

			timesBackward[i] = elapsedTimeBackward;
			timesForward[i] = elapsedTimeForward;

		}

		//a way to compute the average of the two arrays
		final double averageBackward = Arrays.stream(timesBackward).average().getAsDouble();
		final double averageForward = Arrays.stream(timesForward).average().getAsDouble();
		System.out.println("Average times after " + numberOfRepetitions + " runs:");
		System.out.println("Backward: " + averageBackward);
		System.out.println("Forward:  " + averageForward);
		Assert.assertTrue(averageBackward < averageForward);

		System.out.println(
				"Backward differentiation was faster " + count + " times out of " + numberOfRepetitions + "\n");

	}
}