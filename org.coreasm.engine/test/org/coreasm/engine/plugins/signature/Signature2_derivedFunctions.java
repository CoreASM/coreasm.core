package org.coreasm.engine.plugins.signature;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Signature2_derivedFunctions extends TestAllCasm {

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
