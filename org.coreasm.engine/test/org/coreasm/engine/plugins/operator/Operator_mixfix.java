package org.coreasm.engine.plugins.operator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import org.coreasm.engine.TestAllCasm;
import org.junit.BeforeClass;

public class Operator_mixfix extends TestAllCasm {

  @BeforeClass
  public static void onlyOnce() {
    URL url = Operator_mixfix.class.getClassLoader().getResource(".");

    try {
      testFiles = new LinkedList<File>();
      assert url != null;
      getTestFile(testFiles, new File(url.toURI()).getParentFile(), Operator_mixfix.class);
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
