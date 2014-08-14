package org.coreasm.engine.test.plugins.string;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class String1_toString extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = String1_toString.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), String1_toString.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
