package org.coreasm.engine.test.plugins.kernelextensions;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCCasm;

public class CompilerKernel1 extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Kernel1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Kernel1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
