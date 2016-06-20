package org.coreasm.engine.plugins.foreachrule;

import java.util.Map;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/** 
 *	A ForeachRuleNode represents a foreach rule.
 *   
 *  @author  Michael Stegmaier
 *  
 */
@SuppressWarnings("serial")
public class ForeachRuleNode extends ASTNode {
	private VariableMap variableMap;

    /**
     * Creates a new ForeachRuleNode
     */
    public ForeachRuleNode(ScannerInfo info) {
        super(
        		ForeachRulePlugin.PLUGIN_NAME,
        		ASTNode.RULE_CLASS,
        		"ForeachRule",
        		null,
        		info);
    }

    public ForeachRuleNode(ForeachRuleNode node) {
    	super(node);
    }
    
    @Override
	public void addChild(String name, Node node) {
		if (node instanceof ASTNode) {
			ASTNode astNode = (ASTNode)node;
			if (ASTNode.ID_CLASS.equals(astNode.getGrammarClass())) {
				for (ASTNode current = getFirst(); current != null && current.getNext() != null && ASTNode.ID_CLASS.equals(current.getGrammarClass()); current = current.getNext().getNext()) {
		            if (astNode.getToken().equals(current.getToken())) {
		            	super.addChild(name, node);
		            	throw new CoreASMError("Variable \""+current.getToken()+"\" already defined in foreach rule.", node);
		            }
		        }
			}
		}
		super.addChild(name, node);
	}

    /**
     * Returns a map of the variable names to the nodes which
     * represent the domains that variable should be taken from
     */
    public Map<String,ASTNode> getVariableMap() {
    	if (variableMap != null)
    		return variableMap;
    	return variableMap = new VariableMap(this);
    }
    
    /**
     * Returns the node representing the 'do' part of the foreach rule.
     */
    public ASTNode getDoRule() {
        return (ASTNode)getChildNode("rule");   
    }
    
    /**
     * Returns the node representing the 'ifnone' part of the foreach rule.
     */
    public ASTNode getIfnoneRule() {
        return (ASTNode)getChildNode("ifnone");
    }
    
    /**
     * Returns the node representing the 'with' part of the foreach rule.
     * If there is no 'with' condition specified, null is returned.
     */
    public ASTNode getCondition() {
    	return (ASTNode)getChildNode("guard"); 
    }

}
