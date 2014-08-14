package org.coreasm.engine.test.plugins.kernel_extension;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class Kernel1 extends TestAllCasm {

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
