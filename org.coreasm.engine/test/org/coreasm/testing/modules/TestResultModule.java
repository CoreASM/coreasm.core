package org.coreasm.testing.modules;

import org.coreasm.testing.TestingHelperModule;

public class TestResultModule implements TestingHelperModule {

	@Override
	public String modifyCode(String code) {
		return code;
	}

	@Override
	public String getCodeBlock() {
		String result = "";
		
		result += "class TestResult{\n";
		result += "\tpublic CompilerRuntime.Element element;\n";
		result += "\tpublic CompilerRuntime.Location location;\n";
		result += "\tpublic CompilerRuntime.UpdateList ulist;\n";
		result += "}\n";
		
		return result;
	}

}
