package org.coreasm.eclipse.editors.warnings;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.conditionalrule.ConditionalRuleNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;

/**
 * This WarningRecognizer searches an ASM document for dangling elses.
 * @author Michael Stegmaier
 *
 */
public class DanglingElseWarningRecognizer implements IWarningRecognizer {

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
						if (node instanceof ConditionalRuleNode) {
							ConditionalRuleNode conditionalRuleNode = (ConditionalRuleNode)node;
							if (conditionalRuleNode.getParent() instanceof ConditionalRuleNode && conditionalRuleNode.getElseRule() != null) {
								ConditionalRuleNode parent = (ConditionalRuleNode)conditionalRuleNode.getParent();
								if (conditionalRuleNode != parent.getElseRule() && !"endif".equals(getLastChildNode(conditionalRuleNode).getToken())) {
									Node elseKeyWord = findElseKeyWord(conditionalRuleNode);
									if (elseKeyWord != null)
										warnings.add(new DanglingElseWarning(elseKeyWord, document));
								}
							}
						}
						fringe.addAll(node.getAbstractChildNodes());
					}
				}
			}
		}
		
		return warnings;
	}
	
	private static final Node findElseKeyWord(ConditionalRuleNode conditionalRuleNode) {
		for (Node node : conditionalRuleNode.getChildNodes()) {
			if ("else".equals(node.getToken()))
				return node;
		}
		return null;
	}
	
	private static final Node getLastChildNode(ConditionalRuleNode conditionalRuleNode) {
		Node last = null;
		for (Node node : conditionalRuleNode.getChildNodes())
			last = node;
		return last;
	}
}