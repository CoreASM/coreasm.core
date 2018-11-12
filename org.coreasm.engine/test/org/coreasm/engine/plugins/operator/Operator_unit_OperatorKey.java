package org.coreasm.engine.plugins.operator;

import org.coreasm.engine.plugins.operator.OperatorPlugin.Fixity;
import org.coreasm.engine.plugins.operator.OperatorPlugin.OperatorKey;
import org.junit.Assert;
import org.junit.Test;

public class Operator_unit_OperatorKey {

  @Test
  public void performTest() {
    OperatorKey ok = new OperatorKey(Fixity.PREFIX, new String[]{"++"});
    OperatorKey ok2 = ok;
    OperatorKey ok3 = new OperatorKey(Fixity.PREFIX, new String[]{"++"});
    OperatorKey ok4 = new OperatorKey(Fixity.PREFIX, new String[]{"+"});
    OperatorKey ok5 = new OperatorKey(Fixity.INFIX, new String[]{"++"});
    Object obj = new Object();
    Assert.assertEquals(ok, ok);
    Assert.assertSame(ok, ok);
    Assert.assertEquals(ok, ok2);
    Assert.assertSame(ok, ok2);
    Assert.assertEquals(ok, ok3);
    Assert.assertNotSame(ok, ok3);
    Assert.assertFalse(ok.equals(ok4));
    Assert.assertNotSame(ok, ok4);
    Assert.assertFalse(ok.equals(ok5));
    Assert.assertNotSame(ok, ok5);
    Assert.assertFalse(ok.equals(null));
    Assert.assertNotSame(ok, null);
    Assert.assertFalse(ok.equals(obj));
    Assert.assertNotSame(ok, obj);
  }

}
