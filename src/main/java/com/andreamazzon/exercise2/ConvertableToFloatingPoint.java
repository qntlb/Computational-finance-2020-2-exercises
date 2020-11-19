package com.andreamazzon.exercise2;

/**
 * Public interface having a method in order to get the real (that is, floating point for us) number
 * associated to the object calling the method. In our application, the object calling the method
 * will be an object of the class EnhancedValueDoubleDifferentiable, that implements this interface
 * (well, of course) AND EnhancedValueDifferentiable. So the real number returned by the method of this
 * interface will represent the value of the operation (or of a derivative)
 *
 * @author Andrea Mazzon, based on Christian Fries ConvertableToFloatingPoint interface
 *
 */
public interface ConvertableToFloatingPoint {

	/**
	 * Returns the floating point value of the object calling the method, as a Double
	 *
	 * @return Floating point value associated to the object calling the method
	 */
	Double asFloatingPoint();

}
