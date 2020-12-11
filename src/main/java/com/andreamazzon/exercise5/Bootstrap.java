package com.andreamazzon.exercise5;

import java.util.ArrayList;

import net.finmath.rootfinder.BisectionSearch;

/**
 * This class implements the zero coupon bond curve bootstrapping from the values of par swap rates: the idea
 * is that you take the formula for the par swap rate and you use it -knowing the value of the swap rate in the market-
 * to determine the value of one or two bonds, also knowing the value of the other bonds. Here we suppose that the
 * time step of the tenure structure is constant.
 * If some values of the swap rate are missing (for example, if we have annual swap rates and semi-annual payments) a
 * root finder algorithm is used in order to bootstrap the curve, together with a linear interpolation of the
 * logarithm of the bonds.

 * @author: Andrea Mazzon
 */

public class Bootstrap {

	private final ArrayList<Double> computedBonds = new ArrayList<Double>();
	/*
	 * we use it in order to compute the bootstrapped bonds. We want it to be updated every time we get a new
	 * bond (or two new bonds)
	 */
	private double sumOfBonds;

	private int computedBondsSize;//it is used in nextTwoBondsFromParSwapRate

	private final double yearFraction;

	private final double firstBond;

	public Bootstrap(Double firstBond, Double secondBond, double yearFraction) {
		computedBonds.add(firstBond);//the first two bonds are given
		computedBonds.add(secondBond);
		computedBondsSize = 2;
		this.sumOfBonds = secondBond;//the sum is initialized. Note: the first bond is not included!
		this.yearFraction = yearFraction;
		this.firstBond = firstBond;
	}

	/**
	 *  Computes a new bond from the previously computed ones and from the par swap Rate:
	 *  look at the form of the swap rate in the script. Internally, it also add the new bond
	 *  to the bond list and updates the sum
	 *  @param parSwapRate, the par swap rate for the given period
	 */
	public void nextBondFromParSwapRate(double parSwapRate) {
		final double newBond = (firstBond - yearFraction * parSwapRate * sumOfBonds) /
				(1 + parSwapRate * yearFraction);
		sumOfBonds += newBond;//note: the sum is updated!
		computedBonds.add(newBond);
		computedBondsSize ++;
	}

	/**
	 * Here it is assumed that the value of one par swap rate is missing, so that the values
	 * of two bonds have to be computed (for example, this is the case of semi-annual payments when
	 * only annual swap rates are given). In practice, you know the bonds until time T_{k-2}, and you know
	 * the par swap rate S_k at time T_k. You have to compute P(T_{k-1}) and P(T_k).
	 * In order to do this, a rootfinder algorithm is used: the objective function is the difference from the
	 * given par swap rate S_k and the one computed when the two missing bonds are the one computed at the present
	 * iteration and the bond given by interpolation.
	 * @param swapRate, the par swap rate for the given period
	 * @param tolerance, the tolerance to be given to the root finder algorithm: the algorithm gives us x_n as a root
	 * of our function if |x_n-x_{n-1}| < tolerance, at the n-th iteration
	 */
	public void nextTwoBondsFromParSwapRate(double swapRate) {

		final double lastBond = computedBonds.get(computedBondsSize - 1);//P(T_{k-2};0)
		/*
		 * We use the BisectionSearch class of the finmath library. It can be used to find the zero of monotone functions
		 * on some interval, whose extremes are given in the constructor of the class. Here we know that the value of the
		 * bond has to be positive, but smaller than the value of the last computed bond (as it has an higher maturity)
		 */
		final BisectionSearch rootFinder = new BisectionSearch(
				0.0001,//left point of the interval where we search
				lastBond//right point
				);

		while (!rootFinder.isDone()/*look at the class in the Finamth library: Boolean which is True when the points are close enough*/) {
			//next "try" to get the value of the new bond by which the difference of the par swap rate is close to zero
			final double x = rootFinder.getNextPoint();
			//value of the difference between for the new trial
			final double y = differenceSwapRateAtMissingBond(swapRate, x);

			rootFinder.setValue(y);    //the algorithm is repeated for the new difference
		}
		final Double computedBond = rootFinder.getBestPoint();//P(T_k;0)
		//P(T_{n-1}) is computed by interpolation of P(T_{k-2};0) and P(T_k;0)
		final Double interpolatedBond = interpolate(lastBond,computedBond);//P(T_{k-1};0)
		sumOfBonds += interpolatedBond + computedBond;
		computedBonds.add(interpolatedBond);
		computedBonds.add(computedBond);
		computedBondsSize += 2;
	}

	/*
	 * This method computes the value of a bond for a subperiod, through the linear interpolation of
	 * the logarithm of the discount factors.
	 */
	private Double interpolate(double bondT0, double bondT1) {
		return Math.exp(0.5*(Math.log(bondT0) + Math.log(bondT1)));
	}

	/*
	 * Valuation of the difference between a given swap rate and the one calculated for a vector of already
	 * computed bonds, to which the new bond is added together with an interpolated one. It enters in
	 * the root finder algorithm above.
	 */
	private double differenceSwapRateAtMissingBond(double swapRate, double missingBond) {
		/*
		 * By means of the rootfinder algorithm, a value of missingBond will be computed in order
		 * to the following quantity to be close to zero
		 */
		return (computedBonds.get(0) - missingBond) /
				(yearFraction *
						(sumOfBonds +
								interpolate(computedBonds.get(computedBondsSize - 1),missingBond)
						+ missingBond))
				- swapRate;
	}

	public ArrayList<Double> getBonds(){
		return computedBonds;
	}
}