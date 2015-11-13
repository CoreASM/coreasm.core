package org.coreasm.engine.plugins.io;


import java.io.IOException;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.plugins.list.ListElement;

import CompilerRuntime.CoreASMError;

/** 
 * Implements the <i>input</i> monitored function provided by IO Plugin.
 *   
 * @author  Roozbeh Farahbod
 * 
 * @see org.coreasm.engine.plugins.io.IOPlugin
 */
public class FileReadFunctionElement extends FunctionElement {

	private final IOPlugin plugin;
	
	/**
	 * Creates a new read function element with the given
	 * link to an IOPlugin.
	 *  
	 * @param ioPlugin the IOPlugin that created this object
	 * @see IOPlugin
	 */
	public FileReadFunctionElement(IOPlugin ioPlugin) {
		this.plugin = ioPlugin;
		this.setFClass(FunctionClass.fcMonitored);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.absstorage.FunctionElement#getValue(java.util.List)
	 */
	@Override
	public Element getValue(List<? extends Element> args) {
		String path;
		// get the message argument
		if (args.size() == 0)
			path = "";
		else// get the source file argument
			path = args.get(0).toString();
		ListElement lines = new ListElement();
		try {
			lines = this.plugin.readFromFile(path);
		}
		catch (IOException e) {
			throw new CoreASMError("Cannot read from file " + path);
		}

		return lines;

	}

}
