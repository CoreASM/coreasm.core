package org.coreasm.eclipse.editors.quickfix.proposals;

import java.util.List;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Creates a function declaration with the given name.
 * @author Michael Stegmaier
 *
 */
public class CreateFunctionProposal implements ICompletionProposal {
	private final String name;
	private final List<String> domain;
	private final Image image;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private Point selection;
	
	public CreateFunctionProposal(String name, List<String> domain) {
		this(name, domain, null, null, null);
	}
	
	public CreateFunctionProposal(String name, List<String> domain, Image image) {
		this(name, domain, image, null, null);
	}
	
	public CreateFunctionProposal(String name, List<String> domain, Image image, IContextInformation contextInformation, String additionalProposalInfo) {
		this.name = name;
		this.domain = domain;
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
						if (signature instanceof FunctionNode)
							nodeToAddAfter = signature;
					}
				}
			}
		}
		try {
			if (nodeToAddAfter != null) {
				int line = asmDocument.getLineOfNode(nodeToAddAfter) + 1;
				int offset = document.getLineOffset(line);
				String declarationString = "function " + name + ": ";
				
				if (!(nodeToAddAfter instanceof FunctionNode))
					declarationString = '\n' + declarationString;
				
				if (domain != null) {
					for (String domainElement : domain) {
						if (!declarationString.endsWith(": "))
							declarationString += " * ";
						declarationString += domainElement;
					}
					if (!domain.isEmpty())
						declarationString += " ";
					declarationString += "-> ";
				}
				
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
		return "Create Function '" + name + "'";
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
