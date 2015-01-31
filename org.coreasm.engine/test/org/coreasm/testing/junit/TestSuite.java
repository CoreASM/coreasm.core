package org.coreasm.testing.junit;

import junit.framework.Assert;

import org.coreasm.testing.drivers.TestCaseDriver;
import org.coreasm.testing.drivers.TestCaseResult;
import org.coreasm.testing.junit.tests.conditionalplugin.ConditionalPluginTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ConditionalPluginTest.class
})
public class TestSuite {
	public static TestCaseDriver driver;
	@BeforeClass
	public static void setup(){
		driver = new TestCaseDriver();
		driver.init();
	}
	
	public static void evaluateResult(TestCaseResult result){
		if(result.compiler.error != null && result.interpreter.error == null){
			Assert.fail("Compiler failed");
		} else if(result.compiler.error == null && result.interpreter.error != null){
			Assert.fail("Interpreter failed");
		} else if(result.compiler.error != null && result.interpreter.error != null){
			Assert.fail("Compiler and Interpreter failed");	
		}
	}
}
