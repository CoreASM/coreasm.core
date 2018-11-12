package org.coreasm.engine.plugins.debuginfo;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerDebug1 extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Debug1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Debug1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
