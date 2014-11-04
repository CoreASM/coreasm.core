package org.coreasm.compiler.plugins.letrule;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.letrule.LetRulePlugin;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerLetRulePlugin implements CompilerCodeUPlugin, CompilerPlugin{
	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException{
		if(n.getGrammarClass().equals("Rule") && n.getGrammarRule().equals("LetRule")){
			try {

				CodeFragment result = new CodeFragment("");
				CodeFragment name = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.L);
				result.appendFragment(name);
				result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");
				
				CodeFragment val = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(1), CodeType.R);
				CodeFragment upd = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(2), CodeType.U);
				
				result.appendFragment(val);
				result.appendLine("localStack.pushLayer();\nlocalStack.put(@nameloc@.name, evalStack.pop());\n");
				result.appendFragment(upd);
				result.appendLine("localStack.popLayer();\n");
				
				return result;
			} catch (Exception e) {
				throw new CompilerException(e);
			}
			
		}
		
		
		throw new CompilerException("unhandled code type: (LetRulePlugin, lCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
	
	@Override
	public String getName() {
		return LetRulePlugin.PLUGIN_NAME;
	}
}
