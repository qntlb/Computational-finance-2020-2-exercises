package com.andreamazzon.exercise12;

import java.text.DecimalFormat;

import net.finmath.exception.CalculationException;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * In this class we analyze "how much covariance" we lose when performing factor reduction in a correlation matrix
 * with a correlation given by an exponential decay model.
 *
 * @author Andrea Mazzon
 *
 */
public class TestFactorReductionExponentialDecay {

	static final DecimalFormat FORMATTERREAL2 = new DecimalFormat("0.00");

	public static void main(String[] args) throws CalculationException {

		final double initialTime = 1.0;
		final double finalTime = 10.0;
		final double yearFraction = 0.5;
		final int numberOfTimeSteps = (int) ((finalTime-initialTime)/yearFraction);

		final TimeDiscretization liborPeriodDiscretization =
				new TimeDiscretizationFromArray(0.0,numberOfTimeSteps, yearFraction);

		final FactorReductionExponentialDecay factorReduction = new
				FactorReductionExponentialDecay(liborPeriodDiscretization);

		final int numberOfFactors = 2;

		double averageError;

		System.out.println("Correlation decay parameter" + "\t" + "Average relative difference between the entries of the matrices");
		for (double corrDecay = 0; corrDecay <= 1; corrDecay += 0.1) {
			averageError= factorReduction.getErrorFromFactorReduction(corrDecay, numberOfFactors);
			System.out.println(FORMATTERREAL2.format(corrDecay) + "                            " + FORMATTERREAL2.format(averageError));
		}
	}
}
