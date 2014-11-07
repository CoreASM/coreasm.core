package org.coreasm.compiler.plugins.predicatelogic;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.predicatelogic.PredicateLogicPlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;

public class CompilerPredicateLogicPlugin implements CompilerOperatorPlugin,
		CompilerCodeRPlugin {

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
	public CodeFragment rCode(ASTNode n) throws CompilerException {
		if (n.getGrammarRule().equals("")) {

		} else if (n.getGrammarRule().equals("ExistsExp")
				&& n.getGrammarClass().equals("Expression")) {
			CodeFragment result = new CodeFragment("");

			CodeFragment name = CoreASMCompiler.getEngine().compile(
					n.getAbstractChildNodes().get(0), CodeType.L);
			result.appendFragment(name);
			result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
			result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");

			CodeFragment source = CoreASMCompiler.getEngine().compile(
					n.getAbstractChildNodes().get(1), CodeType.R);
			CodeFragment guard = CoreASMCompiler.getEngine().compile(
					n.getAbstractChildNodes().get(2), CodeType.R);

			result.appendFragment(source);
			result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>(((CompilerRuntime.Enumerable)evalStack.pop()).enumerate());\n");
			result.appendLine("for(@decl(int,i)=0;@i@<=@list@.size();@i@++){\n");
			result.appendLine("if(@i@ == @list@.size()){\n");
			result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.FALSE);\n");
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("localStack.pushLayer();\n");
			result.appendLine("localStack.put(@nameloc@.name, @list@.get(@i@));\n");
			result.appendFragment(guard);
			result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
			result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.TRUE);\n");
			result.appendLine("break;\n");
			result.appendLine("}\n");
			result.appendLine("localStack.popLayer();\n");
			result.appendLine("}\n");
			result.appendLine("}\n");

			return result;

		} else if (n.getGrammarRule().equals("ForallExp")
				&& n.getGrammarClass().equals("Expression")) {
			CodeFragment result = new CodeFragment("");

			CodeFragment loc = CoreASMCompiler.getEngine().compile(
					n.getAbstractChildNodes().get(0), CodeType.L);

			CodeFragment source = CoreASMCompiler.getEngine().compile(
					n.getAbstractChildNodes().get(1), CodeType.R);
			CodeFragment guard = CoreASMCompiler.getEngine().compile(
					n.getAbstractChildNodes().get(2), CodeType.R);

			result.appendFragment(loc);
			result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
			result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");

			result.appendFragment(source);
			result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>(((CompilerRuntime.Enumerable)evalStack.pop()).enumerate());\n");
			result.appendLine("for(@decl(int,i)=0;@i@<=@list@.size();@i@++){\n");
			result.appendLine("if(@i@ == @list@.size()){\n");
			result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.TRUE);\n");
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("localStack.pushLayer();\n");
			result.appendLine("localStack.put(@nameloc@.name, @list@.get(@i@));\n");
			result.appendFragment(guard);
			result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.FALSE)){\n");
			result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.FALSE);\n");
			result.appendLine("break;\n");
			result.appendLine("}\n");
			result.appendLine("localStack.popLayer();\n");
			result.appendLine("}\n");
			result.appendLine("}\n");

			return result;
		}

		throw new CompilerException(
				"unhandled code type: (PredicateLogicPlugin, rCode, "
						+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
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
}
