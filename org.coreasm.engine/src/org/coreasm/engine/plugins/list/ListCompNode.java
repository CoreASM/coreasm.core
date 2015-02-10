package org.coreasm.engine.plugins.list;

import java.util.LinkedHashMap;
import java.util.Map;

import org.coreasm.engine.EngineException;
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
	private static final long serialVersionUID = 1L;
	
	private ASTNode dummyGuard = null;
	private Map<String,ASTNode> varMapCache = null;
	
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
	 * @return the specifier function
	 */
	public ASTNode getListFunction() {
		return this.getFirst();
	}
	
	public Map<String,ASTNode> getVarBindings() throws EngineException {
		if (varMapCache == null) {
			ASTNode curVar = getListFunction().getNext();
			ASTNode curDomain = curVar.getNext();
			varMapCache = new LinkedHashMap<String,ASTNode>();
			
			while (curDomain != null) {
				if (varMapCache.containsKey(curVar)) 
					throw new EngineException("No two constrainer variables may have the same name.");
				
				varMapCache.put(curVar.getToken(), curDomain);
				curVar = curDomain.getNext();
				if (curVar == null)
					curDomain = null;
				else
					curDomain = curVar.getNext();
			}
		}
		
		return varMapCache;
	}
	
	/**
	 * @return the guard node
	 */
	public ASTNode getGuard() {
		// starting from the fist variable binding
		ASTNode guard = getListFunction().getNext();
		
		while (guard != null && guard.getNext() != null) {
			// bypassing variable bindings couples
			guard = guard.getNext().getNext();
		}
		
		// guard is optional, so it may be null
		if (guard != null)
			return guard;
		else {
			if (dummyGuard == null)
				dummyGuard = new TrueGuardNode(this);
	    	return dummyGuard;
		}
	}

}
