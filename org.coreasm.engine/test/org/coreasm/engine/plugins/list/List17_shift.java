package org.coreasm.engine.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class List17_shift extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = List17_shift.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), List17_shift.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
