package org.coreasm.engine.test.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class List10_drop extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = List10_drop.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), List10_drop.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
