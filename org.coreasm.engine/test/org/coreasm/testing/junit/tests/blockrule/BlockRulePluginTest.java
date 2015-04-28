package org.coreasm.testing.junit.tests.blockrule;

import org.coreasm.compiler.CodeType;
import org.coreasm.engine.plugins.blockrule.BlockRulePlugin;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.drivers.TestCaseDriver;
import org.coreasm.testing.drivers.TestCaseResult;
import org.coreasm.testing.junit.TestSuite;
import org.coreasm.testing.value.ParameterProvider;
import org.coreasm.testing.value.UpdateListProvider;
import org.coreasm.testing.value.UpdateProvider;
import org.coreasm.testing.value.ValueHelper;
import org.junit.Test;

public class BlockRulePluginTest {
	@Test
	public void blockruletest(){
		TestCase test = new TestCase();
		//build test case
		test.testName = "BlockRuleTest";
		test.testPlugin = new BlockRulePlugin();
		test.codeType = CodeType.U;
		test.spec = TestCaseDriver.makeTestSpec("par\nPARAM r1\nPARAM r2\nendpar");
		UpdateProvider u1 = ValueHelper.constructUpdate("rule1", true);
		UpdateProvider u2 = ValueHelper.constructUpdate("rule2", false);
		
		UpdateListProvider ulist = new UpdateListProvider(u1, u2);
		
		test.parameters.put("r1", new ParameterProvider(u1));
		test.parameters.put("r2", new ParameterProvider(u2));
		
		test.nodeResult = new ParameterProvider(ulist);
		
		TestCaseResult result = TestSuite.driver.executeTestCase(test);
		TestSuite.evaluateResult(result);
	}
}
