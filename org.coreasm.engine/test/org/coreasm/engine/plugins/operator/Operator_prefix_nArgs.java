package org.coreasm.engine.plugins.operator;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import junit.framework.AssertionFailedError;
import org.coreasm.engine.TestAllCasm;
import org.junit.BeforeClass;
import org.junit.Test;

public class Operator_prefix_nArgs extends TestAllCasm {

  @BeforeClass
  public static void onlyOnce() {
    new Thread(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      ThreadMXBean bean = ManagementFactory.getThreadMXBean();
      long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.

      if (threadIds != null) {
        ThreadInfo[] infos = bean.getThreadInfo(threadIds);

        for (ThreadInfo info : infos) {
          StackTraceElement[] stack = info.getStackTrace();
          TestAllCasm.origOutput.println(Arrays.toString(stack));
        }
      }
      System.exit(1);
    }).start();

    URL url = Operator_prefix_nArgs.class.getClassLoader().getResource(".");

    try {
      testFiles = new LinkedList<File>();
      assert url != null;
      getTestFile(testFiles, new File(url.toURI()).getParentFile(), Operator_prefix_nArgs.class);
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
