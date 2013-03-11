package org.coreasm.eclipse.editors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.coreasm.eclipse.editors.errors.AbstractError;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.TextInvocationContext;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * The ASMQuickAssistProcessor handles quick fixes.
 * @author Michael Stegmaier
 *
 */
public class ASMQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canFix(Annotation annotation) {
		if (annotation instanceof MarkerAnnotation)
			return canFix(((MarkerAnnotation)annotation).getMarker());
		return false;
	}
	
	private boolean canFix(IMarker marker) {
		try {
			return ASMEditor.MARKER_TYPE_WARNING.equals(marker.getType());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		ISourceViewer viewer = invocationContext.getSourceViewer();
		TextInvocationContext context = new TextInvocationContext(viewer, invocationContext.getOffset(), (viewer != null ? viewer.getSelectedRange().y : 0));
		IAnnotationModel model = viewer.getAnnotationModel();
		
		if (model == null)
			return null;
		
		List<ICompletionProposal> proposals = computeProposals(context, model);
		
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
	
	@SuppressWarnings("unchecked")
	private List<ICompletionProposal> computeProposals(IQuickAssistInvocationContext context, IAnnotationModel model) {
		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Iterator<Annotation> iterator = model.getAnnotationIterator();
		while (iterator.hasNext()) {
			Annotation annotation = iterator.next();
			if (canFix(annotation)) {
				if (isAtPosition(context.getOffset(), model.getPosition(annotation)))
					collectProposals(annotation, proposals);
			}
		}
		return proposals;
	}
	
	private boolean isAtPosition(int offset, Position pos) {
		return pos != null && offset >= pos.getOffset() && offset <= pos.getOffset() + pos.getLength();
	}
	
	private void collectProposals(Annotation annotation, List<ICompletionProposal> proposals) {
		if (annotation instanceof MarkerAnnotation)
			collectProposals(((MarkerAnnotation)annotation).getMarker(), proposals);
	}
	
	private void collectProposals(IMarker marker, List<ICompletionProposal> proposals) {
		try {
			if (ASMEditor.MARKER_TYPE_WARNING.equals(marker.getType())) {
				String message = MarkerUtilities.getMessage(marker);
				int start = MarkerUtilities.getCharStart(marker);
				int end = MarkerUtilities.getCharEnd(marker);
				if (message.startsWith("Undefined identifier encountered:")) {
					String declarations = getDeclarations(marker.getResource().getProject(), marker.getResource(), new HashSet<IResource>());
					for (String declaration : declarations.split("\u25c9")) {
						String functionName = null;
						String type = declaration.trim().substring(0, declaration.indexOf(':'));
						functionName = declaration.substring(type.length() + 2);
						if ("Universe".equals(type) || "Enumeration".equals(type))
							functionName = functionName.substring(0, functionName.indexOf('=')).trim();
						else if ("Derived Function".equals(type) || "Rule".equals(type)) {
							int indexOfNewline = functionName.indexOf('\n');
							if (indexOfNewline >= 0)
								functionName = functionName.substring(0, indexOfNewline);
							int indexOfBracket = functionName.indexOf('(');
							if (indexOfBracket >= 0)
								functionName = functionName.substring(0, indexOfBracket);
						}
						else if ("Enumeration member".equals(type))
							functionName = functionName.substring(functionName.indexOf('(') + 1, functionName.indexOf(')'));
						else if ("Function".equals(type))
							functionName = functionName.substring(0, functionName.indexOf(':'));
						if (functionName != null) {
							if (isSimilar(functionName, message.substring(message.indexOf(':') + 2)))
								proposals.add(new CompletionProposal(functionName, start, end - start, 0));
						}
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getDeclarations(IProject project, IResource file, Set<IResource> includedFiles) {
		if (includedFiles.contains(file))
			return "";
		includedFiles.add(file);
		String declarations = "";
		try {
			IMarker[] declarationMarker = file.findMarkers(ASMEditor.MARKER_TYPE_DECLARATIONS, false, IResource.DEPTH_ZERO);
			if (declarationMarker.length > 0)
				declarations = declarationMarker[0].getAttribute("declarations", "");
			IMarker[] includeMarker = file.findMarkers(ASMEditor.MARKER_TYPE_INCLUDE, false, IResource.DEPTH_ZERO);
			if (includeMarker.length > 0) {
				for (String include : includeMarker[0].getAttribute("includes", "").split(AbstractError.SEPERATOR_VAL)) {
					if (declarations.length() > 0 && !declarations.endsWith("\u25c9"))
						declarations += '\u25c9';
					declarations += getDeclarations(project, project.getFile(include), includedFiles);
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return declarations;
	}
	
	private boolean isSimilar(String string, String anotherString) {
		string = string.toLowerCase();
		anotherString = anotherString.toLowerCase();
		
		int sum = 0;
		int lengthString = string.length();
		int lengthAnotherString = anotherString.length();
		int minLength = lengthString;
		int maxLength = lengthAnotherString;
		int lengthDiff = maxLength - minLength;
		
		if (minLength > maxLength) {
			int tmp = minLength;
			minLength = maxLength;
			maxLength = tmp;
			lengthDiff = -lengthDiff;
		}
		for (int i = 0; i < minLength; i++)
			sum += Math.abs(string.charAt(i) - anotherString.charAt(i));
		if (minLength != maxLength) {
			int sum2 = 0;
			for (int i = 1; i < minLength + 1; i++)
				sum2 += Math.abs(string.charAt(lengthString - i) - anotherString.charAt(lengthAnotherString - i));
			if (sum2 < sum)
				sum = sum2;
		}
		sum += lengthDiff;
		
		return sum <= minLength;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
