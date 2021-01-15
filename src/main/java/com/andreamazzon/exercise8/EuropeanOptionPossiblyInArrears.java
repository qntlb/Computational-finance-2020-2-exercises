package com.andreamazzon.exercise8;


/**
 * This is an abstract class taking care of the computations of the classical price (i.e., for payment date T_2) and of the
 * price in arrears (i.e., for payment date T_1) of a general European option with two dates T_1 and T_2 on interest rates,
 * under the Black model for the Libor. In particular, the computation of the convexity adjustment and consequently of the
 * price in arrears is implemented here: these quantities can be effectively computed once the value of the classical price
 * as a function of the initial value of the Libor is known, see the derivation in "Solution to exercise 2" in
 * com.andreamazzon.exercise2. This last computation is of course specific to any particular derivative, and it is therefore
 * implemented in the derived classes: for this reason, here it is defined as an abstract method.
 *
 * @author Andrea Mazzon
 *
 */
public abstract class EuropeanOptionPossiblyInArrears {

	//all these parameters are common to all the interest rates derivatives we consider
	private final double firstTime, secondTime;
	private final double firstBond, secondBond;
	private double libor;
	private final double liborVolatility;

	private final double notional;

	public EuropeanOptionPossiblyInArrears(double firstTime, double secondTime, double firstBond, double secondBond, double liborVolatility,
			double notional) {
		this.firstTime = firstTime;
		this.secondTime = secondTime;
		this.firstBond = firstBond;
		this.secondBond = secondBond;
		this.liborVolatility = liborVolatility;
		this.notional = notional;
		computeLibor();//in this way we know that libor is initialized as soon as an object is created
	}

	private void computeLibor(){
		final double timeInterval = getTimeInterval();
		libor = 1/timeInterval * (firstBond/secondBond - 1);
	}

	/*
	 * The fields should be private, but we might need their value in the derived class: we then need
	 * some getters
	 */
	public double getFirstTime() {
		return firstTime;
	}

	public double getSecondTime() {
		return secondTime;
	}

	public double getFirstBond() {
		return firstBond;
	}

	public double getSecondBond() {
		return secondBond;
	}

	public double getLiborVolatility() {
		return liborVolatility;
	}

	public double getNotional() {
		return notional;
	}

	public double getTimeInterval() {
		return secondTime - firstTime;
	}

	public double getInitialValueLibor() {
		return libor;
	}

	/**
	 * It computes and returns the classical price of the contract (i.e., if the payment date is T_2) as a function
	 * of the initial value of the Libor
	 * @param initialValue, initial value of the Libor
	 * @return the classical price of the contract (i.e., if the payment date is T_2) as a function of the initial value
	 * of the Libor.
	 */
	public abstract double getValueInClassicUnits(double initialValueLibor);

	/**
	 * It computes and returns the convexity adjustment using the derivation you can see in "Solution to exercise 2.pdf",
	 * in com.andreamazzon.exercise7. Here the point is that the formula is identical for all the contracts, once you know
	 * the classical price as a function of the initial value of the Libor.
	 * @return the convexity adjustment
	 */
	public double computeConvexityAdjustment() {
		final double valueInUnitsOfM = getValueInClassicUnits(libor * Math.exp(liborVolatility*liborVolatility*firstTime));
		return getTimeInterval() * libor * valueInUnitsOfM;
	}

	/**
	 * It computes and returns the price in arrears of the contract (i.e., if the payment date is T_1) using the derivation
	 * you can see in "Solution to exercise 2.pdf", in com.andreamazzon.exercise7. Here the point is that the formula is
	 * identical for all the contracts, once you know the classical price as a function of the initial value of the Libor.
	 * @return the price in arrears of the contract (i.e., if the payment date is T_1)
	 */
	public double getValueInArrears() {
		return getValueInClassicUnits(libor) + computeConvexityAdjustment();
	}
}