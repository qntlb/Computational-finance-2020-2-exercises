package com.andreamazzon.exercise1;

import java.util.HashMap;
import java.util.Map;

import net.finmath.exception.CalculationException;
import net.finmath.modelling.Model;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.stochastic.RandomVariable;

/**
 * This class extends AbstractAssetMonteCarloProduct, and represents an "asset or nothing" option, that is,
 * an option on an underlying S such that the payoff at maturity T is
 * S(T) 1_{S(T)>K}.
 * Note that we have only to implement the method
 * getValue(final double evaluationTime, final AssetModelMonteCarloSimulationModel model).
 *
 * @author Andrea Mazzon
 *
 */
public class AssetOrNothing extends AbstractAssetMonteCarloProduct {

	private final double maturity;
	private final double strike;
	private final Integer underlyingIndex;//it can be useful if we have a multi-dimensional process. Otherwise, it is always 0
	private final String nameOfUnderliyng;

	/**
	 * Construct a product representing an "asset or nothing" option on an asset S (where S is the asset with index
	 * 0 from the model).
	 * @param underlyingName Name of the underlying
	 * @param maturity The maturity T in the option payoff S(T) 1_{S(T)>K}
	 * @param strike The strike K in the option payoff S(T) 1_{S(T)>K}.
	 */
	public AssetOrNothing(final String underlyingName, final double maturity, final double strike) {
		super();
		nameOfUnderliyng	= underlyingName;
		this.maturity			= maturity;
		this.strike				= strike;
		underlyingIndex		= 0;
	}

	/**
	 * Construct a product representing an "asset or nothing" option on an asset S (where S the asset with index
	 * underlyingIndex from the model).
	 * @param maturity The maturity T in the option payoff S(T) 1_{S(T)>K}
	 * @param strike The strike K in the option payoff S(T) 1_{S(T)>K}.
	 * @param underlyingIndex The index of the underlying to be fetched from the model.
	 */
	public AssetOrNothing(final double maturity, final double strike, final int underlyingIndex) {
		super();
		this.maturity			= maturity;
		this.strike				= strike;
		this.underlyingIndex	= underlyingIndex;
		nameOfUnderliyng	= null;		// Use underlyingIndex
	}

	/**
	 * Construct a product representing an "asset or nothing" option on an asset S (where S the asset with index 0 from the model).
	 * @param maturity The maturity T in the option payoff max(S(T)-K,0)
	 * @param strike The strike K in the option payoff max(S(T)-K,0).
	 */
	public AssetOrNothing(final double maturity, final double strike) {
		this(maturity, strike, 0);
	}

	/**
	 * This method returns the value random variable of the product within the specified model, evaluated at a given evalutationTime.
	 * In this case, the product is an "asset or nothing" option, whose payoff is max(S(T)-K,0). Here S is the asset with index
	 * underlyingIndex from the model.
	 *
	 * @param evaluationTime The time on which this products value should be observed.
	 * @param model The model used to price the product. It gives the underlying of the option.
	 * @return The random variable representing the value of the product discounted to evaluation time
	 * @throws net.finmath.exception.CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	@Override
	public RandomVariable getValue(final double evaluationTime, final AssetModelMonteCarloSimulationModel model) throws CalculationException {

		// Get S(T)
		final RandomVariable underlyingAtMaturity	= model.getAssetValue(maturity, underlyingIndex);

		//the payoff. Note the application of the method choose. Note that the second argument must be of type RandomVariable!
		RandomVariable values = underlyingAtMaturity.sub(strike).choose(underlyingAtMaturity, new RandomVariableFromDoubleArray(0.0));

		//or:
		//final DoubleUnaryOperator payoffFunction = (x) -> (x-strike>0?x:0);
		//RandomVariable values = underlyingAtMaturity.apply(payoffFunction);

		// Discounting...
		final RandomVariable numeraireAtMaturity	= model.getNumeraire(maturity);
		final RandomVariable monteCarloWeights		= model.getMonteCarloWeights(maturity);
		values = values.div(numeraireAtMaturity).mult(monteCarloWeights);

		// ...to evaluation time.
		final RandomVariable	numeraireAtEvalTime			= model.getNumeraire(evaluationTime);
		final RandomVariable	monteCarloWeightsAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		values = values.mult(numeraireAtEvalTime).div(monteCarloWeightsAtEvalTime);

		return values;
	}

	@Override
	public Map<String, Object> getValues(final double evaluationTime, final Model model) {
		final Map<String, Object>  result = new HashMap<>();

		try {
			final double value = getValue(evaluationTime, (AssetModelMonteCarloSimulationModel) model).getAverage();
			result.put("value", value);
		} catch (final CalculationException e) {
			result.put("exception", e);
		}

		return result;
	}

	@Override
	public String toString() {
		return "EuropeanOption [maturity=" + maturity + ", strike=" + strike + ", underlyingIndex=" + underlyingIndex
				+ ", nameOfUnderliyng=" + nameOfUnderliyng + "]";
	}

	public double getMaturity() {
		return maturity;
	}

	public double getStrike() {
		return strike;
	}

	public Integer getUnderlyingIndex() {
		return underlyingIndex;
	}

	public String getNameOfUnderliyng() {
		return nameOfUnderliyng;
	}
}