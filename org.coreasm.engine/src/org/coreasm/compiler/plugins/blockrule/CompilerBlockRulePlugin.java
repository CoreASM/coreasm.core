package org.coreasm.compiler.plugins.blockrule;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.blockrule.BlockRulePlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerBlockRulePlugin implements CompilerCodeUPlugin, CompilerPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerBlockRulePlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}
	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException{
		if ((n.getGrammarRule() != null) && (n.getGrammarRule().equals("BlockRule"))) {
			CodeFragment result;
			result = new CodeFragment("");
		
			if(n.getAbstractChildNodes().size() <= 0) throw new CompilerException("empty BlockRule");
			
			for(int i = 0; i < n.getAbstractChildNodes().size(); i++){
				result.appendFragment(CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(i), CodeType.U));
			}
			
			result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
			for(int i = 0; i < n.getAbstractChildNodes().size(); i++){
				result.appendLine("@ulist@.addAll((CompilerRuntime.UpdateList)evalStack.pop());\n");
			}
			
			result.appendLine("evalStack.push(@ulist@);\n");
			return result;
		}

		throw new CompilerException("unhandled code type: (BlockRulePlugin, lCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public String getName() {
		return BlockRulePlugin.PLUGIN_NAME;
	}
}
