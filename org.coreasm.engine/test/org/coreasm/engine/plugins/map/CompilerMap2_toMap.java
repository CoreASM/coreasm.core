package org.coreasm.engine.plugins.map;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerMap2_toMap extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Map2_toMap.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Map2_toMap.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
