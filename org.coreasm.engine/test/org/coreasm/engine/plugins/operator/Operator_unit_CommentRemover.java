package org.coreasm.engine.plugins.operator;

import org.coreasm.engine.parser.CommentRemover;
import org.coreasm.engine.parser.ParserException;
import org.junit.Assert;
import org.junit.Test;

public class Operator_unit_CommentRemover {

  @Test
  public void performTest() throws ParserException {
    CommentRemover cr = new CommentRemover();
    Assert.assertEquals("CoreASM Test", cr.append("CoreASM Test"));
    Assert.assertEquals("CoreASM \"Test\"", cr.append("CoreASM \"Test\""));
    Assert.assertEquals("CoreASM \"//Test\"", cr.append("CoreASM \"//Test\""));
    Assert.assertEquals("CoreASM ", cr.append("CoreASM //\"//Test\""));
    Assert.assertEquals("CoreASM ", cr.append("CoreASM //\"//*Test\""));
    Assert.assertEquals("CoreASM ", cr.append("CoreASM //\"/*/Test\""));
    Assert.assertEquals("CoreASM \"//*Test\"", cr.append("CoreASM \"//*Test\""));
    Assert.assertEquals("CoreASM \"/*/Test\"", cr.append("CoreASM \"/*/Test\""));
    Assert.assertEquals("CoreASM \"Te\\//st\"", cr.append("CoreASM \"Te\\//st\""));
    Assert.assertEquals("CoreASM \"Te\\\"//st\"", cr.append("CoreASM \"Te\\\"//st\""));
    Assert.assertEquals("CoreASM \"Te\\\\\"", cr.append("CoreASM \"Te\\\\\"//st\""));
    Assert.assertEquals("CoreASM \"Te\\\\\\\"//st\"", cr.append("CoreASM \"Te\\\\\\\"//st\""));
    Assert.assertEquals("CoreASM \"Te\\n\\\\\"", cr.append("CoreASM \"Te\\n\\\\\"//st\""));
    Assert.assertEquals("CoreASM \"Te\\n\\\"//st\"", cr.append("CoreASM \"Te\\n\\\"//st\""));
    Assert.assertEquals("CoreASM Test", cr.append("CoreASM Test/*"));
    Assert.assertEquals("", cr.append("CoreASM Test"));
    Assert.assertEquals("CoreASM Test", cr.append("*/CoreASM Test"));
    Assert.assertEquals("CoreASM Test", cr.append("CoreASM Test/*"));
    Assert.assertEquals("\"Test\"", cr.append("CoreASM \"*/\"Test\""));
    Assert.assertEquals("*/CoreASM Test", cr.append("*/CoreASM Test"));
    Assert.assertEquals("", cr.append("\"//CoreASM Test"));
    Assert.assertEquals("\"//CoreASM Test\\n*/CoreASM Test\"", cr.append("*/CoreASM Test\""));
    Assert.assertEquals("*/CoreASM Test", cr.append("*/CoreASM Test"));
  }

}
