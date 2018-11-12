package org.coreasm.engine.plugins.map;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerMap3_add extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Map3_add.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Map3_add.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
