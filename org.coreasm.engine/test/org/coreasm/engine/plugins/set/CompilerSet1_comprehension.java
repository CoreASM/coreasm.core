package org.coreasm.engine.plugins.set;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerSet1_comprehension extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Set1_comprehension.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Set1_comprehension.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
