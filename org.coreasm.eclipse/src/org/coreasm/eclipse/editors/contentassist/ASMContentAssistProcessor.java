package org.coreasm.eclipse.editors.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Declaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.DerivedFunctionDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.EnumerationDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.FunctionDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.RuleDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.UniverseDeclaration;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.util.IconManager;
import org.coreasm.engine.Specification;
import org.coreasm.engine.Specification.BackgroundInfo;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.Specification.UniverseInfo;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

public class ASMContentAssistProcessor implements IContentAssistProcessor {
	
	private final ASMEditor editor;
	private final IContentAssistProcessor templateProcessor;
	
	public ASMContentAssistProcessor(ASMEditor editor, IContentAssistProcessor templateProcessor) {
		this.editor = editor;
		this.templateProcessor = templateProcessor;
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		proposals.addAll(Arrays.asList(templateProcessor.computeCompletionProposals(viewer, offset)));
		IDocument document = viewer.getDocument();
		try {
			if (document.getChar(offset - 1) == '(')
				offset--;
			int end = offset;
			while (offset > 0 && Character.isLetterOrDigit(document.getChar(offset - 1)))
				offset--;
			String prefix = document.get(offset, end - offset);
			for (Declaration declaration : ASMDeclarationWatcher.getDeclarations(editor.getInputFile(), true)) {
				if (declaration.getName().startsWith(prefix)) {
					Image icon = null;
					String params = "";
					String range = "";
					if (declaration instanceof RuleDeclaration) {
						RuleDeclaration ruleDeclaration = (RuleDeclaration)declaration;
						icon = IconManager.getIcon("/icons/editor/rule.gif");
						for (String param : ruleDeclaration.getParams()) {
							if (!params.isEmpty())
								params += ", ";
							params += param;
						}
					}
					else if (declaration instanceof DerivedFunctionDeclaration) {
						DerivedFunctionDeclaration derivedFunctionDeclaration = (DerivedFunctionDeclaration)declaration;
						icon = IconManager.getIcon("/icons/editor/sign.gif");
						for (String param : derivedFunctionDeclaration.getParams()) {
							if (!params.isEmpty())
								params += ", ";
							params += param;
						}
					}
					else if (declaration instanceof FunctionDeclaration) {
						FunctionDeclaration functionDeclaration = (FunctionDeclaration)declaration;
						icon = IconManager.getIcon("/icons/editor/sign.gif");
						for (String param : functionDeclaration.getDomain()) {
							if (!params.isEmpty())
								params += ", ";
							params += param;
						}
						range = " : " + functionDeclaration.getRange();
					}
					else if (declaration instanceof EnumerationDeclaration || declaration instanceof UniverseDeclaration) {
						icon = IconManager.getIcon("/icons/editor/sign.gif");
						if (declaration instanceof UniverseDeclaration)
							range = " : Universe";
						else
							range = " : Enumeration";
					}
					if (!params.isEmpty())
						params = "(" + params + ")";
					String file = "";
					if (declaration.getFile() != null && declaration.getFile() != editor.getInputFile())
						file = " - " + declaration.getFile().getProjectRelativePath();
					proposals.add(new CompletionProposal(declaration.getName() + params, offset, end - offset, declaration.getName().length() + params.length(), icon, declaration.getName() + params + range + file, null, null));
				}
			}
			Specification spec = editor.getSpec();
			if (spec != null) {
				for (FunctionInfo info : spec.getDefinedFunctions()) {
					if (info.name.startsWith(prefix)) {
						String params = "";
						String range = "";
						if (info.signature != null) {
							for (String param : info.signature.getDomain()) {
								if (!params.isEmpty())
									params += ", ";
								params += param;
							}
							range = " : " + info.signature.getRange();
						}
						if (!params.isEmpty())
							params = "(" + params + ")";
						proposals.add(new CompletionProposal(info.name + params, offset, end - offset, info.name.length() + params.length(), IconManager.getIcon("/icons/editor/package.gif"), info.name + params + range + " - " + info.plugin, null, null));
					}
				}
				for (BackgroundInfo info : spec.getDefinedBackgrounds()) {
					if (info.name.startsWith(prefix))
						proposals.add(new CompletionProposal(info.name, offset, end - offset, info.name.length(), IconManager.getIcon("/icons/editor/package.gif"), info.name + " : Background - " + info.plugin, null, info.plugin));
				}
				for (UniverseInfo info : spec.getDefinedUniverses()) {
					if (info.name.startsWith(prefix))
						proposals.add(new CompletionProposal(info.name, offset, end - offset, info.name.length(), IconManager.getIcon("/icons/editor/package.gif"), info.name + " : Universe - " + info.plugin, null, info.plugin));
				}
			}
		} catch (BadLocationException e) {
		}
		Collections.sort(proposals, new Comparator<ICompletionProposal>() {

			@Override
			public int compare(ICompletionProposal o1, ICompletionProposal o2) {
				return o1.getDisplayString().compareToIgnoreCase(o2.getDisplayString());
			}
		});
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
}
