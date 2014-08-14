package org.coreasm.engine.test.plugins.string;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class String3_matches extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = String3_matches.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), String3_matches.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
