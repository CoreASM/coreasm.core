package org.coreasm.compiler.plugins.predicatelogic;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.predicatelogic.PredicateLogicPlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.plugins.predicatelogic.code.rcode.ExistsExpHandler;
import org.coreasm.compiler.plugins.predicatelogic.code.rcode.ForallExpHandler;

public class CompilerPredicateLogicPlugin extends CompilerCodePlugin implements CompilerOperatorPlugin {

	private Plugin interpreterPlugin;
	
	public CompilerPredicateLogicPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public String getName() {
		return PredicateLogicPlugin.PLUGIN_NAME;
	}

	@Override
	public List<String> unaryOperations() {
		List<String> result = new ArrayList<String>();
		result.add("not");
		return result;
	}

	@Override
	public List<String> binaryOperations() {
		List<String> result = new ArrayList<String>();
		result.add("or");
		result.add("xor");
		result.add("and");
		result.add("implies");
		result.add("!=");
		result.add("memberof");
		return result;
	}

	@Override
	public String compileBinaryOperator(String token) throws CompilerException {
		String result = "";

		if (token.equals("or")) {
			result = result
					+ "if((@lhs@ instanceof CompilerRuntime.BooleanElement) && (@rhs@ instanceof CompilerRuntime.BooleanElement)){\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,l)=(CompilerRuntime.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,r)=(CompilerRuntime.BooleanElement)@rhs@;\n";
			result = result
					+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@l@.equals(CompilerRuntime.BooleanElement.TRUE) || @r@.equals(CompilerRuntime.BooleanElement.TRUE)));\n";
		} else if (token.equals("xor")) {
			result = result
					+ "if((@lhs@ instanceof CompilerRuntime.BooleanElement) && (@rhs@ instanceof CompilerRuntime.BooleanElement)){\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,l)=(CompilerRuntime.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,r)=(CompilerRuntime.BooleanElement)@rhs@;\n";

			result = result
					+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf((@l@.getValue() && !@r@.getValue()) || (!@l@.getValue() && @r@.getValue())));\n";
		} else if (token.equals("and")) {
			result = result
					+ "if((@lhs@ instanceof CompilerRuntime.BooleanElement) && (@rhs@ instanceof CompilerRuntime.BooleanElement)){\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,l)=(CompilerRuntime.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,r)=(CompilerRuntime.BooleanElement)@rhs@;\n";
			result = result
					+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@l@.equals(CompilerRuntime.BooleanElement.TRUE) && @r@.equals(CompilerRuntime.BooleanElement.TRUE)));\n";
		} else if (token.equals("implies")) {
			result = result
					+ "if((@lhs@ instanceof CompilerRuntime.BooleanElement) && (@rhs@ instanceof CompilerRuntime.BooleanElement)){\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,l)=(CompilerRuntime.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(CompilerRuntime.BooleanElement,r)=(CompilerRuntime.BooleanElement)@rhs@;\n";
			result = result
					+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(!@l@.equals(CompilerRuntime.BooleanElement.TRUE) || @r@.equals(CompilerRuntime.BooleanElement.TRUE)));\n";
		} else if (token.equals("!=")) {
			result += "if(true){\n";
			result += "evalStack.push(CompilerRuntime.BooleanElement.valueOf(!@lhs@.equals(@rhs@)));\n";
			// result = result +
			// "if((@lhs@ instanceof CompilerRuntime.BooleanElement) && (@rhs@ instanceof CompilerRuntime.BooleanElement)){\n";
			// result = result +
			// "@decl(CompilerRuntime.BooleanElement,l)=(CompilerRuntime.BooleanElement)@lhs@;\n";
			// result = result +
			// "@decl(CompilerRuntime.BooleanElement,r)=(CompilerRuntime.BooleanElement)@rhs@;\n";
			// result = result +
			// "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@l@.equals(CompilerRuntime.BooleanElement.TRUE) != @r@.equals(CompilerRuntime.BooleanElement.TRUE)));\n";
		} else if (token.equals("memberof")) {
			result = result
					+ "if((@lhs@ instanceof CompilerRuntime.Element) && (@rhs@ instanceof CompilerRuntime.Enumerable)){\n";
			result += ("if(!(@rhs@ instanceof CompilerRuntime.Enumerable)){\n");
			result += ("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
			result += ("}\n");
			result += ("else if(@lhs@.equals(CompilerRuntime.Element.UNDEF)){\n");
			result += ("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
			result += ("}\n");
			result += ("else{\n");
			result += ("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
			result += ("@list@.addAll(((CompilerRuntime.Enumerable)@rhs@).enumerate());\n");
			result += ("for(@decl(int,i)=0;@i@<=@list@.size();@i@++){\n");
			result += ("if(@i@ == @list@.size()){\n");
			result += ("evalStack.push(CompilerRuntime.BooleanElement.FALSE);\n");
			result += ("}\n");
			result += ("else if(@lhs@.equals(@list@.get(@i@))){\n");
			result += ("evalStack.push(CompilerRuntime.BooleanElement.TRUE);\n");
			result += ("break;\n");
			result += ("}\n");
			result += ("}\n");
			result += ("}\n");
		} else
			throw new CompilerException(
					"unkown operator: PredicateLogicPlugin, " + token);

		result = result + "}\n";

		result = result + " else ";

		return result;
	}

	@Override
	public String compileUnaryOperator(String token) throws CompilerException {
		String result = "";

		if (token.equals("not")) {
			result += "if(@lhs@ instanceof CompilerRuntime.BooleanElement){\n";
			result += "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@lhs@.equals(CompilerRuntime.BooleanElement.FALSE)));\n";
			result += "}\n";
		} else
			throw new CompilerException(
					"unkown operator: PredicateLogicPlugin, " + token);

		result = result + " else ";

		return result;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new ExistsExpHandler(), CodeType.R, "Expression", "ExistsExp", null);
		register(new ForallExpHandler(), CodeType.R, "Expression", "ForallExp", null);
	}
}
