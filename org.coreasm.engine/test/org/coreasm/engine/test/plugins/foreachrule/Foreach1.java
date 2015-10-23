package org.coreasm.engine.test.plugins.foreachrule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class Foreach1 extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Foreach1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Foreach1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
