package org.coreasm.engine.plugins.set;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Set2_adding_removing extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Set2_adding_removing.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Set2_adding_removing.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
