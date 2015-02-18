package org.coreasm.testing.modules.kernel;

import org.coreasm.testing.TestingHelperModule;

public class UpdateListModule extends TestingHelperModule{

	@Override
	public String modifyCode(String code) {
		return code.replace("CompilerRuntime.UpdateList", "UpdateList");
	}

	@Override
	public String getCodeBlock() {
		String result = "";
		
		result += "class UpdateList extends java.util.ArrayList<Update>{\n";
		result += "private static final long serialVersionUID = 1L;\n";
		result += "\n";
		result += "public UpdateList(){\n";
		result += "super();\n";
		result += "}\n";
		result += "\n";
		result += "public UpdateList(java.util.Collection<Update> set) {\n";
		result += "super(set);\n";
		result += "}\n";
		result += "\n";
		result += "public UpdateList(Update u){\n";
		result += "super();\n";
		result += "this.add(u);\n";
		result += "}\n";
		result += "\n";
		result += "@Override\n";
		result += "public String toString(){\n";
		result += "String s = \"UpdateList\\n\";\n";
		result += "for(int i = 0; i < this.size(); i++){\n";
		result += "s = s + \"(\" + this.get(i).loc + \", \" + this.get(i).action + \", \" + this.get(i).value + \")\\n\";\n";
		result += "}\n";
		result += "return s;\n";
		result += "}\n";
		result += "\n";
		result += "@Override\n";
		result += "public boolean equals(Object o){\n";
		result += "if(o instanceof UpdateList){\n";
		result += "UpdateList ul = (UpdateList)o;\n";
		result += "if(ul.size() == this.size()){\n";
		result += "for(int i = 0; i < this.size(); i++){\n";
		result += "if(!this.get(i).equals(ul.get(i))) return false;\n";
		result += "}\n";
		result += "return true;\n";
		result += "}\n";
		result += "}\n";
		result += "return false;\n";
		result += "}\n";
		result += "}\n";
		return result;
	}

}
