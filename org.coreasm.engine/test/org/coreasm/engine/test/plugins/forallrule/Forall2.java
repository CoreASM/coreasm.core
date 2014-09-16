package org.coreasm.engine.test.plugins.forallrule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCasm;

public class Forall2 extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = Forall2.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), Forall2.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
