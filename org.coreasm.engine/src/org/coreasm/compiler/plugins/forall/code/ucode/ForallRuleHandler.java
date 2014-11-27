package org.coreasm.compiler.plugins.forall.code.ucode;

import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;

public class ForallRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		
		ForallRuleNode forall = (ForallRuleNode) node;
		
		Map<String, ASTNode> vars = forall.getVariableMap();
		
		result.appendLine("//forall starts here\n");
		result.appendLine("localStack.pushLayer();\n");
		int varcount = 0;
		
		String[] varnames = new String[vars.size()];
		
		for(Entry<String, ASTNode> e : vars.entrySet()){
			varnames[varcount] = e.getKey();
			result.appendFragment(engine.compile(e.getValue(), CodeType.R));
			result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,var" + varcount + ")=new java.util.ArrayList<CompilerRuntime.Element>(((CompilerRuntime.Enumerable) evalStack.pop()).enumerate());\n");
			varcount++;
		}
		
		result.appendLine("@decl(int, exec) = 0;\n");
		
		//open for loops
		for(int i = 0; i < varcount; i++){
			result.appendLine("for(@decl(int, i" + i + ")=0; @i" + i + "@ < @var" + i + "@.size(); @i" + i + "@++){\n");
			result.appendLine("localStack.put(\"" + varnames[i] + "\", @var" + i + "@.get(@i" + i + "@));\n");
		}
		
		if(forall.getCondition() != null){
			result.appendFragment(engine.compile(forall.getCondition(), CodeType.R));
			result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
			result.appendLine("@exec@++;\n");
			result.appendFragment(engine.compile(forall.getDoRule(), CodeType.U));
			result.appendLine("}\n");
		}
		else{
			result.appendLine("@exec@++;\n");
			result.appendFragment(engine.compile(forall.getDoRule(), CodeType.U));
		}
		
		//close for loops
		for(int i = 0; i < varcount; i++){
			result.appendLine("}\n");
		}
		
		result.appendLine("localStack.popLayer();\n");
		
		if(forall.getIfnoneRule() == null){
			result.appendLine("@decl(CompilerRuntime.UpdateList, res) = new CompilerRuntime.UpdateList();\n");
			result.appendLine("for(@decl(int,v)=0; @v@ < @exec@; @v@++){\n");
			result.appendLine("@res@.addAll((CompilerRuntime.UpdateList)evalStack.pop());\n");
			result.appendLine("}\n");
			result.appendLine("evalStack.push(@res@);\n");
		}
		else{
			result.appendLine("if(@exec@ == 0){\n");
			result.appendFragment(engine.compile(forall.getIfnoneRule(), CodeType.U));
			result.appendLine("}\n");
			result.appendLine("else{\n");
			result.appendLine("@decl(CompilerRuntime.UpdateList, res) = new CompilerRuntime.UpdateList();\n");
			result.appendLine("for(@decl(int,v)=0; @v@ < @exec@; @v@++){\n");
			result.appendLine("@res@.addAll((CompilerRuntime.UpdateList)evalStack.pop());\n");
			result.appendLine("}\n");
			result.appendLine("evalStack.push(@res@);\n");
			result.appendLine("}\n");
		}
		
		/*try {			
			//find out which nodes are present
			List<ASTNode> children = node.getAbstractChildNodes();
			
			CodeFragment name = engine.compile(children.get(0), CodeType.L);
			result.appendFragment(name);
			result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
			result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");
			
			CodeFragment source = engine.compile(children.get(1), CodeType.R);
			CodeFragment guard = null;
			CodeFragment dorule = null;
			
			for(NameAbstractNodeTuple nant : node.getAbstractChildNodesWithNames()){
				if(nant.name.equals("guard")){
					if(guard != null) throw new CompilerException("invalid parse tree");
					guard = engine.compile(nant.node, CodeType.R);
				}
				else if(nant.name.equals("rule")){
					if(dorule != null) throw new CompilerException("invalid parse tree");
					dorule = engine.compile(nant.node, CodeType.U);
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
		} catch (Exception e) {
			throw new CompilerException("invalid code generated");
		}*/
	}

}
