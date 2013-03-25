package org.coreasm.eclipse.editors.quickfix.proposals;

import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.RuleOrFuncElementNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Adds the given identifier to the surrounding local block.
 * If there's no surrounding local block, it will create one.
 * @author Michael Stegmaier
 *
 */
public class MarkAsLocalProposal implements ICompletionProposal {
	private final String name;
	private final int identifierOffset;
	private final Image image;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private Point selection;
	
	public MarkAsLocalProposal(String name, int identifierOffset) {
		this(name, identifierOffset, null, null, null);
	}
	
	public MarkAsLocalProposal(String name, int identifierOffset, Image image) {
		this(name, identifierOffset, image, null, null);
	}
	
	public MarkAsLocalProposal(String name, int identifierOffset, Image image, IContextInformation contextInformation, String additionalProposalInfo) {
		this.name = name;
		this.identifierOffset = identifierOffset;
		this.image = image;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		if (!(document instanceof ASMDocument))
			return;
		ASTNode node = getNodeOfOffset((ASMDocument)document, identifierOffset);
		while (node != null && !ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass()) && !(node instanceof LocalRuleNode))
			node = node.getParent();
		try {
			if (node instanceof LocalRuleNode) {
				for (Node child : node.getChildNodes()) {
					if ("in".equals(child.getToken())) {
						document.replace(child.getScannerInfo().charPosition - 1, 0, ", " + name);
						return;
					}
				}
			}
			else if (node != null) {
				ASTNode ruleBody = node.getFirst().getNext();
				int offset = ruleBody.getScannerInfo().charPosition;
				String localBlock = "local " + name + " in ";
				
				document.replace(offset, 0, localBlock);
				
				selection = new Point(offset + localBlock.length(), 0);
			}
		} catch (BadLocationException e) {
		}
	}
		
	private ASTNode getNodeOfOffset(ASMDocument document, int offset) {
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())
				|| "Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (ASTNode.FUNCTION_RULE_CLASS.equals(node.getGrammarClass()) && node instanceof FunctionRuleTermNode) {
							FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
							if (frNode.hasName() && frNode.getScannerInfo().charPosition == offset)
								return frNode;
						}
						else if (ASTNode.EXPRESSION_CLASS.equals(node.getGrammarClass())) {
							if (node instanceof RuleOrFuncElementNode && node.getScannerInfo().charPosition + 1 == offset)
								return node;
						}
						fringe.addAll(node.getAbstractChildNodes());
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return additionalProposalInfo;
	}

	@Override
	public IContextInformation getContextInformation() {
		return contextInformation;
	}

	@Override
	public String getDisplayString() {
		return "Mark '" + name + "' as local in this rule";
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public Point getSelection(IDocument document) {
		return selection;
	}

}
