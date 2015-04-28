package org.coreasm.testing.value;

import org.coreasm.testing.TestingHelperModule;

public class LocationModule extends TestingHelperModule {

	@Override
	public String modifyCode(String code) {
		return code.replace("CompilerRuntime.Location", "Location");
	}

	@Override
	public String getCodeBlock() {
		String result = "";
		result += "class Location{\n";
		result += "\tpublic String name;\n";
		result += "\tpublic java.util.List<CompilerRuntime.Element> args;\n";
		result += "\tpublic Location(String name, java.util.List<CompilerRuntime.Element> args){\n";
		result += "\t\tthis.name = name;\n";
		result += "\t\tthis.args = new java.util.ArrayList<CompilerRuntime.Element>(args);\n";
		result += "\t}\n";
		result += "}\n";
		return result;
	}

}
