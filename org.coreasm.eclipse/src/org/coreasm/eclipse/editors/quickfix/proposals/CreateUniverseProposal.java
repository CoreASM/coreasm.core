package org.coreasm.eclipse.editors.quickfix.proposals;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.signature.UniverseNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Creates a universe declaration with the given name.
 * @author Michael Stegmaier
 *
 */
public class CreateUniverseProposal implements ICompletionProposal {
	private final String name;
	private final Image image;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private Point selection;
	
	public CreateUniverseProposal(String name) {
		this(name, null, null, null);
	}
	
	public CreateUniverseProposal(String name, Image image) {
		this(name, image, null, null);
	}
	
	public CreateUniverseProposal(String name, Image image, IContextInformation contextInformation, String additionalProposalInfo) {
		this.name = name;
		this.image = image;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		if (!(document instanceof ASMDocument))
			return;
		ASMDocument asmDocument = (ASMDocument)document;
		Node rootNode = asmDocument.getRootnode();
		ASTNode nodeToAddAfter = null;
		for (ASTNode node = ((ASTNode)rootNode).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if (nodeToAddAfter == null)
					nodeToAddAfter = node;
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						if (signature instanceof UniverseNode)
							nodeToAddAfter = signature;
					}
				}
			}
		}
		try {
			if (nodeToAddAfter != null) {
				int line = asmDocument.getLineOfNode(nodeToAddAfter) + 1;
				int offset = document.getLineOffset(line);
				String declarationString = "universe " + name;
				
				if (!(nodeToAddAfter instanceof UniverseNode))
					declarationString = '\n' + declarationString;
				
				document.replace(offset, 0, declarationString + "\n");
				
				selection = new Point(offset + declarationString.length(), 0);
			}
		} catch (BadLocationException e) {
		}
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
		return "Create Universe '" + name + "'";
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
