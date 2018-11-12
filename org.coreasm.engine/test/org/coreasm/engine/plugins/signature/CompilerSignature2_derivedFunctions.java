package org.coreasm.engine.plugins.signature;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerSignature2_derivedFunctions extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Signature2_derivedFunctions.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Signature2_derivedFunctions.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
