package com.andreamazzon.exercise2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * This class tests the computation of the derivatives of some functions involving the exponential and the function
 * "add product".
 *
 * @author Andrea Mazzon
 *
 */
class EnhancedValueDoubleDifferentiableTest {


	private static double valueOf(EnhancedValue x) {
		return ((ConvertableToFloatingPoint)x).asFloatingPoint();
	}

	/**
	 * It tests the partial derivatives of the function (a,b)->exp(a^2+ab^2)
	 */
	//	@Test
	//	void testAddProductWithExp() {
	//
	//		/*
	//		 * here we have two objects of type EnhancedValueDifferentiable, instantiations of EnhancedValueDoubleDifferentiable,
	//		 * that only represent a number: no operations. This is the only way an user can create such an object using the
	//		 * constructor
	//		 */
	//		final EnhancedValueDifferentiable a = new EnhancedValueDoubleDifferentiable(2.0);//it will have id = 0
	//		final EnhancedValueDifferentiable b = new EnhancedValueDoubleDifferentiable(3.0);//it will have id = 1. See constructor
	//		//we test the operation (a,b)->exp(a^2+ab^2)
	//		final EnhancedValueDifferentiable result = (EnhancedValueDifferentiable)  a.squared()//id = 2
	//				.addProduct(b.squared()/*id = 3*/,a)/*id = 4*/
	//				.exp();//id = 5
	//
	//
	//
	//		final EnhancedValueDifferentiable derivativeAlgorithmicWithRespectToA = (EnhancedValueDifferentiable) result.getDerivativeWithRespectTo(a);
	//		System.out.println("Algorithmic derivative with respect to a = " + derivativeAlgorithmicWithRespectToA);
	//
	//		//(2a+b^2)exp(a^2+ab^2)
	//		final EnhancedValueDifferentiable derivativeAnalyticWithRespectToA = (EnhancedValueDifferentiable) a.add(a).add(b.squared()).mult(result);
	//		System.out.println("Analytic derivative with respect to a = " + derivativeAnalyticWithRespectToA);
	//
	//		assertEquals(valueOf(derivativeAnalyticWithRespectToA), valueOf(derivativeAlgorithmicWithRespectToA), 1E-15);
	//
	//		final EnhancedValueDifferentiable derivativeAlgorithmicWithRespectToB = (EnhancedValueDoubleDifferentiable) result.getDerivativeWithRespectTo(b);
	//		System.out.println("Algorithmic derivative with respect to b = " + derivativeAlgorithmicWithRespectToB);
	//
	//		//2abe^(a^2+ab^2)
	//		final EnhancedValueDifferentiable derivativeAnalyticWithRespectToB = (EnhancedValueDoubleDifferentiable) a.mult(b.add(b)).mult(result);
	//		System.out.println("Analytic derivative with respect to b = " + derivativeAnalyticWithRespectToB);
	//
	//		assertEquals(valueOf(derivativeAnalyticWithRespectToB), valueOf(derivativeAlgorithmicWithRespectToB), 1E-15, "partial derivative dz/dx");
	//	}

	/**
	 * It tests the derivative of the function x -> 35 x, written as x -> 7 x + 7 x 4. You note here the importance of adding
	 * arguments.get(0),0.0) (or arguments.get(1),0.0) ) when computing the derivatives in the method propagateDerivativeToArguments.
	 */
	@Test
	void testAddProduct() {
		//here we define our function directly by the Java Function interface
		final Function<EnhancedValueDifferentiable, EnhancedValueDifferentiable> function = x -> {

			final EnhancedValueDifferentiable x1 = (EnhancedValueDifferentiable)
					x.mult(new EnhancedValueDoubleDifferentiable(7.0)/*id=1*/);//id=2

			final EnhancedValueDifferentiable x2 = new EnhancedValueDoubleDifferentiable(4.0);//id=3

			final EnhancedValueDifferentiable x3 = (EnhancedValueDifferentiable) x1.addProduct(x1,x2);//id = 4

			return x3;
		};

		final EnhancedValueDifferentiable x = new EnhancedValueDoubleDifferentiable(1.0);//id = 0
		final EnhancedValueDifferentiable y = function.apply(x);

		final Double derivativeAnalytic = 35.0;
		final EnhancedValue derivativeAlgorithmic = y.getDerivativeWithRespectTo(x);
		assertEquals(derivativeAnalytic, valueOf(derivativeAlgorithmic), 1E-15, "partial derivative dy/dx");
		System.out.println("Algorithmic derivative = " + valueOf(derivativeAlgorithmic));
	}


	//	/**
	//	 * This test checks the derivative of the dummy division x/x;
	//	 */
	//	@Test
	//	void testDivSecondArg() {
	//		final EnhancedValueDifferentiable x1 = new EnhancedValueDoubleDifferentiable(2.0);
	//		final EnhancedValueDifferentiable y = (EnhancedValueDifferentiable) x1.div(x1);
	//		final EnhancedValueDifferentiable derivativeAlgorithmic = (EnhancedValueDifferentiable) y.getDerivativeWithRespectTo(x1);
	//		final double derivativeAnalytic = 0.0;
	//		assertEquals(derivativeAnalytic, valueOf(derivativeAlgorithmic), 1E-15, "partial derivative dy/dx");
	//		System.out.println("derivative of the function x/x = " + valueOf(derivativeAlgorithmic));
	//	}
}
