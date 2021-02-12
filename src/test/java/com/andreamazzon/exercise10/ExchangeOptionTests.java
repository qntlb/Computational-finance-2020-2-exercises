package com.andreamazzon.exercise10;

import java.text.DecimalFormat;

import org.junit.Test;

import com.andreamazzon.exercise9.LIBORMarketModelConstruction;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;

/**
 * In this test class we print the value of an exchange option involving two LIBOR rates taken from a LIBOR market model,
 * when the correlation structure between the LIBORs is given by an exponential decay model with changing exponential
 * decay parameter.
 *
 * @author Andrea Mazzon
 *
 */
public class ExchangeOptionTests {

	static final DecimalFormat FORMATTERREAL2 = new DecimalFormat("0.00");
	static final DecimalFormat FORMATTERREAL4 = new DecimalFormat(" 0.0000;-0.0000");

	@Test
	public void test() throws CalculationException {
		//parameters for the option

		final double maturityDateFirstLibor = 2.0;
		final double paymentDateFirstLibor = 2.5;

		final double maturityDateSecondLibor = 4.0;
		final double paymentDateSecondLibor = 4.5;
		final double notional = 100;

		final AbstractLIBORMonteCarloProduct exchangeOption = new MyExchangeOption(maturityDateFirstLibor, paymentDateFirstLibor,
				maturityDateSecondLibor, paymentDateSecondLibor);

		//parameters for the LIBOR market model simulation

		final int numberOfPaths = 50000;
		final double simulationTimeStep = 0.1;
		final double LIBORTimeStep = 0.5;
		final double LIBORRateTimeHorizon = 10;

		final double a = 0.2, b = 0.1, c = 0.15, d = 0.3; //volatility structure

		//fixing times for the forwards: the forwards corresponding to other fixing times will be interpolated
		final double[] fixingForGivenForwards = { 0.5, 1.0, 3.0, 4.0, 9.0 };
		final double[] forwardsForCurve = { 0.05, 0.05, 0.05, 0.05, 0.05 };

		final double initialCorrelationDecayParameter = 0;

		final LIBORModelMonteCarloSimulationModel myLiborMarketModel =
				LIBORMarketModelConstruction.createLIBORMarketModel(
						numberOfPaths,
						simulationTimeStep,
						LIBORTimeStep, //T_i-T_{i-1}, we suppose it to be fixed
						LIBORRateTimeHorizon, //T_n
						fixingForGivenForwards,
						forwardsForCurve,
						initialCorrelationDecayParameter, // decay of the correlation between LIBOR rates. Initially zero
						a, b, c, d
						);

		double optionValue =  notional * exchangeOption.getValue(myLiborMarketModel);
		double currentCorrelationDecayParameter = initialCorrelationDecayParameter;
		//rho_{i,k}=e^(-alpha|T_i-T_k|
		final double differenceBetweenMaturities = maturityDateSecondLibor-maturityDateFirstLibor;
		double correlationBetweenTheTwoLIBORs =
				Math.exp(-currentCorrelationDecayParameter*differenceBetweenMaturities);

		//we do the for loop where the correlation decay parameter increases by 0.1 every time
		final double correlationStep = 0.1;

		System.out.println("Correlation decay parameter" + "\t" + "Correlation between the two LIBORs"+ "\t" + "Option value");

		for (int correlationIndex = 1; correlationIndex <= 10; correlationIndex++) {

			System.out.println(FORMATTERREAL2.format(currentCorrelationDecayParameter) + "\t"
					+ "                         "	+ FORMATTERREAL2.format(correlationBetweenTheTwoLIBORs)
					+ "                                   "	+ FORMATTERREAL4.format(optionValue));

			//rho_i = rho_0 + i * 0.1
			currentCorrelationDecayParameter = currentCorrelationDecayParameter += correlationStep;

			correlationBetweenTheTwoLIBORs = Math.exp(-currentCorrelationDecayParameter*differenceBetweenMaturities);

			final LIBORModelMonteCarloSimulationModel newLiborMarketModel =
					LIBORMarketModelConstruction.getCloneWithModifiedCorrelation(myLiborMarketModel,
							currentCorrelationDecayParameter);

			optionValue =  notional * exchangeOption.getValue(newLiborMarketModel);
		}

		//we print the last ones
		System.out.println(FORMATTERREAL2.format(currentCorrelationDecayParameter) + "\t"
				+ "                         "	+ FORMATTERREAL2.format(correlationBetweenTheTwoLIBORs)
				+ "                                   "		+ FORMATTERREAL4.format(optionValue));
	}
}
