package org.coreasm.engine.plugins.io;

import org.coreasm.engine.absstorage.MapFunction;


/** 
 * @see org.coreasm.engine.plugins.io.IOPlugin
 */
public class FileWriteFunctionElement extends MapFunction {


	public FileWriteFunctionElement() {
		setFClass(FunctionClass.fcOut);
	}
}
