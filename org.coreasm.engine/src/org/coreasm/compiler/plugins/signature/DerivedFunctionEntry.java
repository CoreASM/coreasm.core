package org.coreasm.compiler.plugins.signature;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.classlibrary.MemoryInclude;
import org.coreasm.compiler.exception.LibraryEntryException;


/**
 * Represents a derived function as a library entry
 * @author Markus Brenner
 *
 */
public class DerivedFunctionEntry extends MemoryInclude{
	private String name;
	private String[] params;
	private CodeFragment body;
	
	/**
	 * Builds a new derived function entry
	 * @param name The name of the derived function
	 * @param params The parameters of the function
	 * @param body The body of the function
	 * @param engine The compiler engine supervising the compilation process
	 */
	public DerivedFunctionEntry(String name, String[] params, CodeFragment body, CompilerEngine engine){
		super(engine, "DerFunc_" + name, "SignaturePlugin", LibraryEntryType.DYNAMIC);
		this.name = "DerFunc_" + name;
		this.params = params;
		this.body = body;
	}
	@Override
	protected String buildContent(String entryName) throws LibraryEntryException {
		String result = "";
		
		result += "package " + getPackage(entryName) + ";\n";
		result += "public class " + name + " extends " + runtimePkg() + ".FunctionElement{\n";
		//result += "private CompilerRuntime.EvalStack evalStack;\n";
		//result += "private CompilerRuntime.LocalStack localStack;\n";
		result += "public " + runtimePkg() + ".Rule getUpdateResponsible(){return null;}\n";
		result += "public " + name + "() throws Exception{\n";
		//result += "evalStack = new CompilerRuntime.EvalStack();\n";
		//result += "localStack = new CompilerRuntime.LocalStack();\n";
		result += "setFClass(" + runtimePkg() + ".FunctionElement.FunctionClass.fcDerived);\n";
		result += "}\n";
		result += "@Override\n";
		result += "public " + runtimePkg() + ".Element getValue(java.util.List<? extends " + runtimePkg() + ".Element> args) {\n";
		result += "\t" + runtimePkg() + ".EvalStack evalStack = new " + runtimePkg() + ".EvalStack();\n";
		result += "\t" + runtimePkg() + ".LocalStack localStack = new " + runtimePkg() + ".LocalStack();\n";
		result += "java.util.Map<String, " + runtimePkg() + ".RuleParam> ruleparams = new java.util.HashMap<String, " + runtimePkg() + ".RuleParam>();\n";
		result += "if(args.size() != " + params.length + ") return " + runtimePkg() + ".Element.UNDEF;\n";
		
		for(int i = 0; i < params.length; i++){
			result += "localStack.put(\"" + params[i] + "\", args.get(" + i + "));\n";
		}
		
		result += "try{\n";
		
		try {
			result += body.generateCode(engine);
		} catch (CodeFragmentException e) {
			throw new LibraryEntryException(e);
		}
		
		result += "}catch(Exception exc){return " + runtimePkg() + ".Element.UNDEF;\n}\n";
		
		result += "return (" + runtimePkg() + ".Element)evalStack.pop();\n";
		
		result += "}\n";
		result += "}\n";
		
		
		return result;
	}
	
	

}
