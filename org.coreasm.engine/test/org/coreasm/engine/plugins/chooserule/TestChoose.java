package org.coreasm.engine.plugins.chooserule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class TestChoose extends TestAllCasm {

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
