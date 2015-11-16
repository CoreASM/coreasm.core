package org.coreasm.engine.plugins.letrule;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;

public class LetResultChildNode extends ASTNode {
    private static final long serialVersionUID = 1L;
    
    protected LetResultChildNode(LetRuleNode parent) {
        super(
        		LetRulePlugin.PLUGIN_NAME,
        		ASTNode.RULE_CLASS,
        		"LetResultRuleChildNode",
        		null,
        		parent.getScannerInfo());
        setParent(parent);
    }

    public LetResultChildNode(LetResultChildNode node) {
    	super(node);
    	throw new UnsupportedOperationException("This node must not be copied.");
    }
    
    @Override
    public void addChild(Node node) {
    	throw new UnsupportedOperationException("This node must not be part of a tree.");
    }
    
    @Override
    public void addChild(String name, Node node) {
    	throw new UnsupportedOperationException("This node must not be part of a tree.");
    }
    
    @Override
    public void addChildAfter(Node indexNode, String name, Node node) throws IllegalArgumentException {
    	throw new UnsupportedOperationException("This node must not be part of a tree.");
    }
}
