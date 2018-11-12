package org.coreasm.engine.plugins.chooserule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerPick1 extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Pick1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Pick1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
