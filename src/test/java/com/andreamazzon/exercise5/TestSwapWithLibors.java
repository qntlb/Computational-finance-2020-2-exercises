package com.andreamazzon.exercise5;


import org.junit.Assert;
import org.junit.Test;

import com.andreamazzon.exercise4.Swap;
import com.andreamazzon.exercise4.SwapWithoutFinmath;

import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;


/**
 * This is a test class, testing if the swap rate computed by the class SwapWithoutFinmath
 * is such that the value of the swap contract associated to the computed swap rate is zero.
 *
 * @author Andrea Mazzon
 */

public class TestSwapWithLibors {

	//method to convert a zero coupon bond curve to a Libor rate curve
	private static double[] fromBondToLibors(TimeDiscretization tenureStructure, double[] bonds) {
		final int curveLength = bonds.length;
		final double[] derivedLiborsCurve = new double[curveLength];//vector that will store the zero coupon bond curve
		final double firstBond = 1.0;//P(0;0)=1, we use it to calculate L(0,T_1;0)
		final double secondBond = bonds[0];
		//L(0,T_1;0)=(P(0;0)-P(T_1;0))/P(T_1;0)/T_1 (since T_0=0)
		derivedLiborsCurve[0] = (firstBond - secondBond) / secondBond / tenureStructure.getTime(0);
		for (int periodIndex = 1; periodIndex < curveLength; periodIndex ++) {
			//L(T_{i-1},T_i;0)=(P(T_{i-1};0)-P(T_i;0))/P(T_i;0)/(T_i-T_{i-1})
			derivedLiborsCurve[periodIndex] = (bonds[periodIndex - 1] - bonds[periodIndex]) / bonds[periodIndex] /
					tenureStructure.getTimeStep(periodIndex - 1);
		}
		return derivedLiborsCurve;
	}


	@Test
	public void testSwapRate() {
		final double yearFraction = 0.5;

		final double[] zeroCouponBondCurve = { 0.9986509108, 0.9949129829, 0.9897033769, 0.9835370208, 0.9765298116,
				0.9689909565 };

		final int curveLength = zeroCouponBondCurve.length;
		/*
		 * we could also implement an overloaded version of fromBondToLibors for the case when the time step of the tenure structure
		 * is constant
		 */
		final TimeDiscretization tenureStructure = new TimeDiscretizationFromArray(yearFraction, curveLength, yearFraction);

		//this is the Libor rate curve which corresponds to zeroCouponBondCurve
		final double[] liborRatesCurve = fromBondToLibors(tenureStructure, zeroCouponBondCurve);

		//first we compute the par swap rate giving the zero coupon bond curve
		final Swap swapCalculator = new SwapWithoutFinmath(yearFraction, zeroCouponBondCurve, true);

		final double parSwapRate = swapCalculator.getParSwapRate(yearFraction/*in this way, we compute it more efficiently*/);
		System.out.println("The par swap rate is " + parSwapRate);

		//and let's check if it actually makes the value of the swap equal to zero (everything as last time until now)
		final double swapValue = swapCalculator.getSwapValue(parSwapRate, yearFraction);
		System.out.println("The value of the swap for the par swap rate is " + swapValue);

		final double tolerance = 1E-15;

		// we test if this value is zero, with a tolerance equal to the machine precision
		Assert.assertEquals(0, swapValue, tolerance);

		//now we do the same, but with the Libors

		final Swap swapCalculatorWithLibors = new SwapWithoutFinmath(yearFraction, liborRatesCurve, false);
		final double parSwapRateWithLibors = swapCalculatorWithLibors.getParSwapRate(yearFraction);

		System.out.println("The par swap rate giving the Libor rates is " + parSwapRateWithLibors);

		Assert.assertEquals(parSwapRate, parSwapRateWithLibors, tolerance);


	}

}
