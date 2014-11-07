package org.coreasm.compiler.plugins.signature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.coreasm.compiler.CompilerOptions;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Represents an enumerated background as a library entry
 * @author Markus Brenner
 *
 */
public class EnumBackgroundEntry implements LibraryEntry {
	private String name;
	private String[] elements;
	
	/**
	 * Builds a new enum background
	 * @param name The name of the enum
	 * @param elements The elements of the enum
	 */
	public EnumBackgroundEntry(String name, String[] elements){
		this.name = name;
		this.elements = elements;
	}
	
	@Override
	public void writeFile() throws LibraryEntryException {
		CompilerOptions options = CoreASMCompiler.getEngine().getOptions();
		File file = new File(options.tempDirectory + "\\plugins\\SignaturePlugin\\EnumBackground_" + name + ".java");
		File directory = new File(options.tempDirectory + "\\plugins\\SignaturePlugin\\");
		
		if(file.exists()) throw new LibraryEntryException(new Exception("file already exists"));
		
		BufferedWriter bw = null;
		
		directory.mkdirs();
		try {
			file.createNewFile();
		
			bw = new BufferedWriter(new FileWriter(file));
		
			bw.write(this.generate());
		} 
		catch (IOException e) {
			throw new LibraryEntryException(e);
		} 
		finally{
			try{
				bw.close();
			}
			catch(IOException e){
			}
		}
	}

	private String generate() {
		String result = "";
		
		result = "package plugins.SignaturePlugin;\n"
				+ "public class EnumBackground_" + name + " extends plugins.SignaturePlugin.EnumerationBackgroundElement{\n"
						+ "public EnumBackground_" + name + "() throws Exception{\n";
		//result += "try{\n";
		result += "java.util.List<plugins.SignaturePlugin.EnumerationElement> list = new java.util.ArrayList<plugins.SignaturePlugin.EnumerationElement>();\n";
		result += "plugins.SignaturePlugin.EnumerationElement e = null;\n";
		result += "CompilerRuntime.MapFunction f = null;\n";
		for(String s : elements){
			result += "e = new plugins.SignaturePlugin.EnumerationElement(\"" + s + "\");\n";
			result += "list.add(e);\n";
			result += "f = new CompilerRuntime.MapFunction();\n";
			result += "f.setValue(CompilerRuntime.ElementList.NO_ARGUMENT, e);\n";
			result += "f.setFClass(CompilerRuntime.FunctionElement.FunctionClass.fcStatic);\n";           
			result += "e.setBackground(\"" + name + "\");\n";
			result += "CompilerRuntime.RuntimeProvider.getRuntime().getStorage().addFunction(\"" + s + "\", f);\n";
			result += "\n//next entry\n";
		}
		
		result += "this.setMembers(list);\n";
		//result += "}catch(Exception exc){}\n";
		
		result += "}\n}\n";
		
		return result;
	}

	@Override
	public String getFullName() {
		return "plugins.SignaturePlugin.EnumBackground_" + name;
	}

}
