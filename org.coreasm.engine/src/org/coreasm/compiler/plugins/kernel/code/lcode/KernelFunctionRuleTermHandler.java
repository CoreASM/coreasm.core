package org.coreasm.compiler.plugins.kernel.code.lcode;

import java.util.List;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;

/**
 * Handles Location-code for f(t1, t2...tn)
 * @author Spellmaker
 *
 */
public class KernelFunctionRuleTermHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine) throws CompilerException{
		FunctionRuleTermNode frtn = (FunctionRuleTermNode) node;

		//TODO: location might also be the name of a ruleparam, which could break stuff
		
		String name = frtn.getName();
		if (frtn.hasArguments()) {
			// if the function is not a constant, the arguments
			// need to be evaluated first
			List<ASTNode> args = frtn.getArguments();
			CodeFragment[] argcode = new CodeFragment[args.size()];

			for (int i = 0; i < args.size(); i++) {
				argcode[i] = engine.compile(args.get(i), CodeType.R);
			}
			// each code will leave its value on the eval stack
			for (int i = args.size() - 1; i >= 0; i--) {
				result.appendFragment(argcode[i]);
			}

			result.appendLine("\n"
					+ "@decl(java.util.ArrayList<@RuntimePkg@.Element>,arglist);\n@arglist@ = new java.util.ArrayList<>();\n");
			result.appendLine("for(@decl(int,__i)=0;@__i@<"
					+ args.size()
					+ ";@__i@++)\n@arglist@.add((@RuntimePkg@.Element)evalStack.pop());\n");
			
			//build the location, find out, if there is a local value for this location
			result.appendLine("@decl(Object,o)=localStack.get(\"" + name + "\");\n");
			result.appendLine("if(@o@ instanceof @RuntimePkg@.FunctionElement){\n");
			result.appendLine("evalStack.push(new @RuntimePkg@.Location(@RuntimeProvider@.getStorage().getFunctionName((@RuntimePkg@.FunctionElement)@o@), @arglist@));\n");			
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("evalStack.push(new @RuntimePkg@.Location(\"" + name + "\", @arglist@));\n");
			result.appendLine("}\n");
			
			//result.appendLine("evalStack.push(new CompilerRuntime.Location(\""
			//		+ name + "\", @arglist@));");
		} else {
			//String code = "evalStack.push(new CompilerRuntime.Location(\""
			//		+ name
			//		+ "\", new java.util.ArrayList<CompilerRuntime.Element>()));";
			//result.appendLine(code);
			
			result.appendLine("@decl(Object,o)=localStack.get(\"" + name + "\");\n");
			result.appendLine("if(@o@ instanceof @RuntimePkg@.FunctionElement){\n");
			result.appendLine("evalStack.push(new @RuntimePkg@.Location(@RuntimeProvider@.getStorage().getFunctionName((@RuntimePkg@.FunctionElement)@o@), new java.util.ArrayList<@RuntimePkg@.Element>()));\n");			
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("evalStack.push(new @RuntimePkg@.Location(\"" + name + "\", new java.util.ArrayList<@RuntimePkg@.Element>()));\n");
			result.appendLine("}\n");
		}
	}

}
