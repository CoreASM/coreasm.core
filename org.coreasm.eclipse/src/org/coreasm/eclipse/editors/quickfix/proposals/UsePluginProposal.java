package org.coreasm.eclipse.editors.quickfix.proposals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Creates a use clause for the given plugin name.
 * @author Michael Stegmaier
 *
 */
public class UsePluginProposal implements ICompletionProposal {
	private final String name;
	private final Image image;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private Point selection;
	
	public UsePluginProposal(String name) {
		this(name, null, null, null);
	}
	
	public UsePluginProposal(String name, Image image) {
		this(name, image, null, null);
	}
	
	public UsePluginProposal(String name, Image image, IContextInformation contextInformation, String additionalProposalInfo) {
		this.name = name;
		this.image = image;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	@Override
	public void apply(IDocument document) {
		if (!(document instanceof ASMDocument))
			return;
		int offset = 0;
		boolean insertEmptyLine = true;
		ASMDocument asmDocument = (ASMDocument)document;
		Node rootNode = asmDocument.getRootnode();
		try {
			if (rootNode != null) {
				ASTNode nodeToAddAfter = null;
				for (ASTNode node = ((ASTNode)rootNode).getFirst(); node != null; node = node.getNext()) {
					if (nodeToAddAfter == null)
						nodeToAddAfter = node;
					if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
						if (Kernel.GR_USE_CLAUSE.equals(node.getGrammarRule())) {
							insertEmptyLine = false;
							nodeToAddAfter = node;
						}
					}
				}
				if (nodeToAddAfter != null)
					offset = document.getLineOffset(asmDocument.getLineOfNode(nodeToAddAfter) + 1);
			}
			else {
				Pattern usePattern = Pattern.compile("^[\\s]*[uU][sS][eE][\\s]+");
				for (int i = 0; i < document.getNumberOfLines(); i++) {
					int pos = document.getLineOffset(i);
					String line = document.get(pos, document.getLineLength(i));
					Matcher useMatcher = usePattern.matcher(line);
					if (useMatcher.find()) {
						insertEmptyLine = false;
						offset = pos;
					}
				}
			}
			if (offset <= 0) {
				for (int i = 0; i < document.getNumberOfLines(); i++) {
					offset = document.getLineOffset(i);
					String line = document.get(offset, document.getLineLength(i));
					if (line.startsWith("init"))
						break;
				}
			}
			String useClause = "use " + name;
			if (insertEmptyLine)
				useClause += "\n";
			document.replace(offset, 0, useClause + "\n");
			selection = new Point(offset + useClause.length(), 0);
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
		return "Use '" + name + "'";
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
