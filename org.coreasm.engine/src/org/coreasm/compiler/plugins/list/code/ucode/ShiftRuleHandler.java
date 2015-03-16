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
		
		result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>(((@ListElement@)evalStack.pop()).values());\n");
		result.appendLine("@decl(@RuntimePkg@.Location,listloc)=(@RuntimePkg@.Location)evalStack.pop();\n");
		result.appendLine("@decl(@RuntimePkg@.Location,loc)=(@RuntimePkg@.Location)evalStack.pop();\n");
		result.appendLine("@decl(@RuntimePkg@.UpdateList,ulist)=new @RuntimePkg@.UpdateList();\n");
		
		if(srn.isLeft){
			result.appendLine("@ulist@.add(new @RuntimePkg@.Update(@loc@,@list@.get(0),@RuntimePkg@.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
			result.appendLine("@list@.remove(0);\n");
			result.appendLine("@ulist@.add(new @RuntimePkg@.Update(@listloc@,new @ListElement@(@list@),@RuntimePkg@.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
		}
		else{
			result.appendLine("@ulist@.add(new @RuntimePkg@.Update(@loc@,@list@.get(@list@.size() - 1),@RuntimePkg@.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
			result.appendLine("@list@.remove(@list@.size() - 1);\n");
			result.appendLine("@ulist@.add(new @RuntimePkg@.Update(@listloc@,new @ListElement@(@list@),@RuntimePkg@.Update.UPDATE_ACTION,this.getUpdateResponsible(), null));\n");
		}
		
		result.appendLine("evalStack.push(@ulist@);\n");
	}

}
