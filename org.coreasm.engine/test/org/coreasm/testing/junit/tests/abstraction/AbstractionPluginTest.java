package org.coreasm.testing.junit.tests.abstraction;

import org.coreasm.compiler.CodeType;
import org.coreasm.engine.plugins.abstraction.AbstractionPlugin;
import org.coreasm.engine.plugins.io.IOPlugin;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.drivers.TestCaseDriver;
import org.coreasm.testing.drivers.TestCaseResult;
import org.coreasm.testing.junit.TestSuite;
import org.coreasm.testing.value.LocationProvider;
import org.coreasm.testing.value.ParameterProvider;
import org.coreasm.testing.value.StringProvider;
import org.coreasm.testing.value.UpdateProvider;
import org.junit.Test;

public class AbstractionPluginTest {
	@Test
	public void testAbstraction(){
		TestCase test = new TestCase();
		//build test case
		test.testName = "AbstractionTest";
		test.testPlugin = new AbstractionPlugin();
		test.codeType = CodeType.U;
		//test.specFile = new File(TestCaseDriver.getRootDir().getAbsolutePath() + "\\testing\\conditionaltest.coreasm");
		test.spec = TestCaseDriver.makeTestSpec("abstract PARAM text");
		//IOPlugin.OUTPUT_FUNC_LOC,
		//new StringElement("Abstract Call: " + pos.getMessage().getValue().toString()),
		//IOPlugin.PRINT_ACTION
		StringProvider strval = new StringProvider("Abstract Call: " + "abstract");
		
		UpdateProvider u = new UpdateProvider(new LocationProvider(IOPlugin.OUTPUT_FUNC_NAME), strval, IOPlugin.PRINT_ACTION);
		test.parameters.put("text", new ParameterProvider(new StringProvider("abstract")));
		test.nodeResult = new ParameterProvider(u);
		
		TestCaseResult result = TestSuite.driver.executeTestCase(test);		
		TestSuite.evaluateResult(result);
	}
}
