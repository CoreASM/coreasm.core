package org.coreasm.engine.plugins.conditionalrule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerTestConditionalTerm extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TestConditionalTerm.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TestConditionalTerm.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
