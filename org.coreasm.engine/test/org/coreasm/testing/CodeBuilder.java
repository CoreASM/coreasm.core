package org.coreasm.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.testing.modules.EvalStackModule;

public class CodeBuilder {
	private List<TestingHelperModule> modules;
	
	public CodeBuilder(){
		modules = new ArrayList<TestingHelperModule>();
		modules.add(new EvalStackModule());
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
		res += "public void eval(){\n";
		res += "EvalStack evalStack = new EvalStack();\n";
		res += code;
		res += "}\n";
		res += "}\n";
		res += blocks;
		
		//add replacements from the modules
		for(TestingHelperModule thm : modules){
			res = thm.modifyCode(res);
		}
		
		return res;
	}
}
