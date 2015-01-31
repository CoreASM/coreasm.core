package org.coreasm.testing;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.testing.modules.BooleanTestingModule;
import org.coreasm.testing.modules.ElementModule;
import org.coreasm.testing.modules.EvalStackModule;
import org.coreasm.testing.modules.LocationModule;
import org.coreasm.testing.modules.TestResultModule;
import org.coreasm.testing.modules.UpdateListModule;
import org.coreasm.testing.modules.UpdateModule;

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
