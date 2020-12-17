package com.andreamazzon.exercise6;


import net.finmath.functions.AnalyticFormulas;

/**
 * This class has methods computing the value of a caplet and of a quanto caplet.
 *
 * @auhor: Andrea Mazzon
 */

public class Products {

	/**
	 * This method calculates and return the value of a Caplet under the Black model.
	 *
	 * @param initialForwardLibor, i.e. L_0 = L(T_1,T_2;0)
	 * @param liborVolatility, the volatility of the LIBOR process under the Black model
	 * @param strike, the strike of the option
	 * @param fixing, i.e. T_1
	 * @param paymentDate, i.e. T_2
	 * @param paymentDateDiscountFactor, i.e. P(T_2;0)
	 * @param notional, i.e. N
	 */
	public static double calculateCapletValueBlackModel(double initialForwardLibor,
			double liborVolatility,
			double strike,
			double fixingDate,
			double paymentDate,
			double paymentDateDiscountFactor,
			double notional) {
		final double periodLength = paymentDate - fixingDate;
		/*
		 * the discount factor is the bond maturing at the payment date; the LIBOR rate
		 * has no drift because we are changing to a certain equivalent measure
		 * under which it exhibits martingale dynamics.
		 */
		return notional * paymentDateDiscountFactor * periodLength
				* AnalyticFormulas.blackScholesOptionValue(initialForwardLibor,
						0,
						liborVolatility,
						fixingDate,
						strike);
	}

	/**
	 * This method calculates and return the value of a Quanto Caplet, supposing log-normal
	 * dynamics for both the foreign forward rate and the forward fx rate
	 *
	 * @param initialForeignForwardLibor, the foreign forward LIBOR evaluated at time 0
	 * @param liborForeignVolatility, the volatility of the foreign forward LIBOR process
	 * @param fxVolatility, the volatility of the forward FX rate process
	 * @param correlationFxLibor, the correlation between the foreign LIBOR process and the
	 * forward FX rate process
	 * @param fixing, i.e. T_1
	 * @param paymentDate, i.e. T_2
	 * @param strike, the strike of the option
	 * @param paymentDateDiscountFactor, i.e. P(T_2;0)
	 * @param notional, i.e. N
	 * @param quantoRate, the constant conversion factor
	 */
	public static double calculateQuantoCapletValue(double initialForeignForwardLibor,
			double foreignLiborVolatility,
			double fxVolatility,
			double correlationFxForeignLibor,
			double fixingDate,
			double paymentDate,
			double strike,
			double paymentDateDiscountFactor,
			double notionalInForeignCurrency,
			double quantoRate) {
		final double periodLength = paymentDate - fixingDate;

		/*
		 * Under the pricing measure, the foreign LIBOR has a drift given by
		 * correlationFxLibor * liborForeignVolatility * fxVolatility.
		 * This determines the exponential term which multiplies initialForeignForwardLibor
		 * inside the Black-Scholes formula.
		 */
		return notionalInForeignCurrency * quantoRate * paymentDateDiscountFactor * periodLength *
				AnalyticFormulas.blackScholesOptionValue(initialForeignForwardLibor
						* Math.exp(-correlationFxForeignLibor * foreignLiborVolatility * fxVolatility * fixingDate),
						0,
						//- correlationFxForeignLibor * foreignLiborVolatility * fxVolatility,
						foreignLiborVolatility,
						fixingDate,
						strike);
	}
}
