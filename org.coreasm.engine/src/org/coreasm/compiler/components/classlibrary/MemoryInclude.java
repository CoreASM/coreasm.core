package org.coreasm.compiler.components.classlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.paths.CompilerPathConfig;

/**
 * A compile-time constructed library entry.
 * MemoryInclude Entries are constructed in memory
 * at compile-time from provided information.
 * @author Spellmaker
 *
 */
public abstract class MemoryInclude extends LibraryEntry {
	private String targetName;
	private String sourcePlugin;
	private LibraryEntryType type;
	protected CompilerEngine engine;
	private BufferedReader reader;
	
	/**
	 * Generates the content for this memory include.
	 * @param entryName The complete name of this entry in the compilation unit
	 * @return A string with the content of the include
	 * @throws Exception If an error occured
	 */
	protected abstract String buildContent(String entryName) throws Exception;
	
	/**
	 * Quick access to the runtime package path of the {@link CompilerPathConfig}
	 * @return The runtime package of the compilation unit
	 */
	protected String runtimePkg(){
		return engine.getPath().runtimePkg();
	}
	
	/**
	 * Constructs a new memory entry.
	 * Must be called via super from child class constructors to initialize the variables.
	 * @param engine The compiler engine supervising the compilation process
	 * @param targetName The name of the include (class name, if it is a class file)
	 * @param sourcePlugin The plugin name of the contributing plugin
	 * @param type The entry type of the include
	 */
	public MemoryInclude(CompilerEngine engine, String targetName, String sourcePlugin, LibraryEntryType type){
		this.targetName = targetName;
		this.sourcePlugin = sourcePlugin;
		this.type = type;
		this.engine = engine;
	}
	
	@Override
	public String getName() {
		return this.targetName;
	}

	@Override
	public String getSource() {
		return this.sourcePlugin;
	}

	@Override
	public LibraryEntryType getType() {
		return this.type;
	}

	@Override
	public void open(String entryName) throws Exception {
		reader = new BufferedReader(new StringReader(buildContent(entryName)));
	}

	@Override
	public String readLine() throws IOException {
		return reader.readLine();
	}
	
	@Override
	public void close() throws IOException{
		reader.close();
	}

}
