package org.coreasm.engine.plugins.turboasm;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerTurboASM3_seq extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TurboASM3_seq.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TurboASM3_seq.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
