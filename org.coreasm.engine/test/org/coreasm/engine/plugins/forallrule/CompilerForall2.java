package org.coreasm.engine.plugins.forallrule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCCasm;

public class CompilerForall2 extends TestAllCCasm {

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
