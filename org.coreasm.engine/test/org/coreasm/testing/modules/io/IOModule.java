package org.coreasm.testing.modules.io;

import org.coreasm.engine.plugins.io.IOPlugin;
import org.coreasm.testing.TestingHelperModule;

public class IOModule extends TestingHelperModule {
	public IOModule(){
		moduleFile = null;
	}
	
	@Override
	public String modifyCode(String code){
		return code.replace("plugins.IOPlugin.IOPlugin.OUTPUT_FUNC_LOC", "new Location(\"" + IOPlugin.OUTPUT_FUNC_NAME + "\", new java.util.ArrayList<Element>())").
				replace("plugins.IOPlugin.IOPlugin.PRINT_ACTION", "\"" + IOPlugin.PRINT_ACTION + "\"");
	}
}
