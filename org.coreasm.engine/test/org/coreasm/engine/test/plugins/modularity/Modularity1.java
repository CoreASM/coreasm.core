package org.coreasm.engine.test.plugins.modularity;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class Modularity1 extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Modularity1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Modularity1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
