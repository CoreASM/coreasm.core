package org.coreasm.compiler.classlibrary;

import java.io.File;

import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.exception.LibraryEntryException;

public class ConstantFunctionLibraryEntry extends AbstractLibraryEntry {

	private String fname;
	private String pckg;
	private String value;
	
	public ConstantFunctionLibraryEntry(String name, String pckg, String value){
		this.fname = name;
		this.pckg = pckg;
		this.value = value;
	}
	
	@Override
	public String getFullName() {
		return pckg + "." + "const_function_" + fname;
	}

	@Override
	protected File getFile() {
		return new File(CoreASMCompiler.getEngine().getOptions().tempDirectory + "\\" + pckg.replace(".", "\\") + "\\" + "const_function_" + fname + ".java");
	}

	@Override
	protected String generateContent() throws LibraryEntryException {
		String s = "";
		
		s += "package " + pckg + ";\n";
		s += "public class const_function_" + fname + " extends CompilerRuntime.FunctionElement{\n";
		s += "@Override\n";
		s += "public CompilerRuntime.Element getValue(java.util.List<? extends CompilerRuntime.Element> args){\n";
		s += "return " + value + ";\n";
		s += "}\n";
		s += "}";
		
		return s;
	}

}
