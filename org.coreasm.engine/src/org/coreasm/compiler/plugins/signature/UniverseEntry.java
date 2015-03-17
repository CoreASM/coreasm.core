package org.coreasm.compiler.plugins.signature;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.classlibrary.MemoryInclude;

/**
 * Represents an universe
 * @author Markus Brenner
 *
 */
public class UniverseEntry extends MemoryInclude {
	private String name;
	private String[] elements;
	
	/**
	 * Creates a new universe entry
	 * @param name The name of the final universe
	 * @param elements The elements of the universe
	 * @param engine The compiler engine supervising the compilation process
	 */
	public UniverseEntry(String name, String[] elements, CompilerEngine engine) {
		super(engine, "Universe_" + name, "SignaturePlugin", LibraryEntryType.DYNAMIC);
		this.name = "Universe_" + name;
		this.elements = elements;
	}

	protected String buildContent(String entryName) {
		String result = "";
		
		result = "package " + getPackage(entryName) + ";\n"
				+ "public class " + name + " extends " + runtimePkg() + ".UniverseElement{\n"
						+ "public " + name + "(){\n";
		result += "try{\n";
		result += runtimePkg() + ".MapFunction f = null;\n";
		result += engine.getPath().pluginStaticPkg() + ".SignaturePlugin.EnumerationElement e = null;\n";
		for(String s : elements){
			result += "e = new " + engine.getPath().pluginStaticPkg() + ".SignaturePlugin.EnumerationElement(\"" + s + "\");\n";
			result += "this.setValue(e, " + runtimePkg() + ".BooleanElement.TRUE);\n";
			result += "f = new " + runtimePkg() + ".MapFunction();\n";
			result += "f.setValue(" + runtimePkg() + ".ElementList.NO_ARGUMENT, e);\n";
			result += "f.setFClass(" + runtimePkg() + ".FunctionElement.FunctionClass.fcStatic);\n";           
			result += engine.getPath().runtimeProvider() + ".getStorage().addFunction(\"" + s + "\", f);\n";
		}
		result += "}catch(Exception exc){\nthrow new Error(\"initialization failed\");\n}\n";
		result += "}\n}\n";
		
		return result;
	}
}
