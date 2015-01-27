package org.coreasm.testing.modules;

import org.coreasm.testing.TestingHelperModule;

public class BooleanTestingModule implements TestingHelperModule{

	@Override
	public String modifyCode(String code) {
		return code.replaceAll("CompilerRuntime.BooleanElement", "BooleanMock");
	}

	@Override
	public String getCodeBlock() {
		String result = "";
		result += "class BooleanMock{\n";
		result += "\tpublic static final BooleanMock TRUE = new BooleanMock(true);\n";
		result += "\tpublic static final BooleanMock FALSE = new BooleanMock(false);\n";
		result += "\tprivate boolean value;\n";
		result += "\tpublic BooleanMock(boolean v){\n";
		result += "\t\tvalue = v;\n";
		result += "\t}\n";
		result += "\tpublic boolean equals(Object o){\n";
		result += "\t\tif(o instanceof BooleanMock){\n";
		result += "\t\t\treturn ((BooleanMock)o).value == value;\n";
		result += "\t\t}\n";
		result += "\t\treturn false;\n";
		result += "\t}\n";
		result += "}\n";
		return result;
	}

}
