package org.coreasm.testing.modules.string;

import org.coreasm.testing.TestingHelperModule;

public class StringModule extends TestingHelperModule {
	public StringModule(){
		moduleFile = "String.mod";
	}
	
	@Override
	public String modifyCode(String code){
		return code.replace("plugins.StringPlugin.StringElement", "StringMock");
	}
}
