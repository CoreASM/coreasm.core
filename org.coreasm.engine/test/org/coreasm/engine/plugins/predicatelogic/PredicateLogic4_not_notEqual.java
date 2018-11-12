package org.coreasm.engine.plugins.predicatelogic;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class PredicateLogic4_not_notEqual extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = PredicateLogic4_not_notEqual.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), PredicateLogic4_not_notEqual.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
