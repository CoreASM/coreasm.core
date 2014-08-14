package org.coreasm.engine.test.plugins.set;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class Set3_binary extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Set3_binary.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Set3_binary.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
