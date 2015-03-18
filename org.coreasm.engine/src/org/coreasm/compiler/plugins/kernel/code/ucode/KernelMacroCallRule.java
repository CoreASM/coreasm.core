package org.coreasm.compiler.plugins.kernel.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.Preprocessor;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.kernel.MacroCallRuleNode;

/**
 * Handles rule calls.
 * Lots of potential for digression from interpreter behaviour.
 * Changes in this handler might lead to lots of changes in other code.
 * When changing stuff here, keep an eye on the TurboASM Plugin, it also
 * sports some rule calls
 * @author Spellmaker
 *
 */
public class KernelMacroCallRule implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		// TODO parameter might also be a rule call - how to fix this?
		MacroCallRuleNode mcrn = (MacroCallRuleNode) node;
		String name = mcrn.getFunctionRuleElement().getFirst()
				.getToken();

		result.appendLine("@decl(java.util.ArrayList<@RuntimePkg@.RuleParam>,arglist)=new java.util.ArrayList<>();");

		FunctionRuleTermNode params = (FunctionRuleTermNode) mcrn
				.getFunctionRuleElement();

		if (params.hasArguments()) {
			CodeFragment[] paramCode = new CodeFragment[params
					.getArguments().size()];

			for (int i = 0; i < paramCode.length; i++) {
				// create the code for the parameter
				CodeFragment tmp = engine.compile(
						params.getArguments().get(i), CodeType.R);
				// create the param object and push it onto the stack
				result.appendLine("\n@arglist@.add(new @RuntimePkg@.RuleParam(){\n");
				result.appendLine("public @RuntimePkg@.Rule getUpdateResponsible(){\nreturn null;\n}\n");
				result.appendLine("java.util.Map<String, @RuntimePkg@.RuleParam> ruleparams;\n");
				result.appendLine("public void setParams(java.util.Map<String, @RuntimePkg@.RuleParam> params){\n");
				result.appendLine("this.ruleparams = params;\n");
				result.appendLine("}\n");
				
				//a ruleparam can be evaluated as l-context or r-context, but the l-context is not always possible.
				//try compiling the param as an l-code, but be prepared for failure
				result.appendLine("public @RuntimePkg@.Location evaluateL(@RuntimePkg@.LocalStack localStack) throws Exception{\n");
				try{
					CodeFragment ltmp = engine.tryCompile(params.getArguments().get(i), CodeType.L);
					result.appendFragment(ltmp);
					result.appendLine("return (@RuntimePkg@.Location) evalStack.pop();\n");
				}
				catch(Exception e){
					result.appendLine("throw new Exception(\"This ruleparam cannot be evaluated as a location\");\n");					
				}
				result.appendLine("}\n");
				
				result.appendLine("public @RuntimePkg@.Element evaluateR(@RuntimePkg@.LocalStack localStack) throws Exception{\n");
				result.appendFragment(tmp);
				result.appendLine("\nreturn (@RuntimePkg@.Element)evalStack.pop();\n}\n});\n");
				result.appendLine("@arglist@.get(@arglist@.size() - 1).setParams(ruleparams);\n");
			}
		}
		// cf.appendLine("\n@decl(CompilerRuntime.Rule,macrorule)=new Rules."
		// + name + "(@arglist, localStack);");
		
		Preprocessor prep = engine.getPreprocessor();
		Information inf = prep.getGeneralInfo().get("RuleDeclaration");
		
		if (inf.getChildren().contains(name)) {
			// check parameter count
			if (inf.getInformation(name).getChildren().size() != params
					.getArguments().size()) {
				engine.addError("wrong number of parameters in rulecall to rule " + name);
				throw new CompilerException(
						"wrong number of parameters for Rulecall to Rule "
								+ name);
			}

			// if name is a valid rulename, so call it by creating a new
			// rule instance
			result.appendLine("@decl(@RuntimePkg@.Rule, callruletmp)=new @RulePkg@."
					+ name + "();\n");
			result.appendLine("@callruletmp@.initRule(@arglist@, localStack);\n");
			result.appendLine("@decl(@RuntimePkg@.UpdateList, ulist)=new @RuntimePkg@.UpdateList();\n");
			//cf.appendLine("@callruletmp@.setAgent(this.getAgent());\n");
			result.appendLine("@ulist@.addAll(@callruletmp@.call().updates);\n");
			result.appendLine("evalStack.push(@ulist@);\n");
		} else {
			result.appendFragment(engine.compile(mcrn.getFunctionRuleElement(), CodeType.R));
			result.appendLine("@decl(Object,o)=evalStack.pop();");
			result.appendLine("if(@o@ instanceof @RuntimePkg@.Rule){\n");
			result.appendLine("}\n");
			result.appendLine("else throw new Exception(\"not a rule\");\n");
			result.appendLine("@decl(@RuntimePkg@.Rule,ruleinstance) = ((@RuntimePkg@.Rule)@o@).getCopy();\n");
			result.appendLine("@ruleinstance@.initRule(@arglist@, localStack);\n");
			result.appendLine("@decl(@RuntimePkg@.UpdateList, nulist)=new @RuntimePkg@.UpdateList();\n");
			result.appendLine("@nulist@.addAll(@ruleinstance@.call().updates);\n");
			result.appendLine("evalStack.push(@nulist@);\n");
			//throw new UnsupportedOperationException("currently not supported");
			/*// otherwise, name is a parameter on the local stack
			cf.appendLine("@decl(CompilerRuntime.Rule,callruletmp);\n");
			cf.appendLine("@callruletmp@ = ((CompilerRuntime.Rule)((CompilerRuntime.RuleParam) localStack.get(\""
					+ name + "\")).evaluate(localStack)).getCopy();\n");
			cf.appendLine("@callruletmp@.initRule(@arglist@, localStack);\n");
			cf.appendLine("@decl(CompilerRuntime.UpdateList, ulist)=new CompilerRuntime.UpdateList();\n");
			cf.appendLine("@ulist@.addAll(@callruletmp@.call().updates);\n");
			cf.appendLine("evalStack.push(@ulist@);\n");*/
		}
	}
}
