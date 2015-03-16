package org.coreasm.engine.test.plugins.collection;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCCasm;

public class CompilerCollection1 extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Collection1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Collection1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
