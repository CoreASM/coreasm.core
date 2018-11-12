package org.coreasm.engine.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class List5_last extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = List5_last.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), List5_last.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
