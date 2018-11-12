package org.coreasm.engine.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class List15_replicate extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = List15_replicate.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), List15_replicate.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
