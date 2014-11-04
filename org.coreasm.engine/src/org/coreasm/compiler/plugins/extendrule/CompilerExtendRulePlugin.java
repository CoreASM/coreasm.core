package org.coreasm.compiler.plugins.extendrule;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.extendrule.ExtendRulePlugin;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerExtendRulePlugin implements CompilerCodeUPlugin, CompilerPlugin{
	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException{
		if(n.getGrammarClass().equals("Rule") && n.getGrammarRule().equals("ExtendRule")){
			try {
				CodeFragment loc = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.L);
				CodeFragment upd = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(2), CodeType.U);
				
				CodeFragment result = new CodeFragment("");
				
				CodeFragment name = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(1), CodeType.L);
				result.appendFragment(name);
				result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");
				
				result.appendLine("@decl(CompilerRuntime.Element,elem)=new CompilerRuntime.Element();\n"); // create the new element
				result.appendLine("localStack.pushLayer();\n");
				result.appendLine("localStack.put(@nameloc@.name, @elem@);\n"); // add it to the local environment
				result.appendFragment(upd); // execute the rule
				result.appendLine("localStack.popLayer();\n");
				result.appendFragment(loc); //get the target location
				result.appendLine("@decl(String, target)=((CompilerRuntime.Location)evalStack.pop()).name;\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
				
				result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,arglist)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
				result.appendLine("@arglist@.add(@elem@);\n");
				result.appendLine("@ulist@.add(new CompilerRuntime.Update(new CompilerRuntime.Location(@target@,@arglist@), CompilerRuntime.BooleanElement.TRUE, CompilerRuntime.Update.UPDATE_ACTION, this.getUpdateResponsible()));\n");
				result.appendLine("evalStack.push(@ulist@);\n");
				
				return result;
			} catch (Exception e) {
				throw new CompilerException(e);
			}
			
		}
		
		
		throw new CompilerException("unhandled code type: (ExtendRulePlugin, uCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public String getName() {
		return ExtendRulePlugin.PLUGIN_NAME;
	}
}
