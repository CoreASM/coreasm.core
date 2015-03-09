package org.coreasm.compiler.classlibrary;

import java.util.ArrayList;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.ElementAlreadyExistsException;
import org.coreasm.compiler.exception.LibraryEntryException;
/**
 * Advanced LibraryEntry representing an enum.
 * An enum consists of a name, a package and a list of elements.
 * @author Markus Brenner
 *
 */
public class EnumFile extends MemoryInclude{
	private String enumName;
	private ArrayList<String> elements;
	
	/**
	 * Creates a new, empty enum with the given name and package
	 * @param enumName the name of the enum
	 * @param packageName the package of the enum
	 */
	public EnumFile(String enumName, LibraryEntryType type, String sourcePlugin, CompilerEngine engine){
		super(engine, enumName, sourcePlugin, type);
		this.enumName = enumName;
		this.elements = new ArrayList<String>();
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
	protected String buildContent(String entryName) throws LibraryEntryException {
		String s = "";
		
		s = s + ("package " + getPackage(entryName) + ";\n\n");
		
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
