package org.coreasm.engine.plugins.list;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/**
 * 
 * @author Michael Stegmaier
 *
 */
public class ListCompNode extends ASTNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private ASTNode dummyGuard = null;
	
	public ListCompNode(ScannerInfo info) {
		super(
				ListPlugin.PLUGIN_NAME,
				ASTNode.EXPRESSION_CLASS,
				"ListComprehension",
				null,
				info);
	}

	public ListCompNode(ListCompNode node) {
		super(node);
	}

	/**
	 * @return the first occurrence of the specifier variable
	 */
	public String getSpecifierVar() {
		// as the variable node is a TERM, we need to go two step down
		return this.getFirst().getToken();
	}
	
	/**
	 * @return the constrainer variable
	 */
	public String getConstrainerVar() {
		return this.getFirst().getNext().getToken();
	}
	
	/**
	 * @return the node referring to the domain of the list comprehension
	 */
	public ASTNode getDomain() {
		return this.getFirst().getNext().getNext();
	}
	
	/**
	 * @return the guard node
	 */
	public ASTNode getGuard() {
		ASTNode guard = this.getDomain().getNext();
		if (guard != null)
			return guard;
		else {
			if (dummyGuard == null)
				dummyGuard = new TrueGuardNode(this);
	    	return dummyGuard;
		}
	}
}
