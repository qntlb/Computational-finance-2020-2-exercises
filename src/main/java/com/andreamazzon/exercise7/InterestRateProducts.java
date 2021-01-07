package com.andreamazzon.exercise7;

import com.andreamazzon.exercise4.Swap;
import com.andreamazzon.exercise4.SwapWithoutFinmath;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class has methods computing the value of a caplet, of a quanto caplet, of a caplet in arrears
 * and of a swaption, under the Black model.
 *
 * @auhor: Andrea Mazzon
 */
public class InterestRateProducts {

	/**
	 * This method calculates and return the value of a Caplet under the Black
	 * model.
	 *
	 * @param initialForwardLibor,       i.e. L_0 = L(T_1,T_2;0)
	 * @param liborVolatility,           the volatility of the LIBOR process under
	 *                                   the Black model
	 * @param strike,                    the strike of the option
	 * @param fixing,                    i.e. T_1
	 * @param paymentDate,               i.e. T_2
	 * @param paymentDateDiscountFactor, i.e. P(T_2;0)
	 * @param notional,                  i.e. N
	 */
	public static double calculateCapletValueBlackModel(double initialForwardLibor, double liborVolatility,
			double strike, double fixingDate, double paymentDate, double paymentDateDiscountFactor, double notional) {
		final double periodLength = paymentDate - fixingDate;
		/*
		 * the discount factor is the bond maturing at the payment date; the LIBOR rate
		 * has no drift because we are changing to a certain equivalent measure under
		 * which it exhibits martingale dynamics.
		 */
		return notional * paymentDateDiscountFactor * periodLength
				* AnalyticFormulas.blackScholesOptionValue(initialForwardLibor, 0, liborVolatility, fixingDate, strike);
	}
	/**
	 * This method calculates and return the value of a Caplet under the Black
	 * model, using a Monte Carlo method.
	 *
	 * @param initialForwardLibor,       i.e. L_0 = L(T_1,T_2;0)
	 * @param liborVolatility,           the volatility of the LIBOR process under
	 *                                   the Black model
	 * @param strike,                    the strike of the option
	 * @param fixing,                    i.e. T_1
	 * @param paymentDate,               i.e. T_2
	 * @param paymentDateDiscountFactor, i.e. P(T_2;0)
	 * @param notional,                  i.e. N
	 * @throws CalculationException
	 */
	public static double calculateCapletValueBlackModelWithMonteCarlo(double initialForwardLibor,
			double liborVolatility, double strike, double fixingDate, double paymentDate,
			double paymentDateDiscountFactor, double notional, double timeStep, int numberOfSimulations)
					throws CalculationException {

		// we first get the number of the time steps of the time discretization. Note:
		// it has to be int
		final int numberOfTimeSteps = (int) ((int) fixingDate / timeStep);

		final TimeDiscretization times = new TimeDiscretizationFromArray(0.0, numberOfTimeSteps, timeStep);
		// we construct the simulation..
		final AssetModelMonteCarloSimulationModel blackModel = new MonteCarloBlackScholesModel(times,
				numberOfSimulations, initialForwardLibor, 0, liborVolatility);
		// ..and the object for the ruropean option
		final EuropeanOption europeanOption = new EuropeanOption(fixingDate, strike);

		final double periodLength = paymentDate - fixingDate;
		/*
		 * the discount factor is the bond maturing at the payment date; the LIBOR rate
		 * has no drift because we are changing to a certain equivalent measure under
		 * which it exhibits martingale dynamics.
		 */
		return notional * paymentDateDiscountFactor * periodLength * europeanOption.getValue(blackModel);
	}

	/**
	 * This method calculates and return the value of a Quanto Caplet, supposing
	 * log-normal dynamics for both the foreign forward rate and the forward fx rate
	 *
	 * @param initialForeignForwardLibor, the foreign forward LIBOR evaluated at
	 *                                    time 0
	 * @param liborForeignVolatility,     the volatility of the foreign forward
	 *                                    LIBOR process
	 * @param fxVolatility,               the volatility of the forward FX rate
	 *                                    process
	 * @param correlationFxLibor,         the correlation between the foreign LIBOR
	 *                                    process and the forward FX rate process
	 * @param fixing,                     i.e. T_1
	 * @param paymentDate,                i.e. T_2
	 * @param strike,                     the strike of the option
	 * @param paymentDateDiscountFactor,  i.e. P(T_2;0)
	 * @param notional,                   i.e. N
	 * @param quantoRate,                 the constant conversion factor
	 */
	public static double calculateQuantoCapletValue(double initialForeignForwardLibor, double foreignLiborVolatility,
			double fxVolatility, double correlationFxForeignLibor, double fixingDate, double paymentDate, double strike,
			double paymentDateDiscountFactor, double notionalInForeignCurrency, double quantoRate) {
		final double periodLength = paymentDate - fixingDate;

		/*
		 * Under the pricing measure, the foreign LIBOR has a drift given by
		 * correlationFxLibor * liborForeignVolatility * fxVolatility. This determines
		 * the exponential term which multiplies initialForeignForwardLibor inside the
		 * Black-Scholes formula.
		 */
		return notionalInForeignCurrency * quantoRate * paymentDateDiscountFactor * periodLength
				* AnalyticFormulas.blackScholesOptionValue(initialForeignForwardLibor
						* Math.exp(-correlationFxForeignLibor * foreignLiborVolatility * fxVolatility * fixingDate), 0,
						// - correlationFxForeignLibor * foreignLiborVolatility * fxVolatility,
						foreignLiborVolatility, fixingDate, strike);
	}

