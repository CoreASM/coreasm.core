package org.coreasm.engine.test.plugins.set;

import org.coreasm.engine.test.TestAllCasm;
import org.junit.BeforeClass;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

public class Set4_compose extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Set4_compose.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Set4_compose.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
