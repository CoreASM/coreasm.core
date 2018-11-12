package org.coreasm.engine.plugins.operator;

import org.junit.Assert;
import org.junit.Test;

public class Operator_unit_OperatorPlugin {

  @Test
  public void performTest() {
    OperatorPlugin op = new OperatorPlugin();
    Assert.assertNull(op.getParser(""));
    Assert.assertNull(op.getParser("+"));
    Assert.assertNull(op.getParser("?"));
    Assert.assertNull(op.getParser("foo"));
    Assert.assertNull(op.getParser("bar"));
    Assert.assertNull(op.getParser("foobar"));
    Assert.assertNull(op.getParser("abEE8ZPBLw"));
    Assert.assertNotNull(op.getVersionInfo());
  }

}
