package org.coreasm.testing.modules.kernel;

import org.coreasm.testing.TestingHelperModule;

public class UpdateModule extends TestingHelperModule {

	@Override
	public String modifyCode(String code) {
		return code.replace("CompilerRuntime.Update", "Update");
	}

	@Override
	public String getCodeBlock() {
		String result = "";
		result += "class Update{\n";
		result += "\tpublic Location loc;\n";
		result += "\tpublic String action;\n";
		result += "\tpublic Element value;\n";
		result += "\tpublic Update(CompilerRuntime.Location loc, CompilerRuntime.Element val, String action, CompilerRuntime.Element agent, Object scannerinfo){\n";
		result += "\t\tthis.loc = loc;\n";
		result += "\t\tthis.action = action;\n";
		result += "\t\tthis.value = val;\n";
		result += "\t}\n";
		result += "}\n";
		return result;
	}

}
