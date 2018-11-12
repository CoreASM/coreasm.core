package org.coreasm.engine.plugins.caserule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Case1 extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Case1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Case1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
