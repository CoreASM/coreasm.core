package org.coreasm.compiler.classlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.coreasm.compiler.CompilerEngine;

public abstract class MemoryInclude extends LibraryEntry {
	private String targetName;
	private String sourcePlugin;
	private LibraryEntryType type;
	protected CompilerEngine engine;
	private BufferedReader reader;
	
	protected abstract String buildContent(String entryName) throws Exception;
	
	protected String runtimePkg(){
		return engine.getPath().runtimePkg();
	}
	
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
