package org.coreasm.compiler.plugins.predicatelogic.code.rcode;

import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.predicatelogic.ExistsExpNode;

/**
 * Handles the exists expression
 * @author Spellmaker
 *
 */
public class ExistsExpHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		ExistsExpNode existsExp = (ExistsExpNode)node;
		
		Map<String, ASTNode> vars = existsExp.getVariableMap();
		
		result.appendLine("//existsExp starts here\n");
		result.appendLine("localStack.pushLayer();\n");
		int varcount = 0;
		
		String[] varnames = new String[vars.size()];
		
		//compile the sources
		result.appendLine("@decl(boolean,result) = false;\n");
		result.appendLine("@decl(boolean,hasempty) = false;\n");
		for(Entry<String, ASTNode> e : vars.entrySet()){
			varnames[varcount] = e.getKey();
			result.appendFragment(engine.compile(e.getValue(), CodeType.R));
			result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,var" + varcount + ")=new java.util.ArrayList<@RuntimePkg@.Element>(((@RuntimePkg@.Enumerable) evalStack.pop()).enumerate());\n");
			result.appendLine("@hasempty@ = @hasempty@ || @var" + varcount + "@.size() <= 0;\n");
			varcount++;
		}
		
		result.appendLine("if(!@hasempty@) {\n");
		
		//open for loops
		for(int i = 0; i < varcount; i++){
			result.appendLine("for(@decl(int, i" + i + ")=0; @i" + i + "@ < @var" + i + "@.size(); @i" + i + "@++){\n");
			result.appendLine("localStack.put(\"" + varnames[i] + "\", @var" + i + "@.get(@i" + i + "@));\n");
		}

		result.appendFragment(engine.compile(existsExp.getCondition(), CodeType.R));
		result.appendLine("if(evalStack.pop().equals(@RuntimePkg@.BooleanElement.TRUE)){\n");
		result.appendLine("@result@=true;\n");
		result.appendLine("break;\n");
		result.appendLine("}\n");
		//close for loops
		for(int i = 0; i < varcount; i++){
			result.appendLine("}\n");
		}
		result.appendLine("}\n");
		result.appendLine("localStack.popLayer();\n");
		result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(@result@));");
	}

}
