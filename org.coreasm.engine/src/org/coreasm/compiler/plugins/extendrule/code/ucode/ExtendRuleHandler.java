package org.coreasm.compiler.plugins.extendrule.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class ExtendRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		try {
			CodeFragment loc = engine.compile(node.getAbstractChildNodes().get(0), CodeType.L);
			CodeFragment upd = engine.compile(node.getAbstractChildNodes().get(2), CodeType.U);
						
			CodeFragment name = engine.compile(node.getAbstractChildNodes().get(1), CodeType.L);
			result.appendFragment(name);
			result.appendLine("@decl(@RuntimePkg@.Location,nameloc)=(@RuntimePkg@.Location)evalStack.pop();\n");
			result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");
			
			result.appendLine("@decl(@RuntimePkg@.Element,elem)=new @RuntimePkg@.Element();\n"); // create the new element
			result.appendLine("localStack.pushLayer();\n");
			result.appendLine("localStack.put(@nameloc@.name, @elem@);\n"); // add it to the local environment
			result.appendFragment(upd); // execute the rule
			result.appendLine("localStack.popLayer();\n");
			result.appendFragment(loc); //get the target location
			result.appendLine("@decl(String, target)=((@RuntimePkg@.Location)evalStack.pop()).name;\n");
			result.appendLine("@decl(@RuntimePkg@.UpdateList,ulist)=(@RuntimePkg@.UpdateList)evalStack.pop();\n");
			
			result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,arglist)=new java.util.ArrayList<@RuntimePkg@.Element>();\n");
			result.appendLine("@arglist@.add(@elem@);\n");
			result.appendLine("@ulist@.add(new @RuntimePkg@.Update(new @RuntimePkg@.Location(@target@,@arglist@), @RuntimePkg@.BooleanElement.TRUE, @RuntimePkg@.Update.UPDATE_ACTION, this.getUpdateResponsible(), null));\n");
			result.appendLine("evalStack.push(@ulist@);\n");
		} catch (Exception e) {
			throw new CompilerException(e);
		}
	}

}
