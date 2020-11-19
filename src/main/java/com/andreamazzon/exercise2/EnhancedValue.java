package com.andreamazzon.exercise2;

/**
 * Public interface with methods to perform algebraic operations (here there is still no connection
 * to derivatives)
 *
 * @author Andrea Mazzon, based on Christian Fries ConvertableToFloatingPoint interface
 */

public interface EnhancedValue {

	/**
	 * Applies x*x to the real number x associated to the EnhancedValue object that calls the method.
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue squared();

	/**
	 * Applies sqrt(x) to the real number x associated to the EnhancedValue object that calls the method.
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue sqrt();

	/**
	 * Applies a+x to the real number a associated to the EnhancedValue object that calls the method, where
	 * x is the real number associated to the EnhancedValue object given as an argument
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue add(EnhancedValue x);

	/**
	 * Applies a-x to the real number a associated to the EnhancedValue object that calls the method, where
	 * x is the real number associated to the EnhancedValue object given as an argument
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue sub(EnhancedValue x);

	/**
	 * Applies a*x to the real number a associated to the EnhancedValue object that calls the method, where
	 * x is the real number associated to the EnhancedValue object given as an argument
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue mult(EnhancedValue x);

	/**
	 * Applies a/x to the real number a associated to the EnhancedValue object that calls the method, where
	 * x is the real number associated to the EnhancedValue object given as an argument
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue div(EnhancedValue x);

	/**
	 * Applies exp(x) to the real number x associated to the EnhancedValue object that calls the method.
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue exp();

	/**
	 * Applies a+xy to the real number a associated to the EnhancedValue object that calls the method, where
	 * x and y are the real numbers associated to the two EnhancedValue objects given as an argument
	 *
	 * @return New EnhancedValue object representing the result.
	 */
	EnhancedValue addProduct(EnhancedValue x, EnhancedValue y);
}