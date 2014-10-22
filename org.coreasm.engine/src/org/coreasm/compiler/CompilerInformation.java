package org.coreasm.compiler;

import org.coreasm.engine.interpreter.ASTNode;

/**
 * Contains additional information carried over between
 * different compilation steps.
 * Currently only contains the root node of the CoreASM specification,
 * but could be extended with more information.
 * @author Markus Brenner
 *
 */
public class CompilerInformation {
	/**
	 * The root node of the CoreASM specification
	 */
	public ASTNode root;
}
