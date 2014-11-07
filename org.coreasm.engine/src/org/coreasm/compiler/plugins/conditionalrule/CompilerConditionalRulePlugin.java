package org.coreasm.compiler.plugins.conditionalrule;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.conditionalrule.ConditionalRulePlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerConditionalRulePlugin implements CompilerCodeUPlugin, CompilerPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerConditionalRulePlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}
	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException{
		if(n.getGrammarRule().equals("ConditionalRule") && n.getGrammarClass().equals("Rule")){
			ASTNode cond = n.getAbstractChildNodes().get(0);
			ASTNode ifpart = n.getAbstractChildNodes().get(1);
			ASTNode elsepart = null;
			if(n.getAbstractChildNodes().size() == 3){
				elsepart = n.getAbstractChildNodes().get(2);
			}

			try{
				CodeFragment result = CoreASMCompiler.getEngine().compile(cond, CodeType.R);
				result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
				result.appendFragment(CoreASMCompiler.getEngine().compile(ifpart, CodeType.U));
				result.appendLine("}\n");
				if(elsepart != null){
					result.appendLine("else{\n");
					result.appendFragment(CoreASMCompiler.getEngine().compile(elsepart, CodeType.U));
					result.appendLine("}\n");
				}
				else{
					result.appendLine("else{\n");
					result.appendLine("evalStack.push(new CompilerRuntime.UpdateList());\n");
					result.appendLine("}\n");
				}
				return result;				
			}
			catch(Exception e){
				System.out.println("stack trace");
				e.printStackTrace();
				System.out.println("eofst");
				throw new CompilerException(e);
			}
		}

		throw new CompilerException("unhandled code type: (ConditionalRulePlugin, lCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public String getName() {
		return ConditionalRulePlugin.PLUGIN_NAME;
	}
}
