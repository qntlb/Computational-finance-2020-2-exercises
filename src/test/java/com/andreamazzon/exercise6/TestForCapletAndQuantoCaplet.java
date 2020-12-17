package com.andreamazzon.exercise6;
import org.junit.Assert;
import org.junit.Test;

import net.finmath.functions.AnalyticFormulas;

/**
 * This is a test class, checking if the Caplet value computed by the method
 * Products.calculateCapletValueBlackModel is equal, up to a given tolerance,
 * to the one computed by AnalyticFormulas.blackModelCapletValue in the finmath library.
 *
 * @author Andrea Mazzon
 */

public class TestForCapletAndQuantoCaplet {

	//discount factor
	final double discountFactorAtMaturity = 0.91;

	//parameters for the Libor dynamics
	final double initialForwardLibor = 0.05;
	final double liborVolatility = 0.3;

	//parameters for the both the options
	final double fixingDate = 1;
	final double paymentDate = 2;

	final double strike = 0.044;
	final double notional = 10000;

	@Test
	public void testCaplet() {

		//tolerance for assertEqual
		final double tolerance = 1E-13;
		final double maturityOfTheOption = fixingDate;
		final double periodLength = paymentDate - fixingDate;

		final double finmathLibraryValue = notional * AnalyticFormulas.blackModelCapletValue(
				initialForwardLibor, liborVolatility, maturityOfTheOption, strike, periodLength,
				discountFactorAtMaturity);

		System.out.println("Value of the caplet computed by the Finmath library: " + finmathLibraryValue + "\n");

		final double ourValue = Products.calculateCapletValueBlackModel(initialForwardLibor, liborVolatility,
				strike, fixingDate, paymentDate, discountFactorAtMaturity, notional);

		System.out.println("Value of the caplet computed by our method: " + ourValue + "\n");

		Assert.assertEquals(finmathLibraryValue, ourValue, tolerance);

	}

	@Test
	public void testQuantoCaplet() {

		//foreign Libor rate dynamics
		final double initialForwardForeignLibor = initialForwardLibor;
		final double liborForeignVolatility = liborVolatility;

		//forward fx rate dynamics
		final double fxVolatility = 0.2;

		//correlation between the forward fx rate process and the Libor rate process
		final double correlationFxLibor = 0.4;

		//the quanto rate (i.e., the the constant conversion factor)
		final double quantoRate = 0.9;

		final double quantoPrice = Products.calculateQuantoCapletValue(
				initialForwardForeignLibor, liborForeignVolatility, fxVolatility, correlationFxLibor,
				fixingDate, paymentDate, strike, discountFactorAtMaturity, notional,
				quantoRate);

		System.out.println("Price of the Quanto Caplet: " + quantoPrice);

	}

}