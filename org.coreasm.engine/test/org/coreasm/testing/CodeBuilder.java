package org.coreasm.testing;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.testing.modules.io.IOModule;
import org.coreasm.testing.modules.kernel.BooleanTestingModule;
import org.coreasm.testing.modules.kernel.ElementModule;
import org.coreasm.testing.modules.kernel.EnumerableModule;
import org.coreasm.testing.modules.kernel.EvalStackModule;
import org.coreasm.testing.modules.kernel.LocationModule;
import org.coreasm.testing.modules.kernel.TestResultModule;
import org.coreasm.testing.modules.kernel.UpdateListModule;
import org.coreasm.testing.modules.kernel.UpdateModule;
import org.coreasm.testing.modules.string.StringModule;

public class CodeBuilder {
	private List<TestingHelperModule> modules;
	
	public CodeBuilder(){
		modules = new ArrayList<TestingHelperModule>();
		modules.add(new EvalStackModule());
		modules.add(new ElementModule());
		modules.add(new UpdateModule());
		modules.add(new TestResultModule());
		modules.add(new LocationModule());
		modules.add(new UpdateListModule());
		modules.add(new BooleanTestingModule());
		modules.add(new StringModule());
		modules.add(new IOModule());
		modules.add(new EnumerableModule());
	}
	
	public void addModule(TestingHelperModule mod){
		modules.add(mod);
	}
	
	public String generateCode(String code){
		String res = code;
		String blocks = "";
		
		//add inserts from the various modules
		for(TestingHelperModule htm : modules){
			blocks = blocks + htm.getCodeBlock();
		}
		
		//add missing code constructs
		res = "public class CoreASMCTest{\n";
		res += "\tjava.util.List<CompilerRuntime.Element> __init_param_list__;\n";
		res += "\tjava.util.List<CompilerRuntime.Update> __init_update_list__;\n";
		res += "\tEvalStack evalStack = new EvalStack();\n";
		res += "\tElement getUpdateResponsible(){\n";
		res += "\t\treturn null;\n";
		res += "\t}\n";
		res += "\tpublic TestResult eval(){\n";
		res += "\t\tEvalStack evalStack = new EvalStack();\n";
		res += code;
		res += "\tTestResult testResult = new TestResult();\n";
		res += "\twhile(!evalStack.isEmpty()){\n";
		res += "\t\tObject testResultObject = evalStack.pop();\n";
		res += "\t\tif(testResultObject instanceof CompilerRuntime.Element) testResult.element = (CompilerRuntime.Element) testResultObject;\n";
		res += "\t\telse if(testResultObject instanceof CompilerRuntime.UpdateList) testResult.ulist = (CompilerRuntime.UpdateList) testResultObject;\n";
		res += "\t\telse if(testResultObject instanceof CompilerRuntime.Location) testResult.location = (CompilerRuntime.Location) testResultObject;\n";
		res += "\t}\n";
		res += "\treturn testResult;\n";
		res += "\t}\n";
		res += "}\n";
		res += blocks;
		
		//add replacements from the modules
		for(TestingHelperModule thm : modules){
			res = thm.modifyCode(res);
		}
		
		return res;
	}
}
