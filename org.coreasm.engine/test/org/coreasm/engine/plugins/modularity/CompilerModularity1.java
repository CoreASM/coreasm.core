package org.coreasm.engine.plugins.modularity;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerModularity1 extends TestAllCCasm {

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