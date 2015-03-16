package org.coreasm.compiler.plugins.forall.code.ucode;

import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;

public class ForallRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		
		ForallRuleNode forall = (ForallRuleNode) node;
		
		Map<String, ASTNode> vars = forall.getVariableMap();
		
		result.appendLine("//forall starts here\n");
		result.appendLine("localStack.pushLayer();\n");
		int varcount = 0;
		
		String[] varnames = new String[vars.size()];
		
		//compile the sources
		result.appendLine("@decl(boolean,hasempty) = false;\n");
		for(Entry<String, ASTNode> e : vars.entrySet()){
			varnames[varcount] = e.getKey();
			result.appendFragment(engine.compile(e.getValue(), CodeType.R));
			result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,var" + varcount + ")=new java.util.ArrayList<@RuntimePkg@.Element>(((@RuntimePkg@.Enumerable) evalStack.pop()).enumerate());\n");
			result.appendLine("@hasempty@ = @hasempty@ || @var" + varcount + "@.size() <= 0;\n");
			varcount++;
		}

		result.appendLine("@decl(int, exec) = 0;\n");
		result.appendLine("if(!@hasempty@){\n");
		
		//open for loops
		for(int i = 0; i < varcount; i++){
			result.appendLine("for(@decl(int, i" + i + ")=0; @i" + i + "@ < @var" + i + "@.size(); @i" + i + "@++){\n");
			result.appendLine("localStack.put(\"" + varnames[i] + "\", @var" + i + "@.get(@i" + i + "@));\n");
		}
		
		if(forall.getCondition() != null){
			result.appendFragment(engine.compile(forall.getCondition(), CodeType.R));
			result.appendLine("if(evalStack.pop().equals(@RuntimePkg@.BooleanElement.TRUE)){\n");
			result.appendLine("@exec@++;\n");
			result.appendFragment(engine.compile(forall.getDoRule(), CodeType.U));
			result.appendLine("}\n");
		}
		else{
			result.appendLine("@exec@++;\n");
			result.appendFragment(engine.compile(forall.getDoRule(), CodeType.U));
		}
		
		//close for loops
		for(int i = 0; i < varcount; i++){
			result.appendLine("}\n");
		}
		result.appendLine("}\n");
		result.appendLine("localStack.popLayer();\n");
		
		if(forall.getIfnoneRule() == null){
			result.appendLine("@decl(@RuntimePkg@.UpdateList, res) = new @RuntimePkg@.UpdateList();\n");
			result.appendLine("for(@decl(int,v)=0; @v@ < @exec@; @v@++){\n");
			result.appendLine("@res@.addAll((@RuntimePkg@.UpdateList)evalStack.pop());\n");
			result.appendLine("}\n");
			result.appendLine("evalStack.push(@res@);\n");
		}
		else{
			result.appendLine("if(@exec@ == 0 || @hasempty@){\n");
			result.appendFragment(engine.compile(forall.getIfnoneRule(), CodeType.U));
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("@decl(@RuntimePkg@.UpdateList, res) = new @RuntimePkg@.UpdateList();\n");
			result.appendLine("for(@decl(int,v)=0; @v@ < @exec@; @v@++){\n");
			result.appendLine("@res@.addAll((@RuntimePkg@.UpdateList)evalStack.pop());\n");
			result.appendLine("}\n");
			result.appendLine("evalStack.push(@res@);\n");
			result.appendLine("}\n");
		}
	}

}
