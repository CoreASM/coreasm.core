package org.coreasm.engine.plugins.number;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Number2_numberRange extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Number2_numberRange.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Number2_numberRange.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}