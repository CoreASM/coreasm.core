package org.coreasm.compiler.classlibrary;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.LibraryEntryException;

public class ConstantFunctionLibraryEntry extends MemoryInclude {
	private CompilerEngine engine;
	private String name;
	private String value;
	
	public ConstantFunctionLibraryEntry(String name, String value, String plugin, LibraryEntryType type, CompilerEngine engine){
		super(engine, "const_function_" + name, plugin, type);
		this.name = name;
		this.value = value;
		this.engine = engine;
	}

	@Override
	protected String buildContent(String entryName) throws LibraryEntryException {
		String s = "";
		
		s += "package " + getPackage(entryName) + ";\n";
		s += "public class const_function_" + name + " extends " + engine.getPath().runtimePkg() + ".FunctionElement{\n";
		s += "@Override\n";
		s += "public " + engine.getPath().runtimePkg() + ".Element getValue(java.util.List<? extends " + engine.getPath().runtimePkg() + ".Element> args){\n";
		s += "return " + value + ";\n";
		s += "}\n";
		s += "}";
		
		return s;
	}

}
