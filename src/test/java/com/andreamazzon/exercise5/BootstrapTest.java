package com.andreamazzon.exercise5;

import java.text.DecimalFormat;

/**
 * Zero coupon bond curve bootstrapping from swaps. If some values of the swap rate
 * are missing (for example, if we have annual swap rates and semi-annual payments) a root finder
 * algorithm is used in order to bootstrap the curve, together with a linear interpolation of the
 * logarithm of the discount factors (bonds).
 * @author: Andrea Mazzon
 */
import org.junit.jupiter.api.Test;

/**
 * Here we "test" the class Bootstrap (even if we have no JUnit specific method) by giving to its constructor
 * two initial bonds along with a time step (semi-annual). We then iteratively call the methods nextBondFromParSwapRate
 * and nextTwoBondsFromParSwapRate: first we have semi-annual par swap rates, then only annual swap rates.
 *
 * @author andreamazzon
 *
 */
public class BootstrapTest {

	@Test
	void testBootstrap() {

		final DecimalFormat FORMATTERREAL4 = new DecimalFormat("0.0000");
		final double tolerance = 1E-11;//tolerance for the root finder method
		final double[] firstBonds = { 0.98, 0.975 };
		final double yearFraction = 0.5;

		final Bootstrap bootstrap = new Bootstrap(firstBonds[0],firstBonds[1], yearFraction);

		final double[] semiAnnualSwapRates = {0.0086, 0.0077, 0.0073, 0.0084 };
		System.out.println("Computed bonds: \n");

		//here every par swap rate is known
		for (final double semiAnnualSwapRate : semiAnnualSwapRates) {
			bootstrap.nextBondFromParSwapRate(semiAnnualSwapRate);
		}

		final double[] annualSwapRates = {0.0075,  0.0085, 0.0095, 0.0092 };

		//now only the par swap rates for yearly maturities are given
		for (final double annualSwapRate : annualSwapRates) {
			bootstrap.nextTwoBondsFromParSwapRate(annualSwapRate,tolerance);
		}

		//print the value of the bonds
		for (int i = 0; i <bootstrap.computedBonds.size(); i++) {
			System.out.println("The value of the time " + yearFraction*(i+1) + " bond is : " +
					FORMATTERREAL4.format(bootstrap.computedBonds.get(i)));
		}
	}
}