	/**
	 * Calculate the value of a Caplet payed in arrears under the Black
	 * model.
	 *
	 * @param initialForwardLibor,    i.e. L_0 = L(T_1,T_2;0)
	 * @param liborVolatility,        the volatility of the LIBOR process
	 * @param strike,                 the strike of the option
	 * @param fixing,                 i.e. T_1
	 * @param maturity,               i.e. T_2
	 * @param maturityDiscountFactor, i.e. P(0;T_2)
	 * @param notional,               i.e. N
	 */
	public static double calculateCapletInArrearsBlack(double initialForwardLibor, double liborVolatility,
			double strike, double fixingDate, double paymentDate, double paymentDateDiscountFactor, double notional) {
		final double periodLength = paymentDate - fixingDate;


		//Formula for the pricing of a Caplet payed in arrears, see the solution to exercise 2 for its derivation
		return notional * paymentDateDiscountFactor * periodLength
				* AnalyticFormulas.blackScholesOptionValue(initialForwardLibor, 0, liborVolatility, fixingDate, strike) // classic
				+ notional * paymentDateDiscountFactor * periodLength * periodLength * initialForwardLibor
				* AnalyticFormulas.blackScholesOptionValue(
						initialForwardLibor * Math.exp(liborVolatility * liborVolatility * fixingDate), 0,
						liborVolatility, fixingDate, strike);
	}

	/**
	 * Calculate the value of a swaption under the Black model. The price of the
	 * swaption is computed as the price of a call option on the par swap rate S,
	 * times the annuity. For this reason, among other things we also need the
	 * initial value of the par swap rate, in order to give it to the method
	 * computing the value of the call option.
	 *
	 * @param bondCurve,          the zero coupon bond curve
	 * @param tenureStructure,    the payment dates, given as a TimeDiscretization.
	 * @param swapRateVolatility, the volatility of the stochastic process modelling
	 *                            the evolution of the par swap rate (which has
	 *                            log-normal dynamics under the Black model)
	 * @param strike,             the strike of the option: the swaption pays
	 *                            max (V_swap(T1),0) where V_swap is the value of a
	 *                            swap with swap rates S_i=K  for all i
	 * @param notional,           i.e. N
	 */
	public static double calculateSwaptionValueBlack(double[] zeroBondCurve, TimeDiscretization tenureStructure,
			double strike, double notional, double swapRateVolatility) {
		final int curveLength = zeroBondCurve.length;
		// we need this to compute annuity and par swap rate
		final Swap mySwap = new SwapWithoutFinmath(tenureStructure, zeroBondCurve, true);
		final double initialSwapRate = mySwap.getParSwapRate();
		// we know that initialSwapRate = (zeroBondCurve[0] - zeroBondCurve[curveLength - 1]) / annuity
		final double annuity = (zeroBondCurve[0] - zeroBondCurve[curveLength - 1]) / initialSwapRate;
		final double exerciseDate = tenureStructure.getTime(1); // T_1
		return notional * annuity * AnalyticFormulas.blackScholesOptionValue(initialSwapRate, 0, swapRateVolatility,
				exerciseDate, strike);
	}

	/**
	 * Calculate the value of a swaption under the Black model when the payment
	 * dates are evenly distributed, i.e., when the time step of the tenure
	 * structure is constant.
	 *
	 * @param bondCurve,      the zero coupon bond curve
	 * @param yearFraction,   the length of the intervals between payment dates
	 * @param swapVolatility, the volatility of the swap process (log-normal
	 *                        dynamics)
	 * @param strike,         the strike of the option: S_i=K for all i
	 * @param notional,       i.e. N
	 */
	public static double calculateSwaptionValueBlack(double[] zeroBondCurve, double yearFraction,
			double strike, double notional, double swapRateVolatility) {
		final int curveLength = zeroBondCurve.length;
		// we need id to compute annuity and par swap rate. Note the overloaded constructor with yearFraction
		final Swap mySwap = new SwapWithoutFinmath(yearFraction, zeroBondCurve, true);
		// overloaded method: we save time
		final double initialSwapRate = mySwap.getParSwapRate(yearFraction);
		final double annuity = (zeroBondCurve[0] - zeroBondCurve[curveLength - 1]) / initialSwapRate;
		final double exerciseDate = yearFraction; // T_1
		return notional * annuity
				* AnalyticFormulas.blackScholesOptionValue(initialSwapRate, 0, swapRateVolatility, exerciseDate, strike);

	}

}
