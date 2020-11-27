package com.andreamazzon.exercise3;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.andreamazzon.exercise2.ConvertableToFloatingPoint;
import com.andreamazzon.exercise2.EnhancedValue;
import com.andreamazzon.exercise2.EnhancedValueDifferentiable;
import com.andreamazzon.exercise2.EnhancedValueDoubleDifferentiable;

/**
 * This class has a method that tests the implementation in the class EnhancedValueDoubleDifferentiable by computing
 * the derivative of the function x -> nx, for a given integer n, constructed with a for loop: nx = x + x + x + ... +x,
 * n times. You note here the importance of adding arguments.get(0),0.0) (or arguments.get(1),0.0) ) when computing
 * the derivatives in the method propagateDerivativeToArguments.
 *
 * @author Andrea Mazzon
 *
 */
public class EnhancedValueTestWithLoop {

	//first node of the tree
	final EnhancedValueDifferentiable x =  new EnhancedValueDoubleDifferentiable(1.0);
	final double tolerance = 10E-15;//this is the machine precision error

	//we need it no get the (double) value associated with the object representing the derivative
	private static double valueOf(EnhancedValue x) {
		return ((ConvertableToFloatingPoint)x).asFloatingPoint();
	}

	@Test
	public void testFunction() {
		final int n = 100;

		//at the beginning, the node has just a  value, 1. The we add the value of x at every step
		EnhancedValueDifferentiable result =  new EnhancedValueDoubleDifferentiable(0.0);
		//a way to write the function nx with a for loop
		for (int i = 1; i<=n;i++) {
			// note: when constructing the tree, these will be DIFFERENT nodes, DIFEFRENT objects
			result = (EnhancedValueDifferentiable) result.add(x);
		}

		final EnhancedValueDifferentiable xDerivative = (EnhancedValueDifferentiable) result.getDerivativeWithRespectTo(x);

		final double xDerivativeExact = n;
		System.out.println("Testing derivative of nx as sum:");
		System.out.println("x derivative   : " + xDerivative + ",  expected   : " + xDerivativeExact);
		Assert.assertEquals(valueOf(xDerivative), xDerivativeExact, tolerance);
	}

}
