package com.andreamazzon.exercise4;

import org.junit.Assert;
import org.junit.Test;


/**
 * This is a test class, testing if the swap rate computed by the class SwapWithoutFinmath
 * is such that the value of the swap contract associated to the computed swap rate is zero.
 *
 * @author Andrea Mazzon
 */

public class TestSwap {

	@Test
	public void testSwapRate() {
		final double yearFraction = 0.5;
		final double tolerance = 1E-15;
		final double[] zeroCouponBondCurve = { 0.9986509108, 0.9949129829, 0.9897033769, 0.9835370208, 0.9765298116,
				0.9689909565 };

		final Swap swapCalculator = new SwapWithoutFinmath(yearFraction, zeroCouponBondCurve, true);

		final double parSwapRate = swapCalculator.getParSwapRate(yearFraction/*in this way, we compute it more efficiently*/);
		System.out.println("The par swap rate is " + parSwapRate);

		final double swapValue = swapCalculator.getSwapValue(parSwapRate, yearFraction);
		System.out.println("The value of the swap for the par swap rate is " + swapValue);

		// we test if this value is zero, with a tolerance equal to the machine precision
		Assert.assertEquals(0, swapValue, tolerance);
	}

}
