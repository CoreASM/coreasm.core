package org.coreasm.engine.plugins.activation;

import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

public class ActivationNode extends ASTNode {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActivationNode(ScannerInfo info) {
		super(
				ActivationPlugin.PLUGIN_NAME,
				ASTNode.RULE_CLASS,
				"ActivationRule",
				null,
				info);
	}
	
	/**
	 * @param node
	 */
	public ActivationNode(ActivationNode node) {
		super(node);
	}
	
	/**
     * Returns an unevaluated term node if any. If none exists, <code>null<code> is returned (generic version)
     * 
     * @return <code>Node</code> representing a term node that has not been evaluated. If no such child exists, null is returned. 
     */
    public ASTNode getUnevaluatedTerm() {
        
    		// get first child
    		ASTNode child = this.getFirst();
    		
    		// while the current child exists and has been evaluated, cycle to the next child
    		while (child != null && child.isEvaluated())
    		{
    			child = child.getNext();
    		}
    		
    		// null will be returned when no children are left unevaluated, otherwise and unevaluated
    		// child node will be returned.
    		return child;
    }
    
    /**
     * Checks if this node represents an activate or deactive-node.
     * 
     * @return true, if this node is an 'activate' node, false if node is an 'deactivate' node.
     */
    public boolean isActivate() {
    	return (this.children.get(0).node.getToken().equals("activate"));
    }
    
    /**
     * Returns the location of the trigger variable.
     * 
     * @return <code>Location</code> of the trigger variable.
     */
	public Location getLocation()
	{
		Location loc = this.getFirst().getLocation();
		
		if (loc == null) {
			ActivationPlugin.logger.warn("Performing (de-)activation on non-location!");
		}
		
		return loc;
	}
}
