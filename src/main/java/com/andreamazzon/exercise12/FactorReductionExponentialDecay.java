package com.andreamazzon.exercise12;

import net.finmath.functions.LinearAlgebra;
import net.finmath.time.TimeDiscretization;

/**
 * This class creates two correlation matrices for a LIBOR market model:
 * one correlation matrix with exponential decay and one correlation matrix
 * resulting from factor reduction. The difference between the two can be
 * tested depending on the number of factors and on the decay parameter.
 *
 * @author Andrea Mazzon
 *
 */
public class FactorReductionExponentialDecay {

	private final TimeDiscretization liborPeriodDiscretization;
	private final int numberOfTimeSteps;

	//the only data we need is the time discretization of the tenor structure
	public FactorReductionExponentialDecay(
			TimeDiscretization liborPeriodDiscretization) {
		this.liborPeriodDiscretization = liborPeriodDiscretization;
		numberOfTimeSteps = liborPeriodDiscretization.getNumberOfTimeSteps();
	}

	/**
	 * Returns the original correlation matrix (i.e., without factor reduction) resulting
	 * from an exponentially decaying correlation model with parameter corrDecay.
	 * This is a matrix with entries
	 * A_{i,j}=corr(W_i,W_j)
	 * where
	 * corr(W_i,W_j)=exp(-corrDecay|T_i-T_j|)
	 * @param corrDecay the correlation decay parameter
	 * @return the correlation matrix
	 */
	public double[][] getOriginalCorrelationMatrix(double corrDecay) {
		corrDecay = Math.max(corrDecay, 0); // Negative values of corrDecay do not make sense.

		final double[][] correlationMatrix = new double[numberOfTimeSteps][numberOfTimeSteps];
		// Construction of the n factors correlation matrix: use a double for loop
		for(int row=0; row<correlationMatrix.length; row++) {
			for(int col=0; col<row; col++) {
				// Exponentially decreasing instantaneous correlation
				correlationMatrix[row][col] = Math.exp(
						-corrDecay * (liborPeriodDiscretization.getTime(row)
								- liborPeriodDiscretization.getTime(col)));
				correlationMatrix[col][row] = correlationMatrix[row][col];
			}
			correlationMatrix[row][row] = 1;
		}
		return correlationMatrix;
	}

	/**
	 * Returns the factor matrix resulting from factor reduction. This is the matrix
	 * F^r at page 534 of the script
	 * @param corrDecay the correlation decay parameter
	 * @param numberOfFactors the number of the most relevant factors
	 * @return the factor matrix
	 */
	public double[][] getFactorMatrix(double corrDecay, int numberOfFactors) {
		final double[][] correlationMatrix = getOriginalCorrelationMatrix(corrDecay);

		return LinearAlgebra.factorReduction(correlationMatrix, numberOfFactors);
	}

	/**
	 * Constructs and returns the reduced correlation matrix from the reduced factor matrix
	 * returned by getFactorMatrix. That is, it returns the matrix whose entry (i,j) is given by
	 * \sum_{k=1}^numberOfFactors F^r(i,k)F^r(j,k)
	 *
	 * @param numberOfFactors The number of factors to be used.
	 * @param corrDecay Decay parameter. It has to be positive. Negative values will be floored to 0.
	 */
	public double[][] getReducedCorrelationMatrix(double corrDecay, int numberOfFactors) {

		final double[][] reducedCorrelationMatrix = new double[numberOfTimeSteps][numberOfTimeSteps];

		// factor decomposition (and reduction if numberOfFactors < correlationMatrix.columns()): we get F^r
		final double[][] factorMatrix = getFactorMatrix(corrDecay, numberOfFactors);

		//we construct here the new correlation matrix basing on the factor matrix. Use again a double for loop
		for(int component1=0; component1<numberOfTimeSteps; component1++) {
			for(int component2=0; component2<component1; component2++) {
				double correlation = 0.0;
				for(int factor=0; factor<numberOfFactors; factor++) {
					correlation += factorMatrix[component1][factor] * factorMatrix[component2][factor];
				}
				reducedCorrelationMatrix[component1][component2] = correlation;
				reducedCorrelationMatrix[component2][component1] = correlation;
			}
			reducedCorrelationMatrix[component1][component1] = 1.0;
		}
		return reducedCorrelationMatrix;
	}


	/**
	 * Compare and returns the average absolute difference between elements of the original matrix with respect to the
	 * one obtained by performing factor reduction. That is, it returns
	 * 2/n^2\sum_{i,j=1}^n |A_{i,j}-ReducedA_{i,j}|,
	 * where A is the original matrix and ReducedA the one after factor reduction.
	 * @param corrDecay
	 * @param numberOfFactors
	 * @return
	 */
	public double getErrorFromFactorReduction (double corrDecay, int numberOfFactors) {
		final double[][] originalCorrelationMatrix = getOriginalCorrelationMatrix(corrDecay);
		final double[][] reducedCorrelationMatrix = getReducedCorrelationMatrix(corrDecay, numberOfFactors);
		double diffCorrelation = 0.0;
		for(int component1=0; component1<numberOfTimeSteps; component1++) {
			for(int component2=0; component2<component1; component2++) {
				diffCorrelation +=
						Math.abs(originalCorrelationMatrix[component1][component2]
								- reducedCorrelationMatrix[component1][component2]);
			}
		}
		final double averageDiffCorrelation = 2.0 * diffCorrelation/(numberOfTimeSteps*numberOfTimeSteps);
		return averageDiffCorrelation;
	}
}