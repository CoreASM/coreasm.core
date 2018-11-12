package org.coreasm.engine.plugins.foreachrule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Foreach2 extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Foreach2.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Foreach2.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
