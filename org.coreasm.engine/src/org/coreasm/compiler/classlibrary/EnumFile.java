package org.coreasm.compiler.classlibrary;

import java.io.File;
import java.util.ArrayList;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.AbstractLibraryEntry;
import org.coreasm.compiler.exception.ElementAlreadyExistsException;
import org.coreasm.compiler.exception.LibraryEntryException;
/**
 * Advanced LibraryEntry representing an enum.
 * An enum consists of a name, a package and a list of elements.
 * @author Markus Brenner
 *
 */
public class EnumFile extends AbstractLibraryEntry{
	private String enumName;
	private String packageName;
	private ArrayList<String> elements;
	private CompilerEngine engine;
	
	/**
	 * Creates a new, empty enum with the given name and package
	 * @param enumName the name of the enum
	 * @param packageName the package of the enum
	 */
	public EnumFile(String enumName, String packageName, CompilerEngine engine){
		this.enumName = enumName;
		this.packageName = packageName;
		this.elements = new ArrayList<String>();
		this.engine = engine;
	}
	
	public String getFullName(){
		if(!packageName.equals("")) return packageName + "." + enumName;
		return enumName;
	}
	
	/**
	 * Adds an element to the enum.
	 * If the enum already holds the element, an exception will be thrown
	 * @param e The element
	 * @throws ElementAlreadyExistsException If the enum already contains e
	 */
	public void addElement(String e) throws ElementAlreadyExistsException{
		if(this.elements.contains(e)) throw new ElementAlreadyExistsException();
		this.elements.add(e);
	}

	@Override
	protected File getFile() {
		if(packageName.equals(""))
			return new File(engine.getOptions().tempDirectory + File.separator + enumName + ".java");
		else
			return new File(engine.getOptions().tempDirectory + File.separator + packageName.replace(".", File.separator) + File.separator + enumName + ".java");
	}

	@Override
	protected String generateContent() throws LibraryEntryException {
		String s = "";
		
		if(!packageName.equals("")) s = s + ("package " + packageName + ";\n\n");
		
		s = s + ("\n");
		
		s = s + ("public enum " + enumName);
		s = s + ("{\n");

		for(int i = 0; i < elements.size(); i++){
			s = s + elements.get(i);
			if(i != elements.size() - 1) s = s + ",";
		}
		
		s = s + ("}");
			
		return s;
	}
}
