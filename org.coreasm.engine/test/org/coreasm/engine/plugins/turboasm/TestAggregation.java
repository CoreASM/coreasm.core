package org.coreasm.engine.plugins.turboasm;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class TestAggregation extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = TestAggregation.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), TestAggregation.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
