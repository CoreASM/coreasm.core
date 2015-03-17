package org.coreasm.compiler.plugins.caserule.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.CodeWrapperEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Handles the case rule.
 * The implementation uses {@link CodeWrapperEntry} to avoid the generation
 * of code which exceeds the java limitations.
 * @author Spellmaker
 *
 */
public class CaseRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		try{
			result.appendLine("");
			
			CodeFragment guardcode = engine.compile(node.getAbstractChildNodes().get(0), CodeType.R);
			CodeFragment[] conditions = new CodeFragment[(node.getAbstractChildNodes().size() - 1) / 2];
			CodeFragment[] rules = new CodeFragment[(node.getAbstractChildNodes().size() - 1) / 2];
			
			for(int i = 1; i < node.getAbstractChildNodes().size(); i += 2){
				conditions[(i - 1) / 2] = engine.compile(node.getAbstractChildNodes().get(i), CodeType.R);
				rules[(i - 1) / 2] = engine.compile(node.getAbstractChildNodes().get(i + 1), CodeType.U);
			}
			
			result.appendFragment(guardcode);
			result.appendLine("@decl(@RuntimePkg@.Element,guard)=(@RuntimePkg@.Element)evalStack.pop();\n");
			//result.appendLine("@decl(int,exec)=0;\n");
			result.appendLine("evalStack.push(@guard@);\n");
			result.appendLine("evalStack.push(new Integer(0));\n");
			
			CodeFragment condcode = new CodeFragment("");
			for(int i = 0; i < conditions.length; i++){
				CodeFragment current = new CodeFragment("");
				current.appendLine("@decl(int,count) = (Integer) evalStack.pop();\n");
				current.appendLine("@decl(@RuntimePkg@.Element,guard)=(@RuntimePkg@.Element)evalStack.pop();\n");
				current.appendFragment(conditions[i]);
				current.appendLine("if(@guard@.equals(evalStack.pop())){\n");
				current.appendFragment(rules[i]);
				current.appendLine("evalStack.push(@guard@);\n");
				current.appendLine("evalStack.push(@count@ + 1);\n");
				current.appendLine("}\n");
				current.appendLine("else{\n");
				current.appendLine("evalStack.push(@guard@);\n");
				current.appendLine("evalStack.push(@count@);\n");
				current.appendLine("}\n");
				
				condcode.appendFragment(current);
				if(condcode.getByteCount() > 40000){
					condcode = CodeWrapperEntry.buildWrapper(condcode, "CaseRuleHandler", engine);
				}
			}
			
			result.appendFragment(condcode);
			result.appendLine("@decl(int,exec)=(Integer)evalStack.pop();\nevalStack.pop();\n");
			
			/*for(int i = 0; i < conditions.length; i++){
				result.appendFragment(conditions[i]);
				result.appendLine("if(@guard@.equals(evalStack.pop())){\n");
				result.appendLine("@exec@++;\n");
				result.appendFragment(rules[i]);
				result.appendLine("}\n");
			}*/
			result.appendLine("@decl(@RuntimePkg@.UpdateList,ulist)=new @RuntimePkg@.UpdateList();\n");
			result.appendLine("for(@decl(int,i)=0;@i@<@exec@;@i@++){\n");
			result.appendLine("@ulist@.addAll((@RuntimePkg@.UpdateList)evalStack.pop());\n");
			result.appendLine("}\n");
			result.appendLine("evalStack.push(@ulist@);\n");			
		} catch (Exception e) {
			throw new CompilerException("invalid code generated");
		}
	}

}
