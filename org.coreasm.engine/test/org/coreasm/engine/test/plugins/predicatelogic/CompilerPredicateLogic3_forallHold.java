package org.coreasm.engine.test.plugins.predicatelogic;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.test.TestAllCCasm;

public class CompilerPredicateLogic3_forallHold extends TestAllCCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = PredicateLogic3_forallHold.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), PredicateLogic3_forallHold.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
