package org.coreasm.testing.drivers;

import java.io.File;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.TestDriver;

public class TestCaseDriver {
	private TestCaseParser parser;
	private CoreASMCTestDriver casmcdriver;
	private CoreASMTestDriver casmdriver;
	private static File rootDir;
	
	public TestCaseDriver(){
		parser = new TestCaseParser();
		casmcdriver = new CoreASMCTestDriver();
		casmdriver = new CoreASMTestDriver();
	}
	
	
	public void init(){
		rootDir = new File(TestDriver.class.getClassLoader().getResource("./").getFile());
		System.out.println(rootDir);
		parser.init();
		buildDir();
		casmcdriver.init();
		casmdriver.init();
	}
	
	public void dispose(){
		parser.dispose();
		casmcdriver.dispose();
		casmdriver.dispose();
	}
	
	public static File getRootDir(){
		return rootDir;
	}
	
	private void buildDir() {
		checkAndMake(new File(rootDir.getAbsolutePath() + "\\testCase\\"));
		checkAndMake(new File(rootDir.getAbsolutePath() + "\\testCase\\compiler\\"));
		checkAndMake(new File(rootDir.getAbsolutePath() + "\\testCase\\interpreter\\"));
	}


	public TestCaseResult executeTestCase(TestCase test){
		File compDir = new File(rootDir.getAbsolutePath() + "\\testCase\\compiler\\" + test.testName + "\\");
		File intDir = new File(rootDir.getAbsolutePath() + "\\testCase\\interpreter\\"  + test.testName + "\\");
		purgeDir(compDir);
		purgeDir(intDir);
		checkAndMake(compDir);
		checkAndMake(intDir);
		
		TestCaseResult result = new TestCaseResult();
		
		ASTNode root = parser.parseSpec(test);
		result.compiler = casmcdriver.execute(test, root);
		result.interpreter = casmdriver.execute(test, root);
		
		return result;
	}
	
	public static String makeTestSpec(String s) {
		return "CoreASM testing\nuse Standard\nuse Testing\ninit A\ntest=" + s;
	}
	
	private void checkAndMake(File f){
		if(!f.exists()) f.mkdir();
	}
	
	private void purgeDir(File f){
		if(f.exists()){
			if(f.isFile()){
				f.delete();
			}
			else{
				for(File d : f.listFiles()){
					purgeDir(d);
				}
				f.delete();
			}
		}
	}
}
