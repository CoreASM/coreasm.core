package org.coreasm.engine.plugins.turboasm;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class TurboASM5_local extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TurboASM5_local.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TurboASM5_local.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
