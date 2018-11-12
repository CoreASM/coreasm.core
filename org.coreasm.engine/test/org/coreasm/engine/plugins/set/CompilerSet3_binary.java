package org.coreasm.engine.plugins.set;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerSet3_binary extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Set3_binary.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Set3_binary.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
