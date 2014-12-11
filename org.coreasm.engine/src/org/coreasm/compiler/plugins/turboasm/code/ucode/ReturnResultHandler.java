package org.coreasm.compiler.plugins.turboasm.code.ucode;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.Preprocessor;
import org.coreasm.engine.interpreter.ASTNode;

public class ReturnResultHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		//note: this implementation currently contains a lot of code from the kernel
		//macro call. might be a good idea to merge it somehow
		
		ASTNode leftpart = node.getAbstractChildNodes().get(0);
		ASTNode rulecall = node.getAbstractChildNodes().get(1);
		
		String name = rulecall.getAbstractChildNodes().get(0).getToken();
		
		result.appendLine("@decl(java.util.ArrayList<CompilerRuntime.RuleParam>,arglist)=new java.util.ArrayList<>();");
		for(int i = 1; i < rulecall.getAbstractChildNodes().size(); i++){
			CodeFragment tmp = engine.compile(rulecall.getAbstractChildNodes().get(i), CodeType.R);
			// create the param object and push it onto the stack
			result.appendLine("\n@arglist@.add(new CompilerRuntime.RuleParam(){\n");
			result.appendLine("public CompilerRuntime.Rule getUpdateResponsible(){\nreturn null;\n}\n");
			result.appendLine("java.util.Map<String, CompilerRuntime.RuleParam> ruleparams;\n");
			result.appendLine("public void setParams(java.util.Map<String, CompilerRuntime.RuleParam> params){\n");
			result.appendLine("this.ruleparams = params;\n");
			result.appendLine("}\n");
			
			//try to create code for the location of the parameter
			result.appendLine("public CompilerRuntime.Location evaluateL(CompilerRuntime.LocalStack localStack) throws Exception{\n");
			try{
				CodeFragment tmpl = engine.compile(rulecall.getAbstractChildNodes().get(i), CodeType.L);
				result.appendFragment(tmpl);
				result.appendLine("return (CompilerRuntime.Location) evalStack.pop();\n");	
			}
			catch(Exception e){
				result.appendLine("throw new Exception(\"This ruleparam cannot be evaluated as a location\");\n");
			}
			result.appendLine("}\n");
			
			
			result.appendLine("public CompilerRuntime.Element evaluateR(CompilerRuntime.LocalStack localStack) throws Exception{\n");
			result.appendFragment(tmp);
			result.appendLine("\nreturn (CompilerRuntime.Element)evalStack.pop();\n}\n});\n");
			result.appendLine("@arglist@.get(@arglist@.size() - 1).setParams(ruleparams);\n");
		}
		//add a new parameter for the value of the left location
		result.appendLine("\n@arglist@.add(new CompilerRuntime.RuleParam(){\n");
		result.appendLine("public CompilerRuntime.Rule getUpdateResponsible(){\nreturn null;\n}\n");
		result.appendLine("java.util.Map<String, CompilerRuntime.RuleParam> ruleparams;\n");
		result.appendLine("public void setParams(java.util.Map<String, CompilerRuntime.RuleParam> params){\n");
		result.appendLine("this.ruleparams = params;\n");
		result.appendLine("}\n");
		result.appendLine("public CompilerRuntime.Location evaluateL(CompilerRuntime.LocalStack localStack) throws Exception{\n");
		//location returned is always "result"
		result.appendLine("return new CompilerRuntime.Location(\"result\", CompilerRuntime.ElementList.NO_ARGUMENT);\n");
		result.appendLine("}\n");
		result.appendLine("public CompilerRuntime.Element evaluateR(CompilerRuntime.LocalStack localStack) throws Exception{\n");
		result.appendFragment(engine.compile(leftpart, CodeType.R));
		result.appendLine("\nreturn (CompilerRuntime.Element)evalStack.pop();\n}\n});\n");
		result.appendLine("@arglist@.get(@arglist@.size() - 1).setParams(ruleparams);\n");
		
		//result.appendLine("\n@arglist@.add(new CompilerRuntime.RuleParam(){\npublic CompilerRuntime.Element evaluate(CompilerRuntime.LocalStack localStack) throws Exception{\n");
		//result.appendLine("\nreturn (CompilerRuntime.Element)evalStack.pop();\n}\n});\n");
		
		//cf.appendLine("\n@decl(CompilerRuntime.Rule,macrorule)=new Rules." + name + "(@arglist, localStack);");
		Preprocessor prep = engine.getPreprocessor();
		Information inf = prep.getGeneralInfo().get("RuleDeclaration");
		if(inf.getChildren().contains(name)){
			//if name is a valid rulename, so call it by creating a new rule instance
			result.appendLine("@decl(CompilerRuntime.Rule, callruletmp)=new Rules." + name + "();\n");
		}
		else{
			throw new UnsupportedOperationException("currently not supported");
			//otherwise, name is a parameter on the local stack
			//result.appendLine("@decl(CompilerRuntime.Rule,callruletmp);\n");
			//result.appendLine("@callruletmp@ = ((CompilerRuntime.Rule)((CompilerRuntime.RuleParam) localStack.get(\"" + name + "\")).evaluate(localStack)).getCopy();\n" );  
		}
		result.appendLine("@callruletmp@.initRule(@arglist@, localStack);\n");
		result.appendLine("@decl(CompilerRuntime.RuleResult, result) = @callruletmp@.call();\n");
		//@result@ now contains the updates generated by the rule and the value of the result identifier
		//add an update binding the value to the location
		result.appendFragment(engine.compile(leftpart, CodeType.L));
		result.appendLine("@decl(CompilerRuntime.Location, uloc) = (CompilerRuntime.Location) evalStack.pop();\n");
		result.appendLine("@decl(CompilerRuntime.UpdateList, ulist) = new CompilerRuntime.UpdateList();\n");
		result.appendLine("@ulist@.addAll(@result@.updates);\n");
		result.appendLine("@ulist@.add(new CompilerRuntime.Update(@uloc@, @result@.value, CompilerRuntime.Update.UPDATE_ACTION, this.getUpdateResponsible(), null));\n");
		result.appendLine("evalStack.push(@ulist@);\n");
	}

}
