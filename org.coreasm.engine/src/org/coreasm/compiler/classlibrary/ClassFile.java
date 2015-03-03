package org.coreasm.compiler.classlibrary;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.AbstractLibraryEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Represents a basic class created during the compilation process.
 * Provides the option to add interfaces, a parent class and some more
 * to this class, then generates the class skeleton and adds the body code.
 * Use of more specialized LibraryEntries is recommended in most cases.
 * @author Markus Brenner
 *
 */
public class ClassFile extends AbstractLibraryEntry{	
	private String className;
	private String packageName;
	
	private CodeFragment classBody;
	private ArrayList<String> imports;
	private String extend;
	private ArrayList<String> interfaces;
	private CompilerEngine engine;
	
	/**
	 * Creates a new ClassFile with the given class name and the given
	 * package path.
	 * @param className The name of the class
	 * @param packageName The package of the class
	 */
	public ClassFile(String className, String packageName, CompilerEngine engine){
		this.className = className;
		this.packageName = packageName;
		this.classBody = null;
		this.imports = new ArrayList<String>();
		this.interfaces = new ArrayList<String>();
		this.extend = null;
		this.engine = engine;
	}
	
	/**
	 * Adds an import to the class
	 * @param resource The import
	 */
	public void addImport(String resource){
		if(!imports.contains(resource)) this.imports.add(resource);
	}
	/**
	 * Sets the base class of this class
	 * @param e The base class
	 */
	public void setExtend(String e){
		this.extend = e;
	}
	/**
	 * Sets the body code of this class
	 * @param cf A code fragment for the body
	 */
	public void setBody(CodeFragment cf){
		this.classBody = cf;
	}
	/**
	 * Provides access to the body code object 
	 * @return The body code
	 */
	public CodeFragment getBody(){
		return this.classBody;
	}
	/**
	 * Adds an interface to this class
	 * @param i An interface
	 */
	public void addInterface(String i){
		if(!interfaces.contains(i)) this.interfaces.add(i);
	}
	
	@Override
	public String getFullName(){
		if(!packageName.equals("")) return packageName + "." + className;
		return className;
	}

	@Override
	protected File getFile() {
		File tempDirectory = engine.getOptions().tempDirectory;

		if(packageName.equals("")){
			return new File(tempDirectory, className + ".java");
		}
		else{
			String pkg = packageName.replace(".", File.separator); //create the path from the packagename
			return new File(tempDirectory, pkg + File.separator + className + ".java");
		}
	}
	
	@Override
	protected String generateContent() throws LibraryEntryException {
		String s = "";
		
		if(!packageName.equals("")) s = s + ("package " + packageName + ";\n\n");
		for(Iterator<String> it = imports.iterator(); it.hasNext();){
			s = s + ("import " + it.next() + ";\n");
		}
		
		s = s + ("\n");
		
		s = s + ("public class " + className);
		

		if(extend != null) s = s + " extends " + extend;
		
		if(interfaces.size() > 0) s = s + (" implements ");
		for(Iterator<String> it = interfaces.iterator(); it.hasNext(); ){
			s = s + (it.next());
			if(it.hasNext()) s = s + (", ");
		}
		
		
		s = s + ("{\n");
		try{
			engine.getVarManager().startContext();
			s += classBody.generateCode(engine);
			engine.getVarManager().endContext();
		}
		catch(Exception e){
			throw new LibraryEntryException(e);
		}
		
		s = s + ("}");
			
		return s;
	}
	
	
}
