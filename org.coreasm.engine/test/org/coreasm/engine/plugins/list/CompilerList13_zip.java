package org.coreasm.engine.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerList13_zip extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = List13_zip.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), List13_zip.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
