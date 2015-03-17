package org.coreasm.compiler.classlibrary;

/**
 * The type of a library entry.
 * The compiler uses the library entry type to determine
 * the location of the entry in the final compilation unit.
 * Plugin classes should only use {@link #STATIC} and {@link #DYNAMIC}.
 * @author Spellmaker
 *
 */
public enum LibraryEntryType {
	/**
	 * A library entry located in the root of the compilation unit.
	 * Should only rarely be used, as classes in the root cannot
	 * be addressed by other classes
	 */
	BASE,
	/**
	 * A runtime entry is located in the runtime package of
	 * the compilation unit. Should not be used for plugin files.
	 */
	RUNTIME,
	/**
	 * A static plugin entry, located in the plugin specific subpackage of the static package.
	 * A static entry is a file, which is valid for all specifications using
	 * the plugin.
	 */
	STATIC,
	/**
	 * A dynamic plugin entry, located in the plugin specific subpackage of the dynamic package.
	 * A dynamic entry is a file, which is valid for only this exact specification
	 */
	DYNAMIC,
	/**
	 * A rule entry, located in the rule package
	 */
	RULE
}
