package org.coreasm.eclipse.editors.quickfix.proposals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Creates a rule with the given name.
 * @author Michael Stegmaier
 *
 */
public class CreateRuleProposal implements ICompletionProposal {
	private final String name;
	private final Image image;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private Point selection;
	
	public CreateRuleProposal(String name) {
		this(name, null, null, null);
	}
	
	public CreateRuleProposal(String name, Image image) {
		this(name, image, null, null);
	}
	
	public CreateRuleProposal(String name, Image image, IContextInformation contextInformation, String additionalProposalInfo) {
		this.name = name;
		this.image = image;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		try {
			int offset = document.getLength();
			String declarationString = "\n\nrule " + name + " =\n\t";
			
			document.replace(offset, 0, declarationString + "skip");
			
			selection = new Point(offset + declarationString.length(), "skip".length());
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
		return "Create Rule '" + name + "'";
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
