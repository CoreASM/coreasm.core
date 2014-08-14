package org.coreasm.engine.test.plugins.map;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class Map4_remove extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Map4_remove.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Map4_remove.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
