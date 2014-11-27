package org.coreasm.compiler.plugins.blockrule.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.CodeWrapperEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

public class BlockRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		
		CodeFragment tmpresult = new CodeFragment("");
		
		result.appendLine("");
		
		if(node.getAbstractChildNodes().size() <= 0) throw new CompilerException("empty BlockRule");
		
		for(int i = 0; i < node.getAbstractChildNodes().size(); i++){
			tmpresult.appendLine("//blockrule child " + i + " start\n");
			tmpresult.appendFragment(engine.compile(node.getAbstractChildNodes().get(i), CodeType.U));
			if(tmpresult.getByteCount() > 40000){
				tmpresult = CodeWrapperEntry.buildWrapper(tmpresult, "blockrulehandler");
			}
		}
	
		tmpresult.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
		
		tmpresult.appendLine("//blockrule collection handler\n");
		tmpresult.appendLine("for(@decl(int,i)=0; @i@ < " + node.getAbstractChildNodes().size() + "; @i@++){\n");
		tmpresult.appendLine("@ulist@.addAll((CompilerRuntime.UpdateList)evalStack.pop());\n");
		tmpresult.appendLine("}\n");
		tmpresult.appendLine("evalStack.push(@ulist@);\n");
		
		result.appendFragment(tmpresult);
	}

}
