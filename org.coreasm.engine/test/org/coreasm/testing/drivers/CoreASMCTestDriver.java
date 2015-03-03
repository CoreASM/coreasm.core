package org.coreasm.testing.drivers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.LoggingHelper;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.variablemanager.VarManager;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.testing.CodeBuilder;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.TestingNode;
import org.coreasm.testing.compiling.ClassCompiler;
import org.coreasm.testing.loading.LoadingFactory;
import org.mockito.Mockito;

public class CoreASMCTestDriver {
	private LoadingFactory factory;
	
	public CoreASMCTestDriver() {
		factory = new LoadingFactory();
	}
	
	public void init(){
		
	}
	
	public CompilerResult execute(TestCase test, ASTNode root) {
		CompilerResult res = new CompilerResult();
		CompilerEngine mockCompiler = Mockito.mock(CompilerEngine.class);
		//setup the mock compiler
		VarManager varmanager = new VarManager();
		Mockito.doReturn(varmanager).when(mockCompiler).getVarManager();
		for(String s : test.parameters.keySet()){
			try{
				Mockito.doReturn(new CodeFragment(test.parameters.get(s).compilerValue())).when(mockCompiler).compile(Mockito.eq(new TestingNode(s)), Mockito.any(CodeType.class));
			}
			catch(CompilerException ce){
				res.error = ce;
				res.messages.add("Mocking error");
				return res;
			}
		}
		
		Mockito.doReturn(new LoggingHelper()).when(mockCompiler).getLogger();
		
		CompilerPlugin p = test.testPlugin.getCompilerPlugin();
		if(p == null || !(p instanceof CompilerCodePlugin)){
			res.error = new Exception("Incompatible types");
			res.messages.add("Compiler Plugin not found or not a code generating plugin");
			return res;
		}
		
		CompilerCodePlugin ccp = (CompilerCodePlugin) p;
		try{
			ccp.registerCodeHandlers();
		}
		catch(CompilerException ce){
			res.error = ce;
			res.messages.add("Error registering code handlers");
			return res;
		}
		CodeFragment code;
		try{
			code = ccp.compile(test.codeType, root);
		}
		catch(CompilerException ce){
			res.error = ce;
			res.messages.add("Error generating code");
			return res;
		}
		
		CodeBuilder cb = new CodeBuilder();
		
		File taskFile = new File(TestCaseDriver.getRootDir(), "testCase\\compiler\\" + test.testName + "\\CoreASMCTest.java");//new File(TestCaseDriver.getRootDir().getAbsolutePath() + "\\testCase\\compiler\\" + test.testName + "\\CoreASMCTest.java");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(taskFile));
			bw.write(cb.generateCode(code.generateCode(mockCompiler)));
			bw.close();
		}
		catch(Exception e){
			res.error = e;
			res.messages.add("Could not write to java file " + taskFile);
			return res;
		}
		
		ClassCompiler comp = new ClassCompiler();
		comp.addTask(taskFile);
		try{
			comp.compile();
		}
		catch(Exception e){
			res.error = e;
			res.messages.add("Could not compile sources:");
			for(String str : comp.getErrors()) res.messages.add(str);
			return res;
		}
		
		try{
			Class<?> clazz = factory.loadTestClass(TestCaseDriver.getRootDir().getAbsolutePath() + "\\testCase\\compiler\\" + test.testName + "\\", "CoreASMCTest");
			Object o = clazz.getConstructor().newInstance();
			Method m = clazz.getMethod("eval");
			res.result = m.invoke(o);
			
			if(!test.nodeResult.equalsCompiler(res.result)){
				res.messages.add("Compiler result does not match the expected value");
				res.error = new Exception("Mismatching result");
			}
			
			return res;
		}
		catch(Throwable e){
			res.error = e;
			res.messages.add("Error loading and running class");
			return res;
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
