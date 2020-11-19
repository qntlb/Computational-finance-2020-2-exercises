package com.andreamazzon.exercise2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class performs algorithmic differentiation. It is based on the class ValueDoubleDifferentiable you find
 * in computational-finance-algorithmicdifferentiation, with the addition of the two operations exp and
 * addProduct.
 * It represents a node in the tree standing for the concatenation of some algebraic operations.
 * It implements the interfaces EnhancedValueDifferentiable (which extends EnhancedValue itself)
 * so that it has to implement the methods standing for the operations, and ConvertableToFloatingPoint,
 * so that an object of such a class must be able to return a Double, standing for the value associated to the
 * node represented by the object.
 * An object of this class is identified by the state of four fields:
 * - an Operator, an enum type which has eight possible values, i.e., the possible operators considered
 * - a Double value, representing the real number associated to the node
 * - a List<EnhancedValueDoubleDifferentiable> arguments, representing the arguments of the operation (for example,
 * x in the case when the node represents the operation a -> a + x
 * - a Long id, that is, the identifier of the node. This defines the order with which the operations are performed
 * in the concatenation
 *
 * @author Andrea Mazzon, based on Christian Fries Value interface
 *
 */
public class EnhancedValueDoubleDifferentiable implements EnhancedValueDifferentiable, ConvertableToFloatingPoint {

	//note that we added EXP and ADDPRODUCT with respect to the operations allowed in ValueDoubleDifferentiable
	private enum Operator {
		SQUARED, SQRT, ADD, SUB, MULT, DIV, EXP, ADDPRODUCT
	}

	/*
	 * important: the fact that it's static allows the identifier of the nodes to be incremented by 1 every time an
	 * operation is called, i.e., every time a new node is created
	 */
	private static AtomicLong nextId = new AtomicLong();

	private final Double value;
	private final Operator operator;
	private final List<EnhancedValueDoubleDifferentiable> arguments;
	private final Long id;

	/*
	 * It creates a node representing a single operation. Note that it is private! Only called from internal methods, when an operation
	 * is performed. The only public constructor, see below, has only Double value as an argument. In this case, the arguments are:
	 * - Double value, i.e., the number associated to the node created
	 * - operator Operator that lead to this value
	 * - arguments Arguments that were used in this operation.
	 */
	private EnhancedValueDoubleDifferentiable(Double value, Operator operator, List<EnhancedValueDoubleDifferentiable> arguments) {
		super();//good practice in general, even if here it does nothing: this class has no parent class, but maybe we decide to create one
		this.value = value;
		this.operator = operator;
		this.arguments = arguments;
		this.id = nextId.getAndIncrement();//here is the trick with the id!
		//		System.out.println("id = " + this.id);
		//		System.out.println("value = " + this.value);
		//		if(this.operator != null) {
		//			System.out.println("operator = " + this.operator.toString());
		//		}
		//		if(this.arguments != null) {
		//			System.out.println(this.arguments.toString());
		//		}
		//		System.out.println();
	}

	/**
	 * Creates a node from a constant - a leaf node.
	 *
	 * @param value Value of this node.
	 */
	public EnhancedValueDoubleDifferentiable(Double value) {
		this(value, null, null);
	}

	/*
	 * we want to be able to get the value, the operator, the arguments and the id of our node: this is done in the four methods
	 * below. However, asFloatingPoint() is the only one which is supposed to be public
	 */
	@Override
	public Double asFloatingPoint() {
		return value;
	}

	//these three methods are only supposed to be used internally (unless maybe to check how things work)
	private Operator getOperator() {
		return operator;
	}

	private List<EnhancedValueDoubleDifferentiable> getArguments() {
		return arguments;
	}

	private Long getID() {
		return id.longValue();
	}

	/*
	 * we also want to be able to get the value of another node, i.e, of another object type EnhancedValue.
	 * NOTE: this method DOES NOT depend on the object calling it. It is static. Look at how it is used in methods
	 * representing operations with another node
	 */
	private static double valueOf(EnhancedValue x) {
		//we have to cast x, because a priori an object of type EnhancedValue does not implement ConvertableToFloatingPoint
		return ((ConvertableToFloatingPoint)x).asFloatingPoint();
	}

	//The operations, implementing the interface.

	@Override
	public EnhancedValue squared() {
		return new EnhancedValueDoubleDifferentiable(value * value, Operator.SQUARED, List.of(this));
	}

	@Override
	public EnhancedValue sqrt() {
		return new EnhancedValueDoubleDifferentiable(Math.sqrt(value), Operator.SQRT, List.of(this));
	}

	@Override
	public EnhancedValue exp() {
		return new EnhancedValueDoubleDifferentiable(Math.exp(value), Operator.EXP, List.of(this));
	}

	@Override
	public EnhancedValue add(EnhancedValue x) {
		return new EnhancedValueDoubleDifferentiable(value + valueOf(x), Operator.ADD, List.of(this, (EnhancedValueDoubleDifferentiable)x));
	}

	@Override
	public EnhancedValue sub(EnhancedValue x) {
		return new EnhancedValueDoubleDifferentiable(value - valueOf(x), Operator.SUB, List.of(this, (EnhancedValueDoubleDifferentiable)x));
	}

	@Override
	public EnhancedValue mult(EnhancedValue x) {
		return new EnhancedValueDoubleDifferentiable(value * valueOf(x), Operator.MULT, List.of(this, (EnhancedValueDoubleDifferentiable)x));
	}

	@Override
	public EnhancedValue div(EnhancedValue x) {
		return new EnhancedValueDoubleDifferentiable(value / valueOf(x), Operator.DIV, List.of(this, (EnhancedValueDoubleDifferentiable)x));
	}

	@Override
	public EnhancedValue addProduct(EnhancedValue x, EnhancedValue y) {
		return new EnhancedValueDoubleDifferentiable(value + valueOf(x)*valueOf(y), Operator.ADDPRODUCT, List.of(this, (EnhancedValueDoubleDifferentiable)x,(EnhancedValueDoubleDifferentiable)y));
	}

	@Override
	public String toString() {
		return value.toString();
	}

	//FROM HERE ON: EVERYTHING ABOUT THE COMPUTATION OF THE DERIVIVATIVES

	@Override
	public EnhancedValue getDerivativeWithRespectTo(EnhancedValueDifferentiable x) {
		//it returns the entry with key x of the gradient
		return new EnhancedValueDoubleDifferentiable(getGradient().getOrDefault(x, 0.0));
	}

	/**
	 * Get the derivatives of a node with respect to all the nodes via a backward algorithmic differentiation (adjoint differentiation).
	 * Note that this gives the derivatives with respect to all the nodes with respect to all the nodes on which the object calling
	 * the method depends directly or indirectly.
	 *
	 * @return A map x -> D which gives D = dy/dx, where y is this node and x is any input node.
	 */
	public Map<EnhancedValueDoubleDifferentiable, Double> getGradient() {

		/*
		 *  The map that will contain the derivatives. As you see, the keys are the nodes, and the "values" on which the nodes
		 *  are mapped are the values of the derivative with respect to the nodes
		 */
		final Map<EnhancedValueDoubleDifferentiable, Double> derivativesWithRespectTo = new HashMap<>();

		// the first one is the derivative of the node calling the method with respect to itself: equal to 1
		derivativesWithRespectTo.put(this, 1.0);

		// This creates an ordered set of objects, sorted ascending by their getID() value (first = highest ID)
		final TreeSet<EnhancedValueDoubleDifferentiable> nodesToProcess = new TreeSet<>((o1,o2) -> o1.getID().compareTo(o2.getID()));

		// Add the root note
		nodesToProcess.add(this);

		// Walk down the tree, always removing the node with the highest id and adding their arguments
		while(!nodesToProcess.isEmpty()) {

			// Get and remove the top most node.
			final EnhancedValueDoubleDifferentiable currentNode = nodesToProcess.pollLast();

			final List<EnhancedValueDoubleDifferentiable> currentNodeArguments = currentNode.getArguments();
			if(currentNodeArguments != null) {
				/*
				 *  we create now one or more new entries of derivativesWithRespectTo, whose keys are all the
				 *  currentNodeArguments (the number of course depends on the operation) and whose values are
				 *  the derivatives of the object calling the method with respect to the arguments
				 */
				propagateDerivativeToArguments(derivativesWithRespectTo, currentNode, currentNodeArguments);

				// Add all arguments to our queue of nodes we have to work on
				nodesToProcess.addAll(currentNode.getArguments());
			}
		}
		return derivativesWithRespectTo;
	}

	/**
	 * Apply the update rule Di = Di + Dm * dxm / dxi (where Dm = dy/xm).
	 *
	 * @param derivativesWithRespectTo The map that contains the derivatives x -> dy/dx and will be updated, that is Di = dy/dx_{i}.
	 * @param node The current node (xm).
	 * @param arguments The (list of) arguments of the current node (the i's).
	 */
	private void propagateDerivativeToArguments(Map<EnhancedValueDoubleDifferentiable, Double> derivativesWithRespectTo, EnhancedValueDoubleDifferentiable node, List<EnhancedValueDoubleDifferentiable> arguments) {

		switch(node.getOperator()) {
		case ADD:
			derivativesWithRespectTo.put(arguments.get(0), derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0) + derivativesWithRespectTo.get(node) * 1.0);
			derivativesWithRespectTo.put(arguments.get(1), derivativesWithRespectTo.getOrDefault(arguments.get(1),0.0) + derivativesWithRespectTo.get(node) * 1.0);
			break;
		case SUB:
			derivativesWithRespectTo.put(arguments.get(0), derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0) + derivativesWithRespectTo.get(node) * 1.0);
			derivativesWithRespectTo.put(arguments.get(1), derivativesWithRespectTo.getOrDefault(arguments.get(1),0.0) - derivativesWithRespectTo.get(node) * 1.0);
			break;
		case MULT:
			derivativesWithRespectTo.put(arguments.get(0), derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0) + derivativesWithRespectTo.get(node) * arguments.get(1).asFloatingPoint());
			derivativesWithRespectTo.put(arguments.get(1), derivativesWithRespectTo.getOrDefault(arguments.get(1),0.0) + derivativesWithRespectTo.get(node) * arguments.get(0).asFloatingPoint());
			break;
		case DIV:
			final double x = arguments.get(0).asFloatingPoint();
			final double y = arguments.get(1).asFloatingPoint();
			final double derivativeOfCurrentNode = derivativesWithRespectTo.get(node);
			double derivativeOfFirstArgumentNode = derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0);
			double derivativeOfSecondArgumentNode = derivativesWithRespectTo.getOrDefault(arguments.get(1),0.0);

			// Update and store the derivative with respect to the first argument
			derivativeOfFirstArgumentNode = derivativeOfFirstArgumentNode + derivativeOfCurrentNode * 1/y;//update
			derivativesWithRespectTo.put(arguments.get(0), derivativeOfFirstArgumentNode);//store

			// Update and store the derivative with respect to the second argument
			derivativeOfSecondArgumentNode = derivativeOfSecondArgumentNode - derivativeOfCurrentNode * x/(y*y);//update
			derivativesWithRespectTo.put(arguments.get(1), derivativeOfSecondArgumentNode);//store
			break;
		case ADDPRODUCT:
			final double firstNumberOfTheProduct = arguments.get(1).asFloatingPoint();
			final double secondNumberOfTheProduct = arguments.get(2).asFloatingPoint();
			final double derivativeOTheCurrentNode = derivativesWithRespectTo.get(node);

			// Update
			derivativesWithRespectTo.put(arguments.get(0), derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0) + derivativeOTheCurrentNode);
			derivativesWithRespectTo.put(arguments.get(1), derivativesWithRespectTo.getOrDefault(arguments.get(1),0.0) + derivativeOTheCurrentNode * secondNumberOfTheProduct);
			derivativesWithRespectTo.put(arguments.get(2), derivativesWithRespectTo.getOrDefault(arguments.get(2),0.0) + derivativeOTheCurrentNode * firstNumberOfTheProduct);
			break;
		case SQUARED:
			derivativesWithRespectTo.put(arguments.get(0), derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0) + derivativesWithRespectTo.get(node) * 2 * arguments.get(0).asFloatingPoint());
			break;
		case SQRT:
			derivativesWithRespectTo.put(arguments.get(0), derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0) + derivativesWithRespectTo.get(node) / 2 / Math.sqrt(arguments.get(0).asFloatingPoint()));
			break;
			/*
			 * I want to add a new entry of the map derivativesWithRespectTo whose key is the argument node and whose value is the derivative of
			 * node with respect to the argument
			 */
		case EXP:
			derivativesWithRespectTo.put(arguments.get(0), //key: the argument
					derivativesWithRespectTo.getOrDefault(arguments.get(0),0.0) //derivative with the respect to the current argument
					+ derivativesWithRespectTo.get(node)/*the derivative already "contained" in the node*/ * Math.exp(arguments.get(0).asFloatingPoint()));
			break;
		}
	}
}
