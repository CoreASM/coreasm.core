package org.coreasm.compiler.plugins.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;

/**
 * Generates math functions for the math plugin
 * @author Markus Brenner
 *
 */
public class MathPluginHelper {
	/**
	 * Create the function entries
	 * @return A map containing the generated functions with the corresponding names
	 */
	public static Map<String, MathFunctionEntry> createFunctions(CompilerEngine engine){
		Map<String, MathFunctionEntry> result = new HashMap<String, MathFunctionEntry>();
		
		result.put("MathPI", new MathFunctionEntry("MathPI", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			protected FunctionClass getMathFunctionClass() {" + 
				 "				return FunctionClass.fcStatic;" + 
				 "			}" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(0, args))" + 
				 "					return NumberElement.getInstance(Math.PI);" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("MathE", new MathFunctionEntry("MathE", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			protected FunctionClass getMathFunctionClass() {" + 
				 "				return FunctionClass.fcStatic;" + 
				 "			}" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(0, args))" + 
				 "					return NumberElement.getInstance(Math.E);" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("abs", new MathFunctionEntry("abs", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.abs(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("acos", new MathFunctionEntry("acos", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.acos(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("asin", new MathFunctionEntry("asin", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.asin(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("atan", new MathFunctionEntry("atan", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.atan(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("atan2", new MathFunctionEntry("atan2", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(2, args))" + 
				 "					return NumberElement.getInstance(Math.atan2(ithValue(args, 0), ithValue(args, 1), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("cuberoot", new MathFunctionEntry("cuberoot", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.cbrt(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("cbrt", new MathFunctionEntry("cbrt", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.cbrt(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("ceil", new MathFunctionEntry("ceil", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.ceil(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("cos", new MathFunctionEntry("cos", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.cos(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("cosh", new MathFunctionEntry("cosh", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.cosh(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("exp", new MathFunctionEntry("exp", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.exp(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("expm1", new MathFunctionEntry("expm1", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.expm1(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("floor", new MathFunctionEntry("floor", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.floor(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("hypot", new MathFunctionEntry("hypot", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(2, args))" + 
				 "					return NumberElement.getInstance(Math.hypot(ithValue(args, 0), ithValue(args, 1), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("IEEEremainder", new MathFunctionEntry("IEEEremainder", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(2, args))" + 
				 "					return NumberElement.getInstance(Math.IEEEremainder(ithValue(args, 0), ithValue(args, 1), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("log", new MathFunctionEntry("log", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.log(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("log10", new MathFunctionEntry("log10", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.log10(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("log1p", new MathFunctionEntry("log1p", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.log1p(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("max", new MathFunctionEntry("max", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(2, args))" + 
				 "					return NumberElement.getInstance(Math.max(ithValue(args, 0), ithValue(args, 1), engine));" + 
				 "				else {" + 
				 "					Element arg = args.get(0);" + 
				 "					if (args.size() == 1 && arg != null && arg instanceof Enumerable ) {" + 
				 "						double max = Double.MIN_VALUE;" + 
				 "						for (Element e: ((Enumerable)arg).enumerate())" + 
				 "							if (e instanceof NumberElement) {" + 
				 "								double evalue = ((NumberElement)e).getNumber();" + 
				 "								if (evalue > max)" + 
				 "									max = evalue;" + 
				 "							} else" + 
				 "								return Element.UNDEF;" + 
				 "						return NumberElement.getInstance(max);" + 
				 "					} else" + 
				 "						return Element.UNDEF;" + 
				 "					" + 
				 "				}" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("min", new MathFunctionEntry("min", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(2, args))" + 
				 "					return NumberElement.getInstance(Math.min(ithValue(args, 0), ithValue(args, 1), engine));" + 
				 "				else {" + 
				 "					Element arg = args.get(0);" + 
				 "					if (args.size() == 1 && arg != null && arg instanceof Enumerable ) {" + 
				 "						double min = Double.MAX_VALUE;" + 
				 "						for (Element e: ((Enumerable)arg).enumerate()) {" + 
				 "							if (e instanceof NumberElement) {" + 
				 "								double evalue = ((NumberElement)e).getNumber();" + 
				 "								if (evalue < min)" + 
				 "									min = evalue;" + 
				 "							} else" + 
				 "								return Element.UNDEF;" + 
				 "						}" + 
				 "						return NumberElement.getInstance(min);" + 
				 "					} else" + 
				 "						return Element.UNDEF;" + 
				 "					" + 
				 "				}" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("pow", new MathFunctionEntry("pow", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(2, args))" + 
				 "					return NumberElement.getInstance(Math.pow(ithValue(args, 0), ithValue(args, 1), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("powerset", new MathFunctionEntry("powerset", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (args.size() == 1 && args.get(0) instanceof Enumerable)" + 
				 "					return new PowerSetElement((Enumerable)args.get(0));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("random", new MathFunctionEntry("random", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			protected FunctionClass getMathFunctionClass() {" + 
				 "				return FunctionClass.fcMonitored;" + 
				 "			}" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (args.size() == 0 || args.size() == 1)" + 
				 "					return NumberElement.getInstance(Math.random());" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("round", new MathFunctionEntry("round", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.round(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("signum", new MathFunctionEntry("signum", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.signum(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("sin", new MathFunctionEntry("sin", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.sin(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("sinh", new MathFunctionEntry("sinh", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.sinh(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("sqrt", new MathFunctionEntry("sqrt", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.sqrt(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("tan", new MathFunctionEntry("tan", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.tan(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("tanh", new MathFunctionEntry("tanh", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.tanh(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("toDegrees", new MathFunctionEntry("toDegrees", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.toDegrees(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("toRadians", new MathFunctionEntry("toRadians", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			public Element calcFunction(List<? extends Element> args) {" + 
				 "				if (checkNumberArguments(1, args))" + 
				 "					return NumberElement.getInstance(Math.toRadians(ithValue(args, 0), engine));" + 
				 "				else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));

				result.put("sum", new MathFunctionEntry("sum", new CodeFragment("" + 
				 "" + 
				 "			" + 
				 "			protected Element calcFunction(List<? extends Element> args) {" + 
				 "				if (args.size() > 2)" + 
				 "					return Element.UNDEF;" + 
				 "				" + 
				 "				Element arg1 = null;" + 
				 "				Element arg2 = null;" + 
				 "				Enumerable enumerable = null;" + 
				 "				FunctionElement f = null;" + 
				 "				try { " + 
				 "					arg1 = args.get(0);" + 
				 "					arg2 = args.get(1);" + 
				 "				} catch (Exception e) {}" + 
				 "				" + 
				 "				" + 
				 "				if (arg1 != null && arg1 instanceof Enumerable) {" + 
				 "					enumerable = (Enumerable) arg1;" + 
				 "					if (arg2 != null && arg2 instanceof FunctionElement)" + 
				 "						f  = (FunctionElement) arg2;" + 
				 "				} " + 
				 "				" + 
				 "				if (enumerable != null) {" + 
				 "					double sum = 0;" + 
				 "					Element tempE = null;" + 
				 "			" + 
				 "					if (f != null) {" + 
				 "						for (Element e: enumerable.enumerate()) {" + 
				 "							if (e instanceof NumberElement) { " + 
				 "								tempE = f.getValue(new ElementList(e));" + 
				 "								if (tempE != null && tempE instanceof NumberElement) {" + 
				 "									sum += ((NumberElement)tempE).getNumber();" + 
				 "									continue;" + 
				 "								}" + 
				 "							}" + 
				 "							return Element.UNDEF;" + 
				 "						}" + 
				 "					} else {" + 
				 "						for (Element e: enumerable.enumerate()) {" + 
				 "							if (e instanceof NumberElement)  " + 
				 "								sum += ((NumberElement)e).getNumber();" + 
				 "							else" + 
				 "								return Element.UNDEF;" + 
				 "						}" + 
				 "					}" + 
				 "					return NumberElement.getInstance(sum);" + 
				 "				} else" + 
				 "					return Element.UNDEF;" + 
				 "			}" + 
				 "			" + 
				 "		"), engine));



	
		
		return result;
	}
	
	/**
	 * Provides the names of the generated functions
	 * @return The name of all generated functions
	 */
	public static List<String> getNames(){
		List<String> result = new ArrayList<String>();
		result.add("MathPI");
		result.add("MathE");
		result.add("abs");
		result.add("acos");
		result.add("asin");
		result.add("atan");
		result.add("atan2");
		result.add("cuberoot");
		result.add("cbrt");
		result.add("ceil");
		result.add("cos");
		result.add("cosh");
		result.add("exp");
		result.add("expm1");
		result.add("floor");
		result.add("hypot");
		result.add("IEEEremainder");
		result.add("log");
		result.add("log10");
		result.add("log1p");
		result.add("max");
		result.add("min");
		result.add("pow");
		result.add("powerset");
		result.add("random");
		result.add("round");
		result.add("signum");
		result.add("sin");
		result.add("sinh");
		result.add("sqrt");
		result.add("tan");
		result.add("tanh");
		result.add("toDegrees");
		result.add("toRadians");
		result.add("sum");
		return result;
	}
}
