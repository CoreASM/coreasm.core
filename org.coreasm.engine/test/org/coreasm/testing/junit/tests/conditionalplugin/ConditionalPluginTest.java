package org.coreasm.testing.junit.tests.conditionalplugin;

import java.io.File;

import org.coreasm.compiler.CodeType;
import org.coreasm.engine.plugins.conditionalrule.ConditionalRulePlugin;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.drivers.TestCaseDriver;
import org.coreasm.testing.drivers.TestCaseResult;
import org.coreasm.testing.junit.TestSuite;
import org.coreasm.testing.value.BooleanProvider;
import org.coreasm.testing.value.LocationProvider;
import org.coreasm.testing.value.ParameterProvider;
import org.coreasm.testing.value.UpdateProvider;
import org.junit.Test;

public class ConditionalPluginTest {
	private static ParameterProvider trueval = new ParameterProvider(BooleanProvider.TRUE);
	private static ParameterProvider falseval = new ParameterProvider(BooleanProvider.FALSE);
	private static ParameterProvider truerule = new ParameterProvider(new UpdateProvider(new LocationProvider("test"), BooleanProvider.TRUE, "UPDATE_ACTION"));
	private static ParameterProvider falserule = new ParameterProvider(new UpdateProvider(new LocationProvider("test"), BooleanProvider.FALSE, "UPDATE_ACTION"));

	@Test
	public void testTrueCondition(){
		//build test case
		TestCase test = new TestCase();
		test.testName = "ConditionalTrueTest";
		test.testPlugin = new ConditionalRulePlugin();
		test.codeType = CodeType.U;
		//test.specFile = new File(TestCaseDriver.getRootDir().getAbsolutePath() + "\\testing\\conditionaltest.coreasm");
		test.spec = TestCaseDriver.makeTestSpec("if PARAM guard then PARAM truerule else PARAM falserule");
		test.parameters.put("guard", trueval);
		test.parameters.put("truerule", truerule);
		test.parameters.put("falserule", falserule);
		test.nodeResult = truerule;
		
		TestCaseResult result = TestSuite.driver.executeTestCase(test);
		TestSuite.evaluateResult(result);
	}
	
	@Test
	public void testFalseCondition(){
		//build test case
		TestCase test = new TestCase();
		test.testName = "ConditionalFalseTest";
		test.testPlugin = new ConditionalRulePlugin();
		test.codeType = CodeType.U;
		test.specFile = new File(TestCaseDriver.getRootDir().getAbsolutePath() + "\\testing\\conditionaltest.coreasm");
		
		test.parameters.put("guard", falseval);
		test.parameters.put("truerule", truerule);
		test.parameters.put("falserule", falserule);
		test.nodeResult = falserule;
		
		TestCaseResult result = TestSuite.driver.executeTestCase(test);
		TestSuite.evaluateResult(result);
	}
}
