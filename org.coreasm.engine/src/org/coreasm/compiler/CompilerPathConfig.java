package org.coreasm.compiler;

import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.classlibrary.LibraryEntryType;

/**
 * Maps {@link LibraryEntry} instances to paths and provides package definitions.
 * The compilation units contains several different locations, in which plugins and
 * the kernel will place classes.
 * The path config determines the exact location of these places and should be used
 * by plugins to address other classes
 * @author Spellmaker
 *
 */
public abstract class CompilerPathConfig {
	/**
	 * Gets the definition of the root package
	 * @return The root package of the project
	 */
	public abstract String basePkg();
	/**
	 * Gets the definition of the runtime package
	 * @return The runtime package of the project
	 */
	public abstract String runtimePkg();
	/**
	 * Gets the definition of the static plugin package
	 * @return The static plugin package of the project
	 */
	public abstract String pluginStaticPkg();
	/**
	 * Gets the definition of the dynamic plugin package
	 * @return The dynamic plugin package of the project
	 */
	public abstract String pluginDynamicPkg();
	/**
	 * Gets the definition of the rule plugin package
	 * @return The rule package of the project
	 */
	public abstract String rulePkg();
	/**
	 * Gets the definition of the runtime provider 
	 * @return The runtime provider of the project
	 */
	public abstract String runtimeProvider();
	/**
	 * Maps a {@link LibraryEntry} to a fully specified class name.
	 * The default implementation should only be overwritten in very specific
	 * cases.
	 * @param entry A LibraryEntry
	 * @return The fully specified class name of the entry
	 */
	public String getEntryName(LibraryEntry entry){
		return getEntryName(entry.getType(), entry.getName(), entry.getSource());
	}
	/**
	 * Maps a {@link LibraryEntry} to a fully specified path.
	 * The default implementation should only be overwritten in very specific
	 * cases.
	 * @param entry A LibraryEntry
	 * @return The fully specified file name of the entry
	 */
	public String getEntryPath(LibraryEntry entry){
		return getEntryName(entry.getType(), entry.getName(), entry.getSource()).replace(".", "\\") + ".java";
	}
	/**
	 * Maps an assumed LibraryEntry to a fully specified class name
	 * @param type The type of the assumed entry
	 * @param entryName The name of the assumed entry
	 * @param source The source of the assumed entry
	 * @return A fully specified class name
	 */
	public String getEntryName(LibraryEntryType type, String entryName, String source){
		switch(type){
		case BASE:
			return basePkg() + ((basePkg() == "") ? "" : ".") + entryName;
		case DYNAMIC:
			return pluginDynamicPkg() + "." + source + "." + entryName;
		case RULE:
			return rulePkg() + "." + entryName;
		case RUNTIME:
			return runtimePkg() + "." + entryName;
		case STATIC:
			return pluginStaticPkg() + "." + source + "." + entryName;
		}
		return "";
	}
}
