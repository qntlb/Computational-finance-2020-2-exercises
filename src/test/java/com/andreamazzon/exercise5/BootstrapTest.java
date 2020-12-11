package com.andreamazzon.exercise5;

import java.text.DecimalFormat;
import java.util.ArrayList;

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
 * @author Andrea Mazzon
 *
 */
public class BootstrapTest {

	@Test
	void testBootstrap() {

		final DecimalFormat FORMATTERREAL4 = new DecimalFormat("0.0000");
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
			bootstrap.nextTwoBondsFromParSwapRate(annualSwapRate);
		}

		final ArrayList<Double> computedBonds = bootstrap.getBonds();

		//print the value of the bonds
		for (int i = 0; i <computedBonds.size(); i++) {
			System.out.println("The value of the time " + yearFraction*(i+1) + " bond is : " +
					FORMATTERREAL4.format(computedBonds.get(i)));
		}
	}
}

