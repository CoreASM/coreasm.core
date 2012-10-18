package org.coreasm.engine.interpreter;

import org.coreasm.engine.absstorage.Element;

/**
 *
 * The listener interface for receiving "interesting" interpreter events (node evaluation).
 *
 * The class that is interested in processing a interpreter event either implements this interface (and all the methods it contains).
 *
 * The listener object created from that class is then registered using the ControlAPI's <code>addInterpreterListener</code> method. An interpreter event is generated when the interpreter is evaluating a node. When an interpreter event occurs, the relevant method in the listener object is invoked, and the node is passed to it.
 * 
 * @author Michael Stegmaier
 * @see InterpreterImp#executeTree()
 */
public interface InterpreterListener {
	/**
	 * Invoked before the <code>pos</code> is evaluated by the interpreter.
	 */
    public void beforeNodeEvaluation(ASTNode pos);
    /**
	 * Invoked after the <code>pos</code> has been evaluated by the interpreter.
	 */
    public void afterNodeEvaluation(ASTNode pos);
    /**
     * Invoked on initiating the execution of <code>program</code> by <code>agent</code>.
     */
    public void initProgramExecution(Element agent, Element program);
}
