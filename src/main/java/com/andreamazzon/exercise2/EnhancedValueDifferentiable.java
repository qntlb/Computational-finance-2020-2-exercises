package com.andreamazzon.exercise2;

/**
 * Public interface that inherits from EnhancedValue the methods performing algebraic operations, and has one more
 * method in order to get the derivative of the concatenation of these operations
 *
 * @author Andrea Mazzon, based on Christian Fries ValueDifferentiable interface
 *
 */
public interface EnhancedValueDifferentiable extends EnhancedValue {

	/**
	 * Returns the EnhancedValue objects that represents the value of the derivative of the operation associated to
	 * the EnhacedValue object calling the method, with respect to the EnhancedValue object x representing the argument.
	 *
	 * @return New object representing the result.
	 */
	EnhancedValue getDerivativeWithRespectTo(EnhancedValueDifferentiable x);

}