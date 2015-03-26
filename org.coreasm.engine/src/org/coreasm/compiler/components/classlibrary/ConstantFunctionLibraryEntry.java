package org.coreasm.compiler.components.classlibrary;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * A library entry representing a constant function.
 * The value of the constant function is known at compile 
 * time and won't be modified later.
 * The value string can be any piece of code used in java as a value.
 * The plugin developer has to take care to ensure, that the value is still constant.
 * @author Spellmaker
 *
 */
public class ConstantFunctionLibraryEntry extends MemoryInclude {
	private CompilerEngine engine;
	private String name;
	private String value;
	
	/**
	 * Constructs a new constant function entry.
	 * @param name The name of the constant function
	 * @param value The value of the constant function
	 * @param plugin The plugin name of the plugin generating this function
	 * @param type A valid library entry type
	 * @param engine The compiler engine supervising the compilation process
	 */
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
