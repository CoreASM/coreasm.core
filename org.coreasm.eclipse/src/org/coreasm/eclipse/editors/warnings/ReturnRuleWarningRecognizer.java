package org.coreasm.eclipse.editors.warnings;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;

public class ReturnRuleWarningRecognizer implements IWarningRecognizer {

	@Override
	public List<AbstractWarning> checkForWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new LinkedList<AbstractWarning>();
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())
				|| "Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (node instanceof ReturnRuleNode) {
							ASTNode parent = node.getParent();
							while (parent instanceof LocalRuleNode || parent instanceof DerivedFunctionNode)
								parent = parent.getParent();
							if (parent != declarationNode) {
								ASTNode nameNode = null;
								if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule()))
									nameNode = declarationNode.getFirst().getFirst();
								else if ("Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
									DerivedFunctionNode derivedFunctionNode = (DerivedFunctionNode)declarationNode.getFirst();
									nameNode = derivedFunctionNode.getNameSignatureNode().getFirst();
								}
								if (nameNode != null)
									warnings.add(new ReturnUndefWarning(nameNode.getToken(), nameNode.getScannerInfo().charPosition, node.getScannerInfo().charPosition));
							}
						}
						fringe.addAll(node.getAbstractChildNodes());
					}
				}
			}
		}
		
		return warnings;
	}

}
