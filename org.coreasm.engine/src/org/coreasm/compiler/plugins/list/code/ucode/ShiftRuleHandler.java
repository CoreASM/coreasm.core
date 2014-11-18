package org.coreasm.compiler.plugins.list.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.list.ShiftRuleNode;

public class ShiftRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		ShiftRuleNode srn = (ShiftRuleNode) node;
		
		result.appendFragment(engine.compile(srn.getLocationNode(), CodeType.L));
		result.appendFragment(engine.compile(srn.getListNode(), CodeType.LR));
		
		result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>(((plugins.ListPlugin.ListElement)evalStack.pop()).values());\n");
		result.appendLine("@decl(CompilerRuntime.Location,listloc)=(CompilerRuntime.Location)evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.Location,loc)=(CompilerRuntime.Location)evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
		
		if(srn.isLeft){
			result.appendLine("@ulist@.add(new CompilerRuntime.Update(@loc@,@list@.get(0),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
			result.appendLine("@list@.remove(0);\n");
			result.appendLine("@ulist@.add(new CompilerRuntime.Update(@listloc@,new plugins.ListPlugin.ListElement(@list@),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
		}
		else{
			result.appendLine("@ulist@.add(new CompilerRuntime.Update(@loc@,@list@.get(@list@.size() - 1),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
			result.appendLine("@list@.remove(@list@.size() - 1);\n");
			result.appendLine("@ulist@.add(new CompilerRuntime.Update(@listloc@,new plugins.ListPlugin.ListElement(@list@),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
		}
		
		result.appendLine("evalStack.push(@ulist@);\n");
	}

}
