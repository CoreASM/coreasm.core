package org.coreasm.engine.plugins.predicatelogic;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class PredicateLogic3_forallHold extends TestAllCasm {

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
