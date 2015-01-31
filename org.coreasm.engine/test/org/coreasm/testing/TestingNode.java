package org.coreasm.testing;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;

public class TestingNode extends ASTNode {
	private static final long serialVersionUID = 4296506537051401988L;
	
	public TestingNode(String token) {
		super("JUNIT", "JUNIT", "JUNIT", token, null, Node.DEFAULT_CONCRETE_TYPE);
        this.grammarClass = "JUNIT";
        this.grammarRule = "JUNIT";
        this.token = token;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof TestingNode){
			TestingNode other = (TestingNode) o;
			return (other.grammarClass.equals(grammarClass) && other.grammarRule.equals(grammarRule) && other.token.equals(token));
		}
		return super.equals(o);
	}
}
