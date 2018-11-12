package org.coreasm.engine.plugins.operator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import junit.framework.AssertionFailedError;
import org.coreasm.engine.TestAllCasm;
import org.junit.BeforeClass;
import org.junit.Test;

public class Operator_index_nArgs2 extends TestAllCasm {

  @BeforeClass
  public static void onlyOnce() {
    URL url = Operator_index_nArgs2.class.getClassLoader().getResource(".");

    try {
      testFiles = new LinkedList<File>();
      assert url != null;
      getTestFile(testFiles, new File(url.toURI()).getParentFile(), Operator_index_nArgs2.class);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  @Override
  @Test(expected=AssertionFailedError.class)
  public void performTest() {
    super.performTest();
  }
}
