package org.coreasm.compiler.components.classlibrary;

import java.io.Closeable;

import org.coreasm.compiler.interfaces.CompilerPlugin;

/**
 * An entry for the compiler class library.
 * A LibraryEntry is an in-memory representation of a file
 * in the final compilation unit.
 * This can be a file, which will be completely constructed
 * at compile time (see {@link MemoryInclude}) or a modification
 * of an already existing file (see {@link JarInclude}).
 * @author Spellmaker
 *
 */
public abstract class LibraryEntry implements Closeable{
	/**
	 * Obtains the name of this LibraryEntry
	 * The name of a LibraryEntry representing a class should
	 * always be the name of the class without any packages
	 * @return The name the entry
	 */
	public abstract String getName();
	/**
	 * Obtains the source of this entry.
	 * The source of a plugin provided class should
	 * always be the name of the plugin as returned by {@link CompilerPlugin#getName()}
	 * @return The source of the entry
	 */
	public abstract String getSource();
	/**
	 * Obtains the type of this entry
	 * @return The type of the entry
	 */
	public abstract LibraryEntryType getType();
	/**
	 * Opens the entry for reading.
	 * Must be called before any calls to {@link #readLine()}.
	 * In case of file based entries this method will open up streams
	 * which can cause resource leaks. {@link #close()} has to be called
	 * to free up resources after use.
	 * @param entryName The full name this entryName
	 * @throws Exception If an error occured
	 */
	public abstract void open(String entryName) throws Exception;
	/**
	 * Reads a line from this library entry.
	 * The line is not terminated by a line terminator
	 * @return A line from the library entry
	 * @throws Exception If an error occurred
	 */
	public abstract String readLine() throws Exception;	
	
	/**
	 * Extracts the package of a class from the complete className
	 * @param className The className including a package specification
	 * @return The package of the class
	 */
	protected String getPackage(String className){
		int pos = className.lastIndexOf(".");
		if(pos <= 0) return "";
		return className.substring(0, pos);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof LibraryEntry){
			LibraryEntry l = (LibraryEntry) o;
			return getName().equals(l.getName()) && getSource().equals(l.getSource()) && getType().equals(l.getType());
		}
		return false;
	}
	
	@Override
	public String toString(){
		return "[" + this.getClass().getName() + ":" + getName() + ":" + getSource() + ":" + getType() + "]";
	}
}
