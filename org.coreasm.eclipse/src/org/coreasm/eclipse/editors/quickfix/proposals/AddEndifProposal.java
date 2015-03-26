package org.coreasm.eclipse.editors.quickfix.proposals;

import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.conditionalrule.ConditionalRuleNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Adds an 'endif' at the end of the given ConditionalRule.
 * @author Michael Stegmaier
 *
 */
public class AddEndifProposal implements ICompletionProposal {
	private final int nodeOffset;
	private final Image image;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private Point selection;
	
	public AddEndifProposal(int nodeOffset) {
		this(nodeOffset, null, null, null);
	}
	
	public AddEndifProposal(int nodeOffset, Image image) {
		this(nodeOffset, image, null, null);
	}
	
	public AddEndifProposal(int nodeOffset, Image image, IContextInformation contextInformation, String additionalProposalInfo) {
		this.nodeOffset = nodeOffset;
		this.image = image;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		if (!(document instanceof ASMDocument))
			return;
		ASMDocument asmDocument = (ASMDocument)document;
		ConditionalRuleNode conditionalRuleNode = getNodeOfOffset(asmDocument, nodeOffset);
		int offset = asmDocument.getNodePosition(conditionalRuleNode);
		int len = asmDocument.calculateLength(conditionalRuleNode);
		try {
			int lineOffset = document.getLineOffset(document.getLineOfOffset(offset));
			String indentation = document.get(lineOffset, offset - lineOffset).replaceAll("\\S", " ");
			String endif = "\n" + indentation + "endif";
			document.replace(offset + len, 0, endif);
			selection = new Point(offset + len + endif.length(), 0);
		} catch (BadLocationException e) {
		}
	}
		
	private ConditionalRuleNode getNodeOfOffset(ASMDocument document, int offset) {
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())
				|| "Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (node instanceof ConditionalRuleNode) {
							if (document.getNodePosition(node) == offset)
								return (ConditionalRuleNode)node;
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
		return "Close ConditionalRule with 'endif'";
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
