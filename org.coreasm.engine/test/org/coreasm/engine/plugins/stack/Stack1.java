package org.coreasm.engine.plugins.stack;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Stack1 extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Stack1.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Stack1.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
