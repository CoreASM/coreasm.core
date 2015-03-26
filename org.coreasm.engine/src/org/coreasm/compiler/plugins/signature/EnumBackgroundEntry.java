package org.coreasm.compiler.plugins.signature;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.classlibrary.MemoryInclude;

/**
 * Represents an enumerated background as a library entry
 * @author Markus Brenner
 *
 */
public class EnumBackgroundEntry extends MemoryInclude {
	private String name;
	private String[] elements;
	
	/**
	 * Builds a new enum background
	 * @param name The name of the enum
	 * @param elements The elements of the enum
	 * @param engine The compiler engine supervising the compilation process
	 */
	public EnumBackgroundEntry(String name, String[] elements, CompilerEngine engine){
		super(engine, "EnumBackground_" + name, "SignaturePlugin", LibraryEntryType.DYNAMIC);
		this.name = "EnumBackground_" + name;
		this.elements = elements;
	}

	protected String buildContent(String entryName) {
		String result = "";
		
		result = "package " + getPackage(entryName) + ";\n"
				+ "public class " + name + " extends " + engine.getPath().pluginStaticPkg() + ".SignaturePlugin.EnumerationBackgroundElement{\n"
						+ "public " + name + "() throws Exception{\n";
		//result += "super(new java.util.ArrayList<plugins.SignaturePlugin.EnumerationElement>());\n";
		//result += "try{\n";
		result += "java.util.List<" + engine.getPath().pluginStaticPkg() + ".SignaturePlugin.EnumerationElement> list = new java.util.ArrayList<" + engine.getPath().pluginStaticPkg() + ".SignaturePlugin.EnumerationElement>();\n";
		result += engine.getPath().pluginStaticPkg() + ".SignaturePlugin.EnumerationElement e = null;\n";
		result += "" + runtimePkg() + ".MapFunction f = null;\n";
		for(String s : elements){
			result += "e = new " + engine.getPath().pluginStaticPkg() + ".SignaturePlugin.EnumerationElement(\"" + s + "\");\n";
			result += "list.add(e);\n";
			result += "f = new " + runtimePkg() + ".MapFunction();\n";
			result += "f.setValue(" + runtimePkg() + ".ElementList.NO_ARGUMENT, e);\n";
			result += "f.setFClass(" + runtimePkg() + ".FunctionElement.FunctionClass.fcStatic);\n";           
			result += "e.setBackground(\"" + name + "\");\n";
			result += engine.getPath().runtimeProvider() + ".getStorage().addFunction(\"" + s + "\", f);\n";
			result += "\n//next entry\n";
		}
		
		result += "this.setMembers(list);\n";
		//result += "}catch(Exception exc){}\n";
		
		result += "}\n}\n";
		
		return result;
	}
}
