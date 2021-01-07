package com.andreamazzon.exercise7;


import java.text.DecimalFormat;

import org.junit.jupiter.api.Test;

/**
 * This is a test class with a method getting and printing the price of a caplet, of a caplet in arrears and of a
 * swaption under the Black model.
 *
 * @author Andrea Mazzon
 *
 */
public class InterestRateProductsTest {

	@Test
	void testProducts() {
		final DecimalFormat FORMATTERREAL2 = new DecimalFormat("0.00");
		final DecimalFormat FORMATTERREAL4 = new DecimalFormat("0.0000");


		//parameters for the swaption
		final double[] bondCurve = { 0.98, 0.95, 0.92, 0.9, 0.87 };
		final double yearFraction = 1;
		final double swapRateVolatility = 0.3;
		final double swaptionStrike = 0.03;
		final double notional = 10000;

		final double swaptionValue = InterestRateProducts.calculateSwaptionValueBlack(bondCurve, yearFraction,
				swaptionStrike, notional, swapRateVolatility);

		System.out.println("Swaption value: " + FORMATTERREAL4.format(swaptionValue));
		System.out.println();



		//parameters for the caplet and for the caplet in arrears
		final double initialForwardLibor = 0.05;
		final double liborVolatility = 0.3;

		final double strikeOfTheCaplet = 0.044;
		final double fixing = 1;
		final double paymentDate = 2;
		final double paymentDateDiscountFactor = 0.91;

		final double capletPrice = InterestRateProducts.calculateCapletValueBlackModel(initialForwardLibor, liborVolatility,
				strikeOfTheCaplet, fixing, paymentDate, paymentDateDiscountFactor, notional);

		System.out.println("Price of the caplet: " + FORMATTERREAL4.format(capletPrice));
		System.out.println();


		final double capletInArrearsPrice = InterestRateProducts.calculateCapletInArrearsBlack(initialForwardLibor,
				liborVolatility, strikeOfTheCaplet, fixing, paymentDate,
				paymentDateDiscountFactor, notional);

		System.out.println("Price of the caplet in arrears: " + FORMATTERREAL4.format(capletInArrearsPrice));

		System.out.println("Price of the convexity adjustment: " +
				FORMATTERREAL4.format(capletInArrearsPrice - capletPrice));
		System.out.println();

	}

}
