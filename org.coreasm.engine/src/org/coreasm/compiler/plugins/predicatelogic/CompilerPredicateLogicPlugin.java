package org.coreasm.compiler.plugins.predicatelogic;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.predicatelogic.PredicateLogicPlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.plugins.predicatelogic.code.rcode.ExistsExpHandler;
import org.coreasm.compiler.plugins.predicatelogic.code.rcode.ForallExpHandler;

/**
 * Provides additional operations on booleans
 * @author Spellmaker
 *
 */
public class CompilerPredicateLogicPlugin extends CompilerCodePlugin implements CompilerOperatorPlugin {

	private Plugin interpreterPlugin;
	
	/**
	 * Constructs a new plugin
	 * @param parent The interpreter version
	 */
	public CompilerPredicateLogicPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
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
		result.add("notmemberof");
		return result;
	}

	@Override
	public String compileBinaryOperator(String token) throws CompilerException {
		String result = "";

		if (token.equals("or")) {
			result = result
					+ "if((@lhs@ instanceof @RuntimePkg@.BooleanElement) && (@rhs@ instanceof @RuntimePkg@.BooleanElement)){\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,l)=(@RuntimePkg@.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,r)=(@RuntimePkg@.BooleanElement)@rhs@;\n";
			result = result
					+ "evalStack.push(@RuntimePkg@.BooleanElement.valueOf(@l@.equals(@RuntimePkg@.BooleanElement.TRUE) || @r@.equals(@RuntimePkg@.BooleanElement.TRUE)));\n";
		} else if (token.equals("xor")) {
			result = result
					+ "if((@lhs@ instanceof @RuntimePkg@.BooleanElement) && (@rhs@ instanceof @RuntimePkg@.BooleanElement)){\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,l)=(@RuntimePkg@.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,r)=(@RuntimePkg@.BooleanElement)@rhs@;\n";

			result = result
					+ "evalStack.push(@RuntimePkg@.BooleanElement.valueOf((@l@.getValue() && !@r@.getValue()) || (!@l@.getValue() && @r@.getValue())));\n";
		} else if (token.equals("and")) {
			result = result
					+ "if((@lhs@ instanceof @RuntimePkg@.BooleanElement) && (@rhs@ instanceof @RuntimePkg@.BooleanElement)){\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,l)=(@RuntimePkg@.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,r)=(@RuntimePkg@.BooleanElement)@rhs@;\n";
			result = result
					+ "evalStack.push(@RuntimePkg@.BooleanElement.valueOf(@l@.equals(@RuntimePkg@.BooleanElement.TRUE) && @r@.equals(@RuntimePkg@.BooleanElement.TRUE)));\n";
		} else if (token.equals("implies")) {
			result = result
					+ "if((@lhs@ instanceof @RuntimePkg@.BooleanElement) && (@rhs@ instanceof @RuntimePkg@.BooleanElement)){\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,l)=(@RuntimePkg@.BooleanElement)@lhs@;\n";
			result = result
					+ "@decl(@RuntimePkg@.BooleanElement,r)=(@RuntimePkg@.BooleanElement)@rhs@;\n";
			result = result
					+ "evalStack.push(@RuntimePkg@.BooleanElement.valueOf(!@l@.equals(@RuntimePkg@.BooleanElement.TRUE) || @r@.equals(@RuntimePkg@.BooleanElement.TRUE)));\n";
		} else if (token.equals("!=")) {
			result += "if(true){\n";
			result += "evalStack.push(@RuntimePkg@.BooleanElement.valueOf(!@lhs@.equals(@rhs@)));\n";
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
					+ "if((@lhs@ instanceof @RuntimePkg@.Element) && (@rhs@ instanceof @RuntimePkg@.Enumerable)){\n";
			result += ("if(!(@rhs@ instanceof @RuntimePkg@.Enumerable)){\n");
			result += ("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result += ("}\n");
			result += ("else if(@lhs@.equals(@RuntimePkg@.Element.UNDEF)){\n");
			result += ("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result += ("}\n");
			result += ("else{\n");
			result += ("@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>();\n");
			result += ("@list@.addAll(((@RuntimePkg@.Enumerable)@rhs@).enumerate());\n");
			result += ("for(@decl(int,i)=0;@i@<=@list@.size();@i@++){\n");
			result += ("if(@i@ == @list@.size()){\n");
			result += ("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
			result += ("}\n");
			result += ("else if(@lhs@.equals(@list@.get(@i@))){\n");
			result += ("evalStack.push(@RuntimePkg@.BooleanElement.TRUE);\n");
			result += ("break;\n");
			result += ("}\n");
			result += ("}\n");
			result += ("}\n");
		}  else if (token.equals("notmemberof")) {
			result = result
					+ "if((@lhs@ instanceof @RuntimePkg@.Element) && (@rhs@ instanceof @RuntimePkg@.Enumerable)){\n";
			result += ("if(!(@rhs@ instanceof @RuntimePkg@.Enumerable)){\n");
			result += ("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result += ("}\n");
			result += ("else if(@lhs@.equals(@RuntimePkg@.Element.UNDEF)){\n");
			result += ("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result += ("}\n");
			result += ("else{\n");
			result += ("@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>();\n");
			result += ("@list@.addAll(((@RuntimePkg@.Enumerable)@rhs@).enumerate());\n");
			result += ("for(@decl(int,i)=0;@i@<=@list@.size();@i@++){\n");
			result += ("if(@i@ == @list@.size()){\n");
			result += ("evalStack.push(@RuntimePkg@.BooleanElement.TRUE);\n");
			result += ("}\n");
			result += ("else if(@lhs@.equals(@list@.get(@i@))){\n");
			result += ("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
			result += ("break;\n");
			result += ("}\n");
			result += ("}\n");
			result += ("}\n");
		}
		else
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
			result += "if(@lhs@ instanceof @RuntimePkg@.BooleanElement){\n";
			result += "evalStack.push(@RuntimePkg@.BooleanElement.valueOf(@lhs@.equals(@RuntimePkg@.BooleanElement.FALSE)));\n";
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
