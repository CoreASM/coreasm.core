package org.coreasm.compiler.plugins.signature;

import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.classlibrary.MemoryInclude;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;

/**
 * Represents a general function entry with a type, name and possible starting values
 * @author Markus Brenner
 *
 */
public class FunctionEntry extends MemoryInclude {
	private String name;
	private String fclass;
	private List<String> domain;
	private String range;
	private CodeFragment init;

	/**
	 * Builds a new function entry
	 * @param name The name of the final function
	 * @param fclass The function class
	 * @param domain The domain of the function type
	 * @param range The range of the function type
	 * @param init A code fragment generating the initial values for the function
	 */
	public FunctionEntry(String name, String fclass, List<String> domain, String range, CodeFragment init, CompilerEngine engine){
		super(engine, "Func_" + name, "SignaturePlugin", LibraryEntryType.DYNAMIC);
		this.name = "Func_" + name;
		this.fclass = fclass;
		this.domain = domain;
		this.range = range;
		this.init = init;
	}

	protected String buildContent(String entryName) throws CodeFragmentException {
		String result = "";
		
		result += "package " + getPackage(entryName) + ";\n";
		result += "public class " + name + " extends " + runtimePkg() + ".MapFunction{\n";
		
		result += "public " + name + "() throws Exception{\n";
			if(this.fclass != null) result += "this.setFClass(" + "FunctionClass.fc" + fclass + ");\n";
			result += "" + runtimePkg() + ".Signature sig = new " + runtimePkg() + ".Signature();\n";
			result += "java.util.List<String> list = new java.util.ArrayList<String>();\n";
			for(int i = 0; i < domain.size(); i++){
				result += "list.add(\"" + domain.get(i) + "\");\n";
			}
			result += "sig.setDomain(list);\n";
			result += "sig.setRange(\"" + range + "\");\n";
			result += "this.setSignature(sig);\n";

			if(init != null){
				result += "" + runtimePkg() + ".EvalStack evalStack = new " + runtimePkg() + ".EvalStack();\n";
				result += "" + runtimePkg() + ".LocalStack localStack = new " + runtimePkg() + ".LocalStack();\n";
				result += "java.util.Map<String, " + runtimePkg() + ".RuleParam> ruleparams = new java.util.HashMap<String, " + runtimePkg() + ".RuleParam>();\n";
				result += init.generateCode(engine);
				result += "" + runtimePkg() + ".Element initValue = (" + runtimePkg() + ".Element) evalStack.pop();\n";
				if(this.domain.size() == 0){
					result += "try {\n";
					result += "    setValue(" + runtimePkg() + ".ElementList.NO_ARGUMENT, initValue);\n";
					result += "} catch (" + runtimePkg() + ".UnmodifiableFunctionException e) {}\n";
				}
				else{
					result += "if (initValue instanceof " + runtimePkg() + ".FunctionElement) {\n";
					result += "    " + runtimePkg() + ".FunctionElement map = (" + runtimePkg() + ".FunctionElement) initValue;\n";
					result += "    int dSize = " + domain.size() + ";\n";
					result += "    for (" + runtimePkg() + ".Location l: map.getLocations(\""+ name + "\")) {\n";
					result += "    	if (l.args.size() == dSize) {\n";
					result += "            try {\n";
					result += "                setValue(l.args, map.getValue(l.args));\n";
					result += "            } catch (" + runtimePkg() + ".UnmodifiableFunctionException e) {}\n";
					result += "    	} else {\n";
					result += "    		if (l.args.size() == 1 \n";
					result += "    				&& l.args.get(0) instanceof " + runtimePkg() + ".Enumerable \n";
					result += "    				&& ((" + runtimePkg() + ".Enumerable)l.args.get(0)).enumerate().size() == dSize) \n";
					result += "    		{\n";
					result += "                try {\n";
					result += "                    setValue(" + runtimePkg() + ".ElementList.create(((" + runtimePkg() + ".Enumerable)l.args.get(0)).enumerate()), map.getValue(l.args));\n";
					result += "                } catch (" + runtimePkg() + ".UnmodifiableFunctionException e) {}\n";
					result += "    		}\n";
					result += "    		else\n";
					result += "            	throw new " + runtimePkg() + ".EngineError(\"Initial value of function " + name + " does not match the function signature.\");\n";
					result += "    	}\n";
					result += "            \n";
					result += "    }        \n";                          
					result += "}\n";
				}
			}
		result += "}\n";
		result += "}\n";
		
		return result;
	}
}
