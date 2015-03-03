package org.coreasm.compiler.plugins.signature;

import java.io.File;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.AbstractLibraryEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;
import org.coreasm.compiler.exception.LibraryEntryException;


/**
 * Represents a derived function as a library entry
 * @author Markus Brenner
 *
 */
public class DerivedFunctionEntry extends AbstractLibraryEntry{
	private String name;
	private String[] params;
	private CodeFragment body;
	private CompilerEngine engine;
	
	/**
	 * Builds a new derived function entry
	 * @param name The name of the derived function
	 * @param params The parameters of the function
	 * @param body The body of the function
	 */
	public DerivedFunctionEntry(String name, String[] params, CodeFragment body, CompilerEngine engine){
		this.name = name;
		this.params = params;
		this.body = body;
		this.engine = engine;
	}
	
	@Override
	public String getFullName() {
		return "plugins.SignaturePlugin.DerFunc_" + name;
	}

	@Override
	protected File getFile() {
		String p = engine.getOptions().tempDirectory + "\\plugins\\SignaturePlugin\\DerFunc_" + name + ".java";
		return new File(p.replace("\\", File.separator));
	}

	@Override
	protected String generateContent() throws LibraryEntryException {
		String result = "";
		
		result += "package plugins.SignaturePlugin;\n";
		result += "public class DerFunc_" + name + " extends CompilerRuntime.FunctionElement{\n";
		//result += "private CompilerRuntime.EvalStack evalStack;\n";
		//result += "private CompilerRuntime.LocalStack localStack;\n";
		result += "public CompilerRuntime.Rule getUpdateResponsible(){return null;}\n";
		result += "public DerFunc_" + name + "() throws Exception{\n";
		//result += "evalStack = new CompilerRuntime.EvalStack();\n";
		//result += "localStack = new CompilerRuntime.LocalStack();\n";
		result += "setFClass(CompilerRuntime.FunctionElement.FunctionClass.fcDerived);\n";
		result += "}\n";
		result += "@Override\n";
		result += "public CompilerRuntime.Element getValue(java.util.List<? extends CompilerRuntime.Element> args) {\n";
		result += "\tCompilerRuntime.EvalStack evalStack = new CompilerRuntime.EvalStack();\n";
		result += "\tCompilerRuntime.LocalStack localStack = new CompilerRuntime.LocalStack();\n";
		result += "java.util.Map<String, CompilerRuntime.RuleParam> ruleparams = new java.util.HashMap<String, CompilerRuntime.RuleParam>();\n";
		result += "if(args.size() != " + params.length + ") return CompilerRuntime.Element.UNDEF;\n";
		
		for(int i = 0; i < params.length; i++){
			result += "localStack.put(\"" + params[i] + "\", args.get(" + i + "));\n";
		}
		
		result += "try{\n";
		
		try {
			result += body.generateCode(engine);
		} catch (CodeFragmentException e) {
			throw new LibraryEntryException(e);
		}
		
		result += "}catch(Exception exc){return CompilerRuntime.Element.UNDEF;\n}\n";
		
		result += "return (CompilerRuntime.Element)evalStack.pop();\n";
		
		result += "}\n";
		result += "}\n";
		
		
		return result;
	}
	
	

}
