package org.coreasm.compiler.plugins.kernel.code.rcode;

import java.util.List;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;

/**
 * Handles r-code for f(t1, t2...tn)
 * @author Spellmaker
 *
 */
public class KernelFunctionRuleExpressionHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		FunctionRuleTermNode frtn = (FunctionRuleTermNode) node;
		String name = frtn.getName();

		if (frtn.getArguments().size() > 0) {
			//if the term has arguments
			List<ASTNode> args = frtn.getArguments();
			CodeFragment[] argcode = new CodeFragment[args.size()];
			for (int i = 0; i < args.size(); i++) {
				argcode[i] = engine.compile(args.get(i),
						CodeType.R);
			}

			for (int i = args.size() - 1; i >= 0; i--) {
				result.appendFragment(argcode[i]);
			}
			result.appendLine("\n"
					+ "@decl(java.util.ArrayList<@RuntimePkg@.Element>,arglist);\n@arglist@ = new java.util.ArrayList<>();\n");
			result.appendLine("for(@decl(int,__i)=0;@__i@<"
					+ args.size()
					+ ";@__i@++)\n@arglist@.add((@RuntimePkg@.Element)evalStack.pop());\n");
			
			//get the location, if the name is the name of a ruleparam
			result.appendLine("@decl(Object, rparam) = ruleparams.get(\"" + name + "\");\n");
			result.appendLine("if(@rparam@ != null){\n");
			result.appendLine("@decl(@RuntimePkg@.Location,loc)=new @RuntimePkg@.Location(((@RuntimePkg@.RuleParam)@rparam@).evaluateL(localStack).name, @arglist@);");
			result.appendLine("evalStack.push(@RuntimeProvider@.getStorage().getValue(@loc@));\n");
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("evalStack.push(@RuntimeProvider@.getStorage().getValue(new @RuntimePkg@.Location(\""
					+ name + "\", @arglist@)));");
			result.appendLine("}\n");
		} else {
			// look in all different locations
			// TODO: integrate undef location handlers
			result.appendLine("@decl(Object, res) = ruleparams.get(\"" + name + "\");\n");
			result.appendLine("if(@res@ != null){\n");
			result.appendLine("evalStack.push(((@RuntimePkg@.RuleParam) @res@).evaluateR(localStack));\n");
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("@res@ = localStack.get(\"" + name + "\");\n");
			result.appendLine("if(@res@ == null){\n");
			result.appendLine("@decl(@RuntimePkg@.Location,loc) = new @RuntimePkg@.Location(\"" + name + "\", new java.util.ArrayList<@RuntimePkg@.Element>());\n");
			result.appendLine("@res@ = @RuntimeProvider@.getStorage().getValue(@loc@);\n");
			result.appendLine("}\n");
			result.appendLine("if(@res@.equals(@RuntimePkg@.Element.UNDEF)){\n");
			result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF); //this should actually be the undef handler\n");
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("evalStack.push(@res@);\n");
			result.appendLine("}\n");
			result.appendLine("}\n");
		}
	}
}
