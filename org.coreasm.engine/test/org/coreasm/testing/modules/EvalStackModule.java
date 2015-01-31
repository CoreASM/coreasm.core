package org.coreasm.testing.modules;

import org.coreasm.testing.TestingHelperModule;

public class EvalStackModule implements TestingHelperModule {

	@Override
	public String modifyCode(String code) {
		return code.replace("CompilerRuntime.EvalStack", "EvalStack");
	}

	@Override
	public String getCodeBlock() {
		String res = "";
		res += "class EvalStack{\n";
		res += "\tprivate java.util.Stack<Object> stack;\n";
		res += "\tpublic EvalStack(){\n";
		res += "\t\tstack = new java.util.Stack<Object>();\n";
		res += "\t}\n";
		res += "\tpublic Object pop(){\n";
		res += "\t\treturn stack.pop();\n";
		res += "\t}\n";
		res += "\tpublic void push(Object o){\n";
		res += "\t\tstack.push(o);\n";
		res += "\t}\n";
		res += "\tpublic boolean isEmpty(){\n";
		res += "\t\treturn this.stack.isEmpty();\n";
		res += "\t}";
		res += "}\n";
		
		return res;
	}

}
