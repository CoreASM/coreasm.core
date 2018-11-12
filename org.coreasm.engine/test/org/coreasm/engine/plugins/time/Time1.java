package org.coreasm.engine.plugins.time;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Time1 extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Time1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Time1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
