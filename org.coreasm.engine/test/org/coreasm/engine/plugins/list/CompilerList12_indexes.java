package org.coreasm.engine.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerList12_indexes extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = List12_indexes.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), List12_indexes.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
