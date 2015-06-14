/*	
 * MathFunction.java 	1.0 	$Revision: 116 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2010-02-08 23:48:50 +0100 (Mo, 08 Feb 2010) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.number.NumberElement;

/** 
 * This class implements a series of Math functions that will be 
 * provided to the CoreASM engine through {@link MathPlugin}.
 *   
 * @author Roozbeh Farahbod
 * @version 1.0, $Revision: 116 $, Last modified: $Date: 2010-02-08 23:48:50 +0100 (Mo, 08 Feb 2010) $
 */
public abstract class MathFunction extends FunctionElement {

	/**
	 * Creates a new Derived function.
	 */
	public MathFunction() {
		this.setFClass(this.getMathFunctionClass());
	}
	
	/*
	 * Create new instances of various math functions and returns 
	 * a map of function name to its instance. 
	 */
	protected static Map<String, FunctionElement> createFunctions(ControlAPI capi) {
		Map<String, FunctionElement> result = new HashMap<String, FunctionElement>();
		
		// One can get the number background in order to create
		// numbers. That would make it too complicated, so I skipped
		// this knowing that it would be equivalent to creating a new
		// NumberElement instance. -- Roozbeh
		
		// Math.PI
		result.put("MathPI", new MathFunction() {

			@Override
			protected FunctionClass getMathFunctionClass() {
				return FunctionClass.fcStatic;
			}

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(0, args))
					return NumberElement.getInstance(Math.PI);
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.E
		result.put("MathE", new MathFunction() {

			@Override
			protected FunctionClass getMathFunctionClass() {
				return FunctionClass.fcStatic;
			}

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(0, args))
					return NumberElement.getInstance(Math.E);
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.abs(x)
		result.put("abs", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.abs(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.acos(x)
		result.put("acos", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.acos(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.asin(x)
		result.put("asin", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.asin(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.atan(x)
		result.put("atan", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.atan(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.atan2(x, y)
		result.put("atan2", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(2, args))
					return NumberElement.getInstance(Math.atan2(ithValue(args, 0), ithValue(args, 1)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.cbrt(x)
		result.put("cuberoot", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.cbrt(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.cbrt(x)
		result.put("cbrt", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.cbrt(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.ceil(x)
		result.put("ceil", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.ceil(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.cos(x)
		result.put("cos", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.cos(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.cosh(x)
		result.put("cosh", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.cosh(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.exp(x)
		result.put("exp", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.exp(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.expm1(x)
		result.put("expm1", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.expm1(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.floor(x)
		result.put("floor", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.floor(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.hypot(x, y)
		// Returns sqrt(x^2 +y^2) without intermediate overflow or underflow.
		result.put("hypot", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(2, args))
					return NumberElement.getInstance(Math.hypot(ithValue(args, 0), ithValue(args, 1)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.IEEEremainder(x, y)
		// Computes the remainder operation on two arguments 
		// as prescribed by the IEEE 754 standard.
		result.put("IEEEremainder", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(2, args))
					return NumberElement.getInstance(Math.IEEEremainder(ithValue(args, 0), ithValue(args, 1)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.log(x)
		result.put("log", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.log(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.log10(x)
		result.put("log10", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.log10(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});
		
		// Math.log1p(x)
		result.put("log1p", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.log1p(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		/*
		 * - max(x, y)
		 *   returns the maximum of x and y
		 *   
		 * - max(enumerable)
		 *   This function returns the maximum number in a collection of numbers.
		 *   If there is one non-number in the collection, it returns undef.
		 */
		result.put("max", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				// Math.max(x,y)
				if (checkNumberArguments(2, args))
					return NumberElement.getInstance(Math.max(ithValue(args, 0), ithValue(args, 1)));
				else {
					// max({x1,..,xn})
					Element arg = args.get(0);
					if (args.size() == 1 && arg != null && arg instanceof Enumerable ) {
						double max = Double.MIN_VALUE;
						for (Element e: ((Enumerable)arg).enumerate())
							if (e instanceof NumberElement) {
								double evalue = ((NumberElement)e).getNumber();
								if (evalue > max)
									max = evalue;
							} else
								return Element.UNDEF;
						return NumberElement.getInstance(max);
					} else
						return Element.UNDEF;
					
				}
			}
			
		});
		
		/*
		 * - min(x, y)
		 *   returns the minimum of x and y
		 *   
		 * - min(enumerable)
		 *   This function returns the minimum numeric value in a collection of numbers.
		 *   If there is one non-number in the collection, it returns undef.
		 */
		result.put("min", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(2, args))
					return NumberElement.getInstance(Math.min(ithValue(args, 0), ithValue(args, 1)));
				else {
					// min({x1,..,xn})
					Element arg = args.get(0);
					if (args.size() == 1 && arg != null && arg instanceof Enumerable ) {
						double min = Double.MAX_VALUE;
						for (Element e: ((Enumerable)arg).enumerate()) {
							if (e instanceof NumberElement) {
								double evalue = ((NumberElement)e).getNumber();
								if (evalue < min)
									min = evalue;
							} else
								return Element.UNDEF;
						}
						return NumberElement.getInstance(min);
					} else
						return Element.UNDEF;
					
				}
			}
			
		});
		
		// Math.pow(x, y)
		result.put("pow", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(2, args))
					return NumberElement.getInstance(Math.pow(ithValue(args, 0), ithValue(args, 1)));
				else
					return Element.UNDEF;
			}
			
		});
		
		
		// powerset(s)
		result.put("powerset", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (args.size() == 1 && args.get(0) instanceof Enumerable)
					return new PowerSetElement((Enumerable)args.get(0));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.random()
		result.put("random", new MathFunction() {

			@Override
			protected FunctionClass getMathFunctionClass() {
				return FunctionClass.fcMonitored;
			}

			@Override
			public Element calcFunction(List<? extends Element> args) {
				// one optional argument is accepted as an "id" for
				// the random value
				if (args.size() == 0 || args.size() == 1)
					return NumberElement.getInstance(Math.random());
				else
					return Element.UNDEF;
			}
			
		});

		// Math.round(x)
		result.put("round", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.round(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.signum(x)
		result.put("signum", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.signum(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.sin(x)
		result.put("sin", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.sin(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.sinh(x)
		result.put("sinh", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.sinh(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.sqrt(x)
		result.put("sqrt", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.sqrt(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.tan(x)
		result.put("tan", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.tan(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.tanh(x)
		result.put("tanh", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.tanh(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.toDegrees(x)
		// Converts an angle measured in radians to 
		// an approximately equivalent angle measured in degrees.
		result.put("toDegrees", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.toDegrees(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		// Math.toRadians(x)
		// Converts an angle measured in degrees to an 
		// approximately equivalent angle measured in radians.
		result.put("toRadians", new MathFunction() {

			@Override
			public Element calcFunction(List<? extends Element> args) {
				if (checkNumberArguments(1, args))
					return NumberElement.getInstance(Math.toRadians(ithValue(args, 0)));
				else
					return Element.UNDEF;
			}
			
		});

		/*
		 * This function returns the sum of a collection of numbers.
		 * If there is one non-number in the collection, it returns undef.
		 */
		result.put("sum", new MathFunction() {

			@Override
			protected Element calcFunction(List<? extends Element> args) {
				if (args.size() > 2)
					return Element.UNDEF;
				
				Element arg1 = null;
				Element arg2 = null;
				Enumerable enumerable = null;
				FunctionElement f = null;
				try { 
					arg1 = args.get(0);
					arg2 = args.get(1);
				} catch (Exception e) {}
				
				
				if (arg1 != null && arg1 instanceof Enumerable) {
					enumerable = (Enumerable) arg1;
					if (arg2 != null && arg2 instanceof FunctionElement)
						f  = (FunctionElement) arg2;
				} 
				
				if (enumerable != null) {
					double sum = 0;
					Element tempE = null;
			
					// if there is also a function provided
					if (f != null) {
						for (Element e: enumerable.enumerate()) {
							tempE = f.getValue(new ElementList(e));
							if (tempE != null && tempE instanceof NumberElement) {
								sum += ((NumberElement)tempE).getNumber();
								continue;
							}
							return Element.UNDEF;
						}
					} else {
						for (Element e: enumerable.enumerate()) {
							if (e instanceof NumberElement)  
								sum += ((NumberElement)e).getNumber();
							else
								return Element.UNDEF;
						}
					}
					return NumberElement.getInstance(sum);
				} else
					return Element.UNDEF;
			}
			
		});
				
		return result;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		Element result = calcFunction(args);
		
		if (result instanceof NumberElement) {
			double number = ((NumberElement)result).getNumber();
			if (number == Double.NaN)
				return Element.UNDEF;
		}
		
		return result;
	}
	
	/*
	 * This is the core of the function computation.
	 */
	protected abstract Element calcFunction(List<? extends Element> args);

	/*
	 * checks the validity of the arguments.
	 */
	protected boolean checkNumberArguments(int count, List<? extends Element> args) {
		boolean result = true;
		
		if (args.size() != count)
			result = false;
		else 
			for (int i=0; i < count; i++)
				if ( ! (args.get(i) instanceof NumberElement)) {
					result = false;
					break;
				}
		
		return result;
	}
	
	/*
	 * handy method to get the double value of the ith argument
	 */
	protected double ithValue(List<? extends Element> args, int i) {
		return ((NumberElement)args.get(i)).getNumber();
	}
	
	/*
	 * a placeholder for function class
	 */
	protected FunctionClass getMathFunctionClass() {
		return FunctionClass.fcDerived;
	}
}
