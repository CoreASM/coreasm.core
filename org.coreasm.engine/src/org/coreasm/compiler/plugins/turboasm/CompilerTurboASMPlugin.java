package org.coreasm.compiler.plugins.turboasm;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.Preprocessor;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.turboasm.TurboASMPlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerTurboASMPlugin implements CompilerPlugin, CompilerCodeUPlugin, CompilerCodeRPlugin {

	@Override
	public CodeFragment uCode(ASTNode n)
			throws CompilerException {
		
		if(n.getGrammarClass().equals("Rule")){
			if(n.getGrammarRule().equals("SeqRule") || n.getGrammarRule().equals("SeqBlockRule")){
				CodeFragment result = new CodeFragment("");
				CodeFragment first = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.U);
				CodeFragment second = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(1), CodeType.U);
				
				//obtain the updates of the first rule
				result.appendFragment(first);
				result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
				//aggregate updates on the update set of the first rule
				result.appendLine("@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,alist)=@storage@.performAggregation(@ulist@);\n");
				result.appendLine("if(@storage@.isConsistent(@alist@)){\n");
				result.appendLine("@storage@.pushState();\n");
				result.appendLine("@storage@.apply(@alist@);\n");
				result.appendFragment(second);
				result.appendLine("@decl(CompilerRuntime.UpdateList,comp)=@storage@.compose(@ulist@,(CompilerRuntime.UpdateList)evalStack.pop());\n");
				result.appendLine("@storage@.popState();\n");
				result.appendLine("evalStack.push(@comp@);\n");
				result.appendLine("}\n");
				result.appendLine("else{\n");
				result.appendLine("evalStack.push(@ulist@);\n");
				result.appendLine("}\n");
				return result;
			}
			else if(n.getGrammarRule().equals("IterateRule")){
				CodeFragment rule = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.U);
				CodeFragment result = new CodeFragment("");
				result.appendLine("@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,composed)=new CompilerRuntime.UpdateList();\n");
				result.appendLine("@storage@.pushState();\n");
				result.appendFragment(rule);
				result.appendLine("@decl(CompilerRuntime.UpdateList,current)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
				result.appendLine("while(!@current@.isEmpty()){\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,aggreg)=@storage@.performAggregation(@current@);\n");
				result.appendLine("@composed@ = @storage@.compose(@composed@,@current@);\n");
				result.appendLine("if(@storage@.isConsistent(@aggreg@)){\n");
				result.appendLine("@storage@.apply(@aggreg@);\n");
				result.appendFragment(rule);
				result.appendLine("@current@ = (CompilerRuntime.UpdateList) evalStack.pop();\n");
				result.appendLine("}\n");
				result.appendLine("else{\n");
				result.appendLine("break;\n");
				result.appendLine("}\n");
				result.appendLine("}\n");
				result.appendLine("@storage@.popState();\n");
				result.appendLine("evalStack.push(@composed@);\n");
				return result;
			}
			else if(n.getGrammarRule().equals("WhileRule")){
				CodeFragment guard = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.R);
				CodeFragment rule = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(1), CodeType.U);
				CodeFragment result = new CodeFragment("");
				result.appendLine("@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,composed)=new CompilerRuntime.UpdateList();\n");
				result.appendLine("@decl(boolean,guard)=true;\n");
				result.appendFragment(guard);
				result.appendLine("@guard@=((CompilerRuntime.BooleanElement)evalStack.pop()).getValue();\n");
				result.appendLine("@storage@.pushState();\n");
				result.appendLine("if(@guard@){\n");
				result.appendFragment(rule);
				result.appendLine("@decl(CompilerRuntime.UpdateList,current)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
				result.appendLine("while(!@current@.isEmpty() && @guard@){\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,aggreg)=@storage@.performAggregation(@current@);\n");
				result.appendLine("@composed@ = @storage@.compose(@composed@,@current@);\n");
				result.appendLine("if(@storage@.isConsistent(@aggreg@)){\n");
				result.appendLine("@storage@.apply(@aggreg@);\n");
				result.appendFragment(guard);
				result.appendLine("@guard@=((CompilerRuntime.BooleanElement)evalStack.pop()).getValue();\n");
				result.appendLine("if(!@guard@){\n");
				result.appendLine("break;\n");
				result.appendLine("}\n");
				result.appendFragment(rule);
				result.appendLine("@current@ = (CompilerRuntime.UpdateList) evalStack.pop();\n");
				result.appendLine("}\n");
				result.appendLine("else{\n");
				result.appendLine("break;\n");
				result.appendLine("}\n");
				result.appendLine("}\n");
				result.appendLine("}\n");
				result.appendLine("@storage@.popState();\n");
				result.appendLine("evalStack.push(@composed@);\n");
				return result;
			}
			else if(n.getGrammarRule().equals("LocalRule")){
				CodeFragment id = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.L);
				CodeFragment rule = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(1), CodeType.U);
				
				CodeFragment result = new CodeFragment("");
				result.appendFragment(id);
				result.appendFragment(rule);
				result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.Location,loc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,result)=new CompilerRuntime.UpdateList();\n");
				result.appendLine("for(@decl(CompilerRuntime.Update,u): @ulist@){\n");
				result.appendLine("if(!@u@.loc.name.equals(@loc@.name)){\n");
				result.appendLine("@result@.add(@u@);\n");
				result.appendLine("}\n");
				result.appendLine("}\n");
				result.appendLine("evalStack.push(@result@);\n");
				return result;
			}
			else if(n.getGrammarRule().equals("ReturnResultRule")){
				//note: this implementation currently contains a lot of code from the kernel
				//macro call. might be a good idea to merge it somehow
				
				CodeFragment result = new CodeFragment("");
				
				ASTNode leftpart = n.getAbstractChildNodes().get(0);
				ASTNode rulecall = n.getAbstractChildNodes().get(1);
				
				String name = rulecall.getAbstractChildNodes().get(0).getToken();
				
				result.appendLine("@decl(java.util.ArrayList<CompilerRuntime.RuleParam>,arglist)=new java.util.ArrayList<>();");
				for(int i = 1; i < rulecall.getAbstractChildNodes().size(); i++){
					CodeFragment tmp = CoreASMCompiler.getEngine().compile(rulecall.getAbstractChildNodes().get(i), CodeType.R);
					// create the param object and push it onto the stack
					result.appendLine("\n@arglist@.add(new CompilerRuntime.RuleParam(){\n");
					result.appendLine("public CompilerRuntime.Rule getUpdateResponsible(){\nreturn null;\n}\n");
					result.appendLine("java.util.Map<String, CompilerRuntime.RuleParam> ruleparams;\n");
					result.appendLine("public void setParams(java.util.Map<String, CompilerRuntime.RuleParam> params){\n");
					result.appendLine("this.ruleparams = params;\n");
					result.appendLine("}\n");
					result.appendLine("public CompilerRuntime.Element evaluate(CompilerRuntime.LocalStack localStack) throws Exception{\n");
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
				result.appendLine("public CompilerRuntime.Element evaluate(CompilerRuntime.LocalStack localStack) throws Exception{\n");
				result.appendFragment(CoreASMCompiler.getEngine().compile(leftpart, CodeType.R));
				result.appendLine("\nreturn (CompilerRuntime.Element)evalStack.pop();\n}\n});\n");
				result.appendLine("@arglist@.get(@arglist@.size() - 1).setParams(ruleparams);\n");
				
				//result.appendLine("\n@arglist@.add(new CompilerRuntime.RuleParam(){\npublic CompilerRuntime.Element evaluate(CompilerRuntime.LocalStack localStack) throws Exception{\n");
				//result.appendLine("\nreturn (CompilerRuntime.Element)evalStack.pop();\n}\n});\n");
				
				//cf.appendLine("\n@decl(CompilerRuntime.Rule,macrorule)=new Rules." + name + "(@arglist, localStack);");
				Preprocessor prep = CoreASMCompiler.getEngine().getPreprocessor();
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
				result.appendFragment(CoreASMCompiler.getEngine().compile(leftpart, CodeType.L));
				result.appendLine("@decl(CompilerRuntime.Location, uloc) = (CompilerRuntime.Location) evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList, ulist) = new CompilerRuntime.UpdateList();\n");
				result.appendLine("@ulist@.addAll(@result@.updates);\n");
				result.appendLine("@ulist@.add(new CompilerRuntime.Update(@uloc@, @result@.value, CompilerRuntime.Update.UPDATE_ACTION, this.getUpdateResponsible()));\n");
				result.appendLine("evalStack.push(@ulist@);\n");
				
				return result;
			}
		}
		
		
		
		throw new CompilerException("unhandled code type: (TurboASMPlugin, uCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public String getName() {
		return TurboASMPlugin.PLUGIN_NAME;
	}

	@Override
	public CodeFragment rCode(ASTNode n)
			throws CompilerException {

		if(n.getGrammarClass().equals("Expression")){
			if(n.getGrammarRule().equals("ReturnRule")){
				//Note: The current definition of return result
				//is a bit strange. Even though it returns an expression,
				//it can't be used as one and can only be used as the top
				//level node of a rule, making that rule return a value.
				
				CodeFragment exp = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.R);
				CodeFragment rule = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(1), CodeType.U);
				
				CodeFragment result = new CodeFragment("");
				result.appendFragment(rule);
				result.appendLine("@decl(CompilerRuntime.UpdateList,updates)=(CompilerRuntime.UpdateList)evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,aggreg)=@storage@.performAggregation(@updates@);\n");
				result.appendLine("if(@storage@.isConsistent(@aggreg@)){\n");
				result.appendLine("@storage@.pushState();\n");
				result.appendLine("@storage@.apply(@aggreg@);\n");
				result.appendFragment(exp);
				result.appendLine("@storage@.popState();\n");
				result.appendLine("}\n");
				result.appendLine("else{\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");
				
				return result;
			}
		}
		throw new CompilerException("unhandled code type: (TurboASMPlugin, rCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
}
