package org.coreasm.engine.plugins.collection;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import org.coreasm.engine.TestAllCasm;
import org.junit.BeforeClass;

public class CardinalityOp extends TestAllCasm {

	@BeforeClass
	public static void onlyOnce() {
		URL url = CardinalityOp.class.getClassLoader().getResource(".");

		try {
			testFiles = new LinkedList<File>();
			getTestFile(testFiles, new File(url.toURI()).getParentFile(), CardinalityOp.class);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
