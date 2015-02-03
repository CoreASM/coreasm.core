package org.coreasm.testing.modules;

import org.coreasm.testing.TestingHelperModule;

public class ElementModule extends TestingHelperModule {

	@Override
	public String modifyCode(String code) {
		return code.replace("CompilerRuntime.Element", "Element");
	}

	@Override
	public String getCodeBlock() {
		String result = "";
		result += "class Element{\n";
		result += "}\n";
		return result;
	}

}
