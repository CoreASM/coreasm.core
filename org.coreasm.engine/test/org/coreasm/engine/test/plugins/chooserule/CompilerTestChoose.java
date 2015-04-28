package org.coreasm.engine.test.plugins.chooserule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCCasm;

public class CompilerTestChoose extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TestChoose.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TestChoose.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
