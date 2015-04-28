package org.coreasm.compiler.plugins.turboasm.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;

/**
 * Handles the local rule
 * @author Spellmaker
 *
 */
public class LocalRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		
		LocalRuleNode local = (LocalRuleNode) node;	
		
		CodeFragment rule = engine.compile(local.getRuleNode(), CodeType.U);
		result.appendLine("@decl(java.util.ArrayList,locs)= new java.util.ArrayList();\n");
		for(String s : local.getFunctionNames()){
			result.appendLine("@locs@.add(\"" + s + "\");\n");
		}
		result.appendFragment(rule);
		result.appendLine("@decl(@RuntimePkg@.UpdateList,ulist)=(@RuntimePkg@.UpdateList)evalStack.pop();\n");
		result.appendLine("@decl(@RuntimePkg@.UpdateList,result)=new @RuntimePkg@.UpdateList();\n");
		result.appendLine("for(@decl(@RuntimePkg@.Update,u): @ulist@){\n");
		result.appendLine("if(!@locs@.contains(@u@.loc.name)){\n");
		result.appendLine("@result@.add(@u@);\n");
		result.appendLine("}\n");
		result.appendLine("}\n");
		result.appendLine("evalStack.push(@result@);\n");
	}

}
