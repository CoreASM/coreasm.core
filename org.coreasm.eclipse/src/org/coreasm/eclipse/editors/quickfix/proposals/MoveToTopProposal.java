package org.coreasm.eclipse.editors.quickfix.proposals;

import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class MoveToTopProposal implements ICompletionProposal {
	private final int nodeOffset;
	private final Image image;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private Point selection;
	
	public MoveToTopProposal(int nodeOffset) {
		this(nodeOffset, null, null, null);
	}
	
	public MoveToTopProposal(int nodeOffset, Image image) {
		this(nodeOffset, image, null, null);
	}
	
	public MoveToTopProposal(int nodeOffset, Image image, IContextInformation contextInformation, String additionalProposalInfo) {
		this.nodeOffset = nodeOffset;
		this.image = image;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		if (!(document instanceof ASMDocument))
			return;
		ASTNode node = getNodeOfOffset((ASMDocument)document, nodeOffset);
		try {
			if (node instanceof ReturnRuleNode) {
				ReturnRuleNode returnRuleNode = (ReturnRuleNode)node;
				int start = node.getScannerInfo().charPosition;
				int end = node.getFirstCSTNode().getNextCSTNode().getNextCSTNode().getScannerInfo().charPosition + 2;
				
				document.replace(start, end - start, "");
				
				while (node != null && !ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass()) && !(node instanceof LocalRuleNode))
					node = node.getParent();
				if (node != null) {
					ASTNode ruleBody = node.getFirst().getNext();
					int offset = ruleBody.getScannerInfo().charPosition;
					if (ruleBody instanceof ReturnRuleNode && ruleBody.getFirst().getNext() instanceof LocalRuleNode)
						offset = ruleBody.getFirst().getNext().getScannerInfo().charPosition;
					String returnStatement = "return " + returnRuleNode.getExpressionNode().unparseTree() + " in ";
					
					document.replace(offset, 0, returnStatement);
					
					selection = new Point(offset + returnStatement.length(), 0);
				}
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
						if (node instanceof ReturnRuleNode && node.getScannerInfo().charPosition == offset)
							return node;
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
		return "Move return statement to the top of this rule";
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
