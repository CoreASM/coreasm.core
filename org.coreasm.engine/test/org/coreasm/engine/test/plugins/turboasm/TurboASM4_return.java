package org.coreasm.engine.test.plugins.turboasm;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class TurboASM4_return extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TurboASM4_return.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TurboASM4_return.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
