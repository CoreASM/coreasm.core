package org.coreasm.engine.plugins.list;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerList2_head extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = List2_head.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), List2_head.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
