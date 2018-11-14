package org.coreasm.engine.plugins.operator;

import org.coreasm.engine.parser.ParserException;
import org.junit.Assert;
import org.junit.Test;

public class Operator_unit_CommentRemover_unbalanced {

  @Test(expected=ParserException.class)
  public void performTest() throws ParserException {
    CommentRemover cr = new CommentRemover();
    Assert.assertEquals("CoreASM Test", cr.append("CoreASM \"Test"));
  }

}
