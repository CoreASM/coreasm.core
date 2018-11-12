package org.coreasm.engine.plugins.operator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.coreasm.engine.TestAllCasm;

public class Operator_prefix extends TestAllCasm {

  @BeforeClass
  public static void onlyOnce() {
    URL url = Operator_prefix.class.getClassLoader().getResource(".");

    try {
      testFiles = new LinkedList<File>();
      assert url != null;
      getTestFile(testFiles, new File(url.toURI()).getParentFile(), Operator_prefix.class);
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
