package org.coreasm.engine.plugins.conditionalrule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerIf1 extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = If1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), If1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
