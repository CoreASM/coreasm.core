package org.coreasm.eclipse.editors.quickfix;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.coreasm.eclipse.editors.ASMEditor;

public class ASMMarkerResolutionGenerator implements IMarkerResolutionGenerator {

	private static class ASMMarkerResolution implements IMarkerResolution {
		private ICompletionProposal proposal;
		
		public ASMMarkerResolution(ICompletionProposal proposal) {
			this.proposal = proposal;
		}
		
		@Override
		public String getLabel() {
			return proposal.getDisplayString();
		}

		@Override
		public void run(IMarker marker) {
			IEditorPart editor = getEditor(marker);
			if (editor == null) {
				IResource resource = marker.getResource();
				if (resource instanceof IFile) {
					try {
						editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile)marker.getResource(), true);
						if (editor instanceof ITextEditor) {
							int start = MarkerUtilities.getCharStart(marker);
							int end = MarkerUtilities.getCharEnd(marker);
							((ITextEditor)editor).selectAndReveal(start, end - start);
						}
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
				
			}
			if (editor instanceof ASMEditor)
				proposal.apply(((ASMEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput()));
		}
		
		private static IEditorPart getEditor(IMarker marker) {
			IResource resource = marker.getResource();
			if (resource instanceof IFile) {
				IEditorInput input = new FileEditorInput((IFile)resource);
				
				if (input != null) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					if (page != null)
						return page.findEditor(input);
				}
			}
			return null;
		}
	}
	
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		ArrayList<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		ASMQuickAssistProcessor.collectProposals(marker, proposals);
		
		for (ICompletionProposal proposal : proposals)
			resolutions.add(new ASMMarkerResolution(proposal));
		
		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}
}
