package org.coreasm.eclipse.util;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.preferences.PreferenceConstants;

public final class Utilities {
	public static void createMarker(String markerType, String filename, int line, int column, int length, Map<String, Object> attributes) {
		try {
			final IWorkbenchWindow[] pointer = new IWorkbenchWindow[1];
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					pointer[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				}
			});
			IWorkbenchWindow win = pointer[0];
			
			IWorkbenchPage page = win.getActivePage();
			if (page != null) {
				ASMEditor editor = (ASMEditor)page.getActiveEditor();
				ASMDocument doc = (ASMDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
				attributes.put(IMarker.LOCATION, filename);
				int start = doc.getLineOffset(line - 1) + column - 1;
				MarkerUtilities.setCharStart(attributes, start);
				MarkerUtilities.setCharEnd(attributes, start + length);
				MarkerUtilities.createMarker(((IFileEditorInput)page.getActiveEditor().getEditorInput()).getFile(), attributes, markerType);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void removeMarkers(String markerType) {
		final IWorkbenchWindow[] pointer = new IWorkbenchWindow[1];
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				pointer[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			}
		});
		IWorkbenchWindow win = pointer[0];
		
		IWorkbenchPage page = win.getActivePage();
		if (page != null)
			((ASMEditor)page.getActiveEditor()).removeMarkers(markerType);
	}

	public static String getAdditionalPluginsFolders() {
		IPreferenceStore prefStore = CoreASMPlugin.getDefault().getPreferenceStore();
		return prefStore.getString(PreferenceConstants.ADDITIONAL_PLUGINS_FOLDERS);
	}
}
