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
 * Represents an universe
 * @author Markus Brenner
 *
 */
public class UniverseEntry implements LibraryEntry {
	private String name;
	private String[] elements;
	
	/**
	 * Creates a new universe entry
	 * @param name The name of the final universe
	 * @param elements The elements of the universe
	 */
	public UniverseEntry(String name, String[] elements) {
		this.name = name;
		this.elements = elements;
	}

	@Override
	public void writeFile() throws LibraryEntryException {
		CompilerOptions options = CoreASMCompiler.getEngine().getOptions();
		File file = new File(options.tempDirectory + "\\plugins\\SignaturePlugin\\Universe_" + name + ".java");
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
				+ "public class Universe_" + name + " extends CompilerRuntime.UniverseElement{\n"
						+ "public Universe_" + name + "(){\n";
		result += "try{\n";
		result += "CompilerRuntime.MapFunction f = null;\n";
		result += "plugins.SignaturePlugin.EnumerationElement e = null;\n";
		for(String s : elements){
			result += "e = new plugins.SignaturePlugin.EnumerationElement(\"" + s + "\");\n";
			result += "this.setValue(e, CompilerRuntime.BooleanElement.TRUE);\n";
			result += "f = new CompilerRuntime.MapFunction();\n";
			result += "f.setValue(CompilerRuntime.ElementList.NO_ARGUMENT, e);\n";
			result += "f.setFClass(CompilerRuntime.FunctionElement.FunctionClass.fcStatic);\n";           
			result += "CompilerRuntime.RuntimeProvider.getRuntime().getStorage().addFunction(\"" + s + "\", f);\n";
		}
		result += "}catch(Exception exc){\nthrow new Error(\"initialization failed\");\n}\n";
		result += "}\n}\n";
		
		return result;
	}

	@Override
	public String getFullName() {
		return "plugins.SignaturePlugin.Universe_" + name;
	}

}
