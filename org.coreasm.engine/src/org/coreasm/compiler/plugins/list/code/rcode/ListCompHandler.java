package org.coreasm.compiler.plugins.list.code.rcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.list.ListCompNode;
import org.coreasm.engine.plugins.list.TrueGuardNode;

public class ListCompHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		ListCompNode compnode = (ListCompNode) node;
		
		CodeFragment guard = null;
		if(!(compnode.getGuard() instanceof TrueGuardNode)){
			guard = engine.compile(compnode.getGuard(), CodeType.R);
		}
		
		List<String> constrnames = new ArrayList<String>();
		
		result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
		try{
			//evaluate all constrainer domains and collect the list of variable names
			int counter = 0;
			for(Entry<String, ASTNode> e: compnode.getVarBindings().entrySet()){
				constrnames.add(e.getKey());
				result.appendFragment(engine.compile(e.getValue(), CodeType.R));
				result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,domain" + counter + ")=new java.util.ArrayList<CompilerRuntime.Element>(((CompilerRuntime.Enumerable)evalStack.pop()).enumerate());\n");
				counter++;
			}	
			//iterate
			
			//open for loops
			for(int i = 0; i < constrnames.size(); i++){
				String var = "@domain" + i + "@";
				String cvar = "@c" + i + "@";
				result.appendLine("for(@decl(int,c" + i + ")=0; " + cvar + " < " + var + ".size(); " + cvar + "++){\n");
			}
			
			result.appendLine("localStack.pushLayer();\n");
			
			for(int i = 0; i < constrnames.size(); i++){
				result.appendLine("localStack.put(\"" + constrnames.get(i) + "\", @domain" + i + "@.get(@c" + i + "@));\n");
			}
			
			if(guard == null){
				result.appendFragment(engine.compile(compnode.getListFunction(), CodeType.R));
				result.appendLine("@list@.add((CompilerRuntime.Element)evalStack.pop());\n");
			}
			else{
				result.appendFragment(guard);
				result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
				result.appendFragment(engine.compile(compnode.getListFunction(), CodeType.R));
				result.appendLine("@list@.add((CompilerRuntime.Element)evalStack.pop());\n");
				result.appendLine("}\n");
			}
			
			result.appendLine("localStack.popLayer();\n");
			
			//close for loops
			for(int i = 0; i < constrnames.size(); i++){
				result.appendLine("}\n");
			}
			result.appendLine("evalStack.push(new plugins.ListPlugin.ListElement(@list@));\n");
		}
		catch(EngineException exc){
			throw new CompilerException(exc);
		}
	}

}
