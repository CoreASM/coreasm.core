package org.coreasm.compiler.plugins.kernel.code.bcode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.compiler.plugins.kernel.CompilerKernelPlugin;
import org.coreasm.engine.interpreter.ASTNode;

public class KernelCoreHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		List<ASTNode> children = new ArrayList<ASTNode>();
		children.addAll(node.getAbstractChildNodes());

		// Find the id node and drop all use nodes
		ASTNode id = null;
		for (int i = 0; i < children.size(); i++) {
			ASTNode cnode = children.get(i);
			if (cnode.getGrammarRule().equals("ID")) {
				if (id != null)
					throw new CompilerException(
							"only one id node allowed");
				id = cnode;
				children.remove(i);
				i--;
			}
			if (cnode.getGrammarRule().equals("UseClauses")) {
				children.remove(i);
				i--;
			}
		}
		if (id == null)
			throw new CompilerException(
					"Couldn't find id node for init rule");

		// request basic code for all other nodes
		for (Iterator<ASTNode> it = children.iterator(); it.hasNext();) {
			engine.compile((ASTNode) it.next(), CodeType.BASIC);
		}
	}

}
