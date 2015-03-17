package org.coreasm.compiler.plugins.letrule.code.ucode;

import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.letrule.LetRuleNode;

/**
 * Handles the let rule
 * @author Spellmaker
 *
 */
public class LetRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		System.out.println("called, father is " + node.getParent());
		try {
			LetRuleNode letrule = (LetRuleNode) node;
			Map<String, ASTNode> letmap = letrule.getVariableMap();
			
			result.appendLine("//start of let\n");
			result.appendLine("localStack.pushLayer();\n");
			for(Entry<String, ASTNode> entry : letmap.entrySet()){
				CodeFragment val = engine.compile(entry.getValue(), CodeType.R);
				result.appendFragment(val);
				result.appendLine("localStack.put(\"" + entry.getKey() + "\", evalStack.pop());\n");
			}
			
			result.appendFragment(engine.compile(letrule.getInRule(), CodeType.U));
			result.appendLine("localStack.popLayer();\n");
			result.appendLine("//end of let\n");
		} catch (Exception e) {
			throw new CompilerException(e);
		}
	}

}
