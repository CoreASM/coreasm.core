package org.coreasm.engine.plugins.io;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerTestIO1 extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TestIO1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TestIO1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
