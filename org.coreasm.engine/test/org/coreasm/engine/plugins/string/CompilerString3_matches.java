package org.coreasm.engine.plugins.string;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerString3_matches extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = String3_matches.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), String3_matches.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
