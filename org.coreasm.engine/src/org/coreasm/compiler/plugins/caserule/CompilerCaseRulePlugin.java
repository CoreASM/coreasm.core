package org.coreasm.compiler.plugins.caserule;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.caserule.CaseRulePlugin;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerCaseRulePlugin implements CompilerCodeUPlugin, CompilerPlugin{
	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException{
		if ((n.getGrammarClass().equals("Rule")) && (n.getGrammarRule().equals("CaseRule"))) {
			try{
				CodeFragment result = new CodeFragment("");
				
				CodeFragment guardcode = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.R);
				CodeFragment[] conditions = new CodeFragment[(n.getAbstractChildNodes().size() - 1) / 2];
				CodeFragment[] rules = new CodeFragment[(n.getAbstractChildNodes().size() - 1) / 2];
				
				for(int i = 1; i < n.getAbstractChildNodes().size(); i += 2){
					conditions[(i - 1) / 2] = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(i), CodeType.R);
					rules[(i - 1) / 2] = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(i + 1), CodeType.U);
				}
				
				result.appendFragment(guardcode);
				result.appendLine("@decl(CompilerRuntime.Element,guard)=(CompilerRuntime.Element)evalStack.pop();\n");
				result.appendLine("@decl(int,exec)=0;\n");
				
				for(int i = 0; i < conditions.length; i++){
					result.appendFragment(conditions[i]);
					result.appendLine("if(@guard@.equals(evalStack.pop())){\n");
					result.appendLine("@exec@++;\n");
					result.appendFragment(rules[i]);
					result.appendLine("}\n");
				}
				result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
				result.appendLine("for(@decl(int,i)=0;@i@<@exec@;@i@++){\n");
				result.appendLine("@ulist@.addAll((CompilerRuntime.UpdateList)evalStack.pop());\n");
				result.appendLine("}\n");
				result.appendLine("evalStack.push(@ulist@);\n");
				
				return result;
				
			} catch (Exception e) {
				throw new CompilerException("invalid code generated");
			}
		}

		throw new CompilerException("unhandled code type: (CaseRulePlugin, lCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public String getName() {
		return CaseRulePlugin.PLUGIN_NAME;
	}
}
