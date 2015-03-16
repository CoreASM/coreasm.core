package org.coreasm.engine.test.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCCasm;

public class CompilerList15_replicate extends TestAllCCasm {

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
