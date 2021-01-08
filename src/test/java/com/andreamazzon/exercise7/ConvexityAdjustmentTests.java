package com.andreamazzon.exercise7;


import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This is a test class, testing the convexity adjustment for a natural floater
 * and for a LIBOR in arrears.
 *
 * @author Andrea Mazzon
 */
public class ConvexityAdjustmentTests {

	@Test
	void ConvexityAdjustmentTest() throws CalculationException {

		final DecimalFormat FORMATTERREAL4 = new DecimalFormat("0.0000");

		final double initialTime = 0;
		final double fixingTime = 1;
		final double maturityTime = 2;
		final double notional = 10000;
		final double firstDiscountingFactor = 0.95;// P(T_1;0)
		final double secondDiscountingFactor = 0.9;// P(T_2;0)

		final double floaterTimeInterval = maturityTime - fixingTime;

		// floater price: N(P(T_1;0)-P(T_2;0))
		final double analiticFloater = notional * (firstDiscountingFactor - secondDiscountingFactor);

		System.out.println("Natural floater analytic price " + FORMATTERREAL4.format(analiticFloater));

		/*
		 * we now want to compute the convexity adjustment: we need the LIBOR volatility
		 * and the initial LIBOR L(T_1,T_2;0)
		 */

		final double liborVolatility = 0.25;
		// we get L(T_1,T_2;0) from P(T_1;0) and P(T_2;0)
		final double initialForwardLibor = 1 / floaterTimeInterval * (firstDiscountingFactor / secondDiscountingFactor - 1);

		// convexity adjustment: N * P(T_2;0) * L(0)^2 * (T_2-T_1)^2 * exp(sigma^2 T_1)
		final double analyticConvexityAdjustment = notional * secondDiscountingFactor * initialForwardLibor * initialForwardLibor
				* floaterTimeInterval * floaterTimeInterval * Math.exp(liborVolatility * liborVolatility * fixingTime);

		//System.out.println("Convexity Adjustment analytic price " + analyticConvexityAdjustment);

		// price of the natural floater + convexity adjustment
		final double analyticFloaterInArrears = analiticFloater + analyticConvexityAdjustment;

		System.out.println("Floater in arrears analytic price: " + FORMATTERREAL4.format(analyticFloaterInArrears));
		System.out.println();

		// Monte Carlo implementation
		final int numberOfPaths = 100000;
		final int numberOfTimeSteps = 100;
		final double stepSize = fixingTime / numberOfTimeSteps;
		// discretization of the time interval..
		final TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, stepSize);
		// and discretization of the simulated process, as usual
		final MonteCarloBlackScholesModel bsLiborModel = new MonteCarloBlackScholesModel(times, numberOfPaths,
				initialForwardLibor, 0.0, liborVolatility);

		// we get here all the realizations of the final value of the LIBOR, i.e., P(T_1,T_2;T_1)
		final RandomVariable finalLibors = bsLiborModel.getAssetValue(fixingTime, 0);

		/*
		 * we discount the floater value at final time computed by Monte Carlo, and we
		 * get the average
		 */
		final double montecarloFloater = secondDiscountingFactor * notional * floaterTimeInterval
				* finalLibors.getAverage();

		/*
		 * Convexity adjustment: we compute it as the discounted expectation of
		 * L(T_1)^2, multiplied by (T_2-T_1)^2, look at page 359 of the script
		 */
		final RandomVariable finalLiborsSquare = finalLibors.mult(finalLibors);

		final double montecarloConvexityAdjustment = notional * secondDiscountingFactor * floaterTimeInterval * floaterTimeInterval
				* finalLiborsSquare.getAverage();

		final double montecarloFloaterInArrears = montecarloFloater + montecarloConvexityAdjustment;

		final double tolerance = 0.01; // we want the result to be accurate up to the 0.1 %

		System.out.println("Natural floater MonteCarlo price " + FORMATTERREAL4.format(montecarloFloater));

		System.out.println("Convexity adjustment MonteCarlo price " + FORMATTERREAL4.format(montecarloConvexityAdjustment));

		System.out.println("Floater in arrears MonteCarlo price " + FORMATTERREAL4.format(montecarloFloaterInArrears));

		Assert.assertEquals(0,
				(montecarloConvexityAdjustment - analyticConvexityAdjustment) / analyticConvexityAdjustment, tolerance);

		Assert.assertEquals(0, (montecarloFloater - analiticFloater) / analiticFloater, tolerance);

	}

}

