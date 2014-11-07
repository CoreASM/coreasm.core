package org.coreasm.compiler.plugins.forall;

import java.util.List;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ASTNode.NameAbstractNodeTuple;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.forallrule.ForallRulePlugin;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;

public class CompilerForallRulePlugin implements CompilerCodeUPlugin, CompilerPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerForallRulePlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}
	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException{
		if ((n.getGrammarClass().equals("Rule")) && (n.getGrammarRule().equals("ForallRule"))) {
			CodeFragment result;
			try {
				result = new CodeFragment("");
				
				//find out which nodes are present
				List<ASTNode> children = n.getAbstractChildNodes();
				
				CodeFragment name = CoreASMCompiler.getEngine().compile(children.get(0), CodeType.L);
				result.appendFragment(name);
				result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");
				
				CodeFragment source = CoreASMCompiler.getEngine().compile(children.get(1), CodeType.R);
				CodeFragment guard = null;
				CodeFragment dorule = null;
				
				for(NameAbstractNodeTuple nant : n.getAbstractChildNodesWithNames()){
					if(nant.name.equals("guard")){
						if(guard != null) throw new CompilerException("invalid parse tree");
						guard = CoreASMCompiler.getEngine().compile(nant.node, CodeType.R);
					}
					else if(nant.name.equals("rule")){
						if(dorule != null) throw new CompilerException("invalid parse tree");
						dorule = CoreASMCompiler.getEngine().compile(nant.node, CodeType.U);
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
					result.appendLine("//testcomment1\n");
					result.appendLine("localStack.put(@nameloc@.name, @xenumsrcx@.get(@countervar@));\n");
					result.appendFragment(guard);
					result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
					result.appendLine("@xslistx@.add(@xenumsrcx@.get(@countervar@));\n");
					result.appendLine("}\n");
					result.appendLine("localStack.popLayer();\n");
					result.appendLine("}\n");
				}
				else{
					result.appendLine("@decl(java.util.Collection<? extends CompilerRuntime.Element>,xslistx)=@xenumx@.enumerate();\n");
				}

				result.appendLine("@decl(int,exec)=0;\n");
				result.appendLine("if(@xslistx@.size() > 0){\n");
				result.appendLine("for(@decl(CompilerRuntime.Element,xelementx) : @xslistx@){\n");
				result.appendLine("@exec@++;\n");
				result.appendLine("localStack.pushLayer();\n");
				result.appendLine("//testcomment2\n");
				result.appendLine("localStack.put(@nameloc@.name, @xelementx@);\n");
				result.appendFragment(dorule);
				result.appendLine("localStack.popLayer();\n");
				result.appendLine("}\n");
				result.appendLine("}\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
				result.appendLine("for(@decl(int,i)=0;@i@<@exec@;@i@++){\n");
				result.appendLine("@ulist@.addAll((CompilerRuntime.UpdateList)evalStack.pop());");
				result.appendLine("}\n");
				result.appendLine("evalStack.push(@ulist@);\n");
				return result;
			} catch (Exception e) {
				throw new CompilerException("invalid code generated");
			}
		}

		throw new CompilerException("unhandled code type: (ForallRulePlugin, uCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public String getName() {
		return ForallRulePlugin.PLUGIN_NAME;
	}
}
