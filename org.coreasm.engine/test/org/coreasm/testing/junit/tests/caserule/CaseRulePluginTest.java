package org.coreasm.testing.junit.tests.caserule;

import org.coreasm.compiler.CodeType;
import org.coreasm.engine.plugins.caserule.CaseRulePlugin;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.drivers.TestCaseDriver;
import org.coreasm.testing.drivers.TestCaseResult;
import org.coreasm.testing.junit.TestSuite;
import org.coreasm.testing.value.BooleanProvider;
import org.coreasm.testing.value.ParameterProvider;
import org.coreasm.testing.value.StringProvider;
import org.coreasm.testing.value.UpdateListProvider;
import org.coreasm.testing.value.UpdateProvider;
import org.coreasm.testing.value.ValueHelper;
import org.junit.Test;

public class CaseRulePluginTest {
	@Test
	public void caserulenomatchtest(){
		TestCase test = new TestCase();
		test.testName = "CaseRuleNoMatchTest";
		test.testPlugin = new CaseRulePlugin();
		test.codeType = CodeType.U;
		test.spec = TestCaseDriver.makeTestSpec("case PARAM val of\nPARAM v1 : PARAM r1\nPARAM v2 : PARAM r2\nPARAM v3 : PARAM r3\nendcase");
		UpdateProvider u1 = ValueHelper.constructUpdate("r1", true);
		UpdateProvider u2 = ValueHelper.constructUpdate("r2", false);
		UpdateProvider u3 = ValueHelper.constructUpdate("r3", false);
		BooleanProvider v1 = BooleanProvider.FALSE;
		BooleanProvider v2 = BooleanProvider.TRUE;
		
		test.parameters.put("val", new ParameterProvider(v2));
		test.parameters.put("v1", new ParameterProvider(v1));
		test.parameters.put("v2", new ParameterProvider(v1));
		test.parameters.put("v3", new ParameterProvider(v1));

		test.parameters.put("r1", new ParameterProvider(u1));
		test.parameters.put("r2", new ParameterProvider(u2));
		test.parameters.put("r3", new ParameterProvider(u3));
		
		UpdateListProvider ulist = new UpdateListProvider();
		
		test.nodeResult = new ParameterProvider(ulist);
		
		TestCaseResult result = TestSuite.driver.executeTestCase(test);
		if(result.interpreter.error != null) result.interpreter.error.printStackTrace();
		if(result.compiler.error != null) result.compiler.error.printStackTrace();
		TestSuite.evaluateResult(result);
	}
	
	@Test
	public void caseruleBooltest(){
		TestCase test = new TestCase();
		test.testName = "CaseRuleBoolTest";
		test.testPlugin = new CaseRulePlugin();
		test.codeType = CodeType.U;
		test.spec = TestCaseDriver.makeTestSpec("case PARAM val of\nPARAM v1 : PARAM r1\nPARAM v2 : PARAM r2\nPARAM v3 : PARAM r3\nendcase");
		UpdateProvider u1 = ValueHelper.constructUpdate("r1", true);
		UpdateProvider u2 = ValueHelper.constructUpdate("r2", false);
		UpdateProvider u3 = ValueHelper.constructUpdate("r3", false);
		BooleanProvider v1 = BooleanProvider.FALSE;
		BooleanProvider v2 = BooleanProvider.TRUE;
		
		test.parameters.put("val", new ParameterProvider(v2));
		test.parameters.put("v1", new ParameterProvider(v1));
		test.parameters.put("v2", new ParameterProvider(v2));
		test.parameters.put("v3", new ParameterProvider(v2));

		test.parameters.put("r1", new ParameterProvider(u1));
		test.parameters.put("r2", new ParameterProvider(u2));
		test.parameters.put("r3", new ParameterProvider(u3));
		
		UpdateListProvider ulist = new UpdateListProvider(u2, u3);
		
		test.nodeResult = new ParameterProvider(ulist);
		
		TestCaseResult result = TestSuite.driver.executeTestCase(test);

		if(result.interpreter.error != null) result.interpreter.error.printStackTrace();
		if(result.compiler.error != null) result.compiler.error.printStackTrace();
		TestSuite.evaluateResult(result);
	}
	
	@Test
	public void caseruleStringTest(){
		TestCase test = new TestCase();
		//build test case
		test.testName = "CaseRuleBoolTest";
		test.testPlugin = new CaseRulePlugin();
		test.codeType = CodeType.U;
		test.spec = TestCaseDriver.makeTestSpec("case PARAM val of\nPARAM v1 : PARAM r1\nPARAM v2 : PARAM r2\nPARAM v3 : PARAM r3\nendcase");
		UpdateProvider u1 = ValueHelper.constructUpdate("r1", true);
		UpdateProvider u2 = ValueHelper.constructUpdate("r2", false);
		UpdateProvider u3 = ValueHelper.constructUpdate("r3", false);
		StringProvider v1 = new StringProvider("hallo");
		StringProvider v2 = new StringProvider("welt");
		
		test.parameters.put("val", new ParameterProvider(v2));
		test.parameters.put("v1", new ParameterProvider(v1));
		test.parameters.put("v2", new ParameterProvider(v2));
		test.parameters.put("v3", new ParameterProvider(v2));

		test.parameters.put("r1", new ParameterProvider(u1));
		test.parameters.put("r2", new ParameterProvider(u2));
		test.parameters.put("r3", new ParameterProvider(u3));
		
		UpdateListProvider ulist = new UpdateListProvider(u2, u3);
		
		test.nodeResult = new ParameterProvider(ulist);
		
		TestCaseResult result = TestSuite.driver.executeTestCase(test);

		if(result.interpreter.error != null) result.interpreter.error.printStackTrace();
		if(result.compiler.error != null) result.compiler.error.printStackTrace();
		TestSuite.evaluateResult(result);
	}
}
