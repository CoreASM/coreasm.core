package org.coreasm.compiler.plugins.chooserule;

import java.util.List;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ASTNode.NameAbstractNodeTuple;
import org.coreasm.engine.plugins.chooserule.ChooseRulePlugin;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerChooseRulePlugin implements CompilerCodeUPlugin, CompilerPlugin, CompilerCodeRPlugin{
	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException{
		if ((n.getGrammarClass().equals("Rule")) && (n.getGrammarRule().equals("ChooseRule"))) {
			CodeFragment result;
			try {
				result = new CodeFragment("");
				
				//find out which nodes are present
				List<ASTNode> children = n.getAbstractChildNodes();
				
				CodeFragment source = CoreASMCompiler.getEngine().compile(children.get(1), CodeType.R);
				CodeFragment guard = null;
				CodeFragment dorule = null;
				CodeFragment ifnone = null;

				CodeFragment name = CoreASMCompiler.getEngine().compile(children.get(0), CodeType.L);
				result.appendFragment(name);
				result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");
				
				for(NameAbstractNodeTuple nant : n.getAbstractChildNodesWithNames()){
					if(nant.name.equals("guard")){
						if(guard != null) throw new CompilerException("invalid parse tree");
						guard = CoreASMCompiler.getEngine().compile(nant.node, CodeType.R);
					}
					else if(nant.name.equals("dorule")){
						if(dorule != null) throw new CompilerException("invalid parse tree");
						dorule = CoreASMCompiler.getEngine().compile(nant.node, CodeType.U);
					}
					else if(nant.name.equals("ifnonerule")){
						if(ifnone != null) throw new CompilerException("invalid parse tree");
						ifnone = CoreASMCompiler.getEngine().compile(nant.node, CodeType.U);
					}
				}
				if(dorule == null) throw new CompilerException("invalid parse tree");
				
				//first find the source of the elements we want to consider
				result.appendFragment(source);
				//then fetch it from the abstract storage and try to convert it to Enumerable
				result.appendLine("@decl(CompilerRuntime.Enumerable,xenumx)=(CompilerRuntime.Enumerable)evalStack.pop();\n");
				//result.appendLine("@decl(CompilerRuntime.Enumerable,xenumx)=(CompilerRuntime.Enumerable)CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue((CompilerRuntime.Location)evalStack.pop());\n");
				//select all elements that satisfy the condition
				//TODO: fix codefragment, so there is no error with the loop
				if(guard != null){
					result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,xslistx)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
					result.appendLine("@decl(java.util.List<CompilerRuntime.Element>, xenumsrcx)= new java.util.ArrayList<CompilerRuntime.Element>(@xenumx@.enumerate());\n");
					result.appendLine("for(@decl(int,countervar) = 0; @countervar@ < @xenumsrcx@.size(); @countervar@++){\n");
					result.appendLine("localStack.pushLayer();\n");
					result.appendLine("localStack.put(@nameloc@.name, @xenumsrcx@.get(@countervar@));\n");
					result.appendFragment(guard);
					result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
					result.appendLine("@xslistx@.add(@xenumsrcx@.get(@countervar@));\n");
					result.appendLine("}\n");
					result.appendLine("localStack.popLayer();\n");
					result.appendLine("}\n");
				}
				else{
					result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,xslistx)=new java.util.ArrayList<CompilerRuntime.Element>(@xenumx@.enumerate());\n");
				}
				
				result.appendLine("@decl(boolean,hasupdate)=false;\n");
				
				if(ifnone != null){
					result.appendLine("if(@xslistx@.size()<=0){\n");
					result.appendFragment(ifnone);
					result.appendLine("@hasupdate@=true;\n");
					result.appendLine("}\n");
				}
				
				//now, choose one of the elements and execute the dorule with it (given there is at least one element
				result.appendLine("if(@xslistx@.size() > 0){\n");
				result.appendLine("localStack.pushLayer();\n");
				result.appendLine("localStack.put(@nameloc@.name, @xslistx@.get(CompilerRuntime.RuntimeProvider.getRuntime().randInt(@xslistx@.size())));\n");
				result.appendFragment(dorule);
				result.appendLine("@hasupdate@=true;\n");
				result.appendLine("localStack.popLayer();\n");
				
				result.appendLine("}\n");
				
				result.appendLine("if(!@hasupdate@){\n");
				result.appendLine("evalStack.push(new CompilerRuntime.UpdateList());\n");
				result.appendLine("}\n");
				
				return result;
			} catch (Exception e) {
				throw new CompilerException("invalid code generated");
			}
		}

		throw new CompilerException("unhandled code type: (ChooseRulePlugin, uCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
	@Override
	public String getName() {
		return ChooseRulePlugin.PLUGIN_NAME;
	}
	@Override
	public CodeFragment rCode(ASTNode n) throws CompilerException {
		if(n.getGrammarClass().equals("Expression") && n.getGrammarRule().equals("PickExp")){
			CodeFragment loc = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(0), CodeType.L);
			CodeFragment source = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(1), CodeType.R);
			
			CodeFragment result = new CodeFragment("");
			result.appendFragment(loc);
			result.appendLine("@decl(String,loc)=((CompilerRuntime.Location)evalStack.pop()).name;\n");
			result.appendFragment(source);
			result.appendLine("@decl(java.util.ArrayList<CompilerRuntime.Element>, src) = new java.util.ArrayList<CompilerRuntime.Element>(((CompilerRuntime.Enumerable)evalStack.pop()).enumerate());\n");
			if(n.getAbstractChildNodes().size() == 3){
				CodeFragment test = CoreASMCompiler.getEngine().compile(n.getAbstractChildNodes().get(2), CodeType.R);
				result.appendLine("for(@decl(int,i)=@src@.size() - 1; @i@ >= 0; @i@--){\n");
				result.appendLine("localStack.pushLayer();\n");
				result.appendLine("localStack.put(@loc@, @src@.get(@i@));\n");
				result.appendFragment(test);
				result.appendLine("localStack.popLayer();\n");
				result.appendLine("if(!evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
				result.appendLine("@src@.remove(@i@);\n");
				result.appendLine("}\n");
				result.appendLine("}\n");
			}
			result.appendLine("evalStack.push(@src@.get(CompilerRuntime.RuntimeProvider.getRuntime().randInt(@src@.size())));\n");
			return result;
		}
		throw new CompilerException("unhandled code type: (ChooseRulePlugin, rCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
}
