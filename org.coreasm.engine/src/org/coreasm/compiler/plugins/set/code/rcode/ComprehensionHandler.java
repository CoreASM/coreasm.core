package org.coreasm.compiler.plugins.set.code.rcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.set.SetCompNode;
import org.coreasm.engine.plugins.set.TrueGuardNode;

/**
 * Handles set comprehension
 * @author Spellmaker
 *
 */
public class ComprehensionHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		//set comprehension was changed in a newer update of CoreASM.
		//there are no longer two different set comprehension node types
		

		//evaluates a set comprehension of the form
		//{id | id in value with guard}
		//{id is exp | id1 in value1, ... idn in value n with guard}
		//where the guard is optional.
		//in the first case, the exp is simply id
		
		SetCompNode cnode = (SetCompNode) node;
		
		//guard might be non existent, so initialize it
		CodeFragment guard = null;
		//optimization: the true guard is always true anyway, so if it is existent, leave it out
		if(!(cnode.getGuard() instanceof TrueGuardNode)) guard = engine.compile(cnode.getGuard(), CodeType.R);

		List<String> constrnames = new ArrayList<String>();
		
		result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>();\n");
		try{
			//evaluate all constrainer domains and collect the list of variable names
			int counter = 0;
			for(Entry<String, ASTNode> e: cnode.getVarBindings().entrySet()){
				constrnames.add(e.getKey());
				result.appendFragment(engine.compile(e.getValue(), CodeType.R));
				result.appendLine("@decl(java.util.List<@RuntimePkg@.Element>,domain" + counter + ")=new java.util.ArrayList<@RuntimePkg@.Element>(((@RuntimePkg@.Enumerable)evalStack.pop()).enumerate());\n");
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
				result.appendFragment(engine.compile(cnode.getSetFunction(), CodeType.R));
				result.appendLine("@list@.add((@RuntimePkg@.Element)evalStack.pop());\n");
			}
			else{
				result.appendFragment(guard);
				result.appendLine("if(evalStack.pop().equals(@RuntimePkg@.BooleanElement.TRUE)){\n");
				result.appendFragment(engine.compile(cnode.getSetFunction(), CodeType.R));
				result.appendLine("@list@.add((@RuntimePkg@.Element)evalStack.pop());\n");
				result.appendLine("}\n");
			}
			
			result.appendLine("localStack.popLayer();\n");
			
			//close for loops
			for(int i = 0; i < constrnames.size(); i++){
				result.appendLine("}\n");
			}
			String setelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "SetElement", "SetPlugin");
			result.appendLine("evalStack.push(new " + setelement + "(@list@));\n");
		}
		catch(EngineException exc){
			throw new CompilerException(exc);
		}
	}

}
