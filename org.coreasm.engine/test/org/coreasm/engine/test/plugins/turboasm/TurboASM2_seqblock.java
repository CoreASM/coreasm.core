package org.coreasm.engine.test.plugins.turboasm;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class TurboASM2_seqblock extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TurboASM2_seqblock.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TurboASM2_seqblock.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
