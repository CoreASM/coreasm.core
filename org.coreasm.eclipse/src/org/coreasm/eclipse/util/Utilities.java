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
import org.coreasm.eclipse.editors.outlining.AbstractContentPage;
import org.coreasm.eclipse.editors.outlining.ParsedOutlinePage;
import org.coreasm.eclipse.editors.outlining.RootOutlineTreeNode;
import org.coreasm.eclipse.preferences.PreferenceConstants;

public final class Utilities {
	
	/**
	 * @return	The current editor from the workbench
	 */
	private static ASMEditor getCurrentEditor() {
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
			return (ASMEditor)page.getActiveEditor();
		}
		
		return null;
	}
	
	/**
	 * @param rootNode	Node which has to be removed to the outline
	 * 
	 * Removes a root node from the outline
	 */
	public static void removeExternOutlineRoot(RootOutlineTreeNode rootNode) {
		ASMEditor editor = getCurrentEditor();
		
		if (editor != null) {
			AbstractContentPage outlinePage = editor.getOutlinePage();
			if (outlinePage instanceof ParsedOutlinePage) {
				ParsedOutlinePage parsedOutlinePage = (ParsedOutlinePage) outlinePage;
				parsedOutlinePage.getContentProvider().removeRootNode(rootNode);
			}
		}
	}
	
	/**
	 * @param rootNode	Node which has to be added to the outline
	 * 
	 * Adds a root node to the outline
	 */
	public static void addExternOutlineRoot(RootOutlineTreeNode rootNode) {
		ASMEditor editor = getCurrentEditor();
		
		if (editor != null) {
			AbstractContentPage outlinePage = editor.getOutlinePage();
			if (outlinePage instanceof ParsedOutlinePage) {
				ParsedOutlinePage parsedOutlinePage = (ParsedOutlinePage) outlinePage;
				parsedOutlinePage.getContentProvider().addRootNode(rootNode);
			}
		}
	}
	
	public static void createMarker(String markerType, String filename, int line, int column, int length, Map<String, Object> attributes) {
		ASMEditor editor = getCurrentEditor();
		
		if (editor != null) {
			try {
				ASMDocument doc = (ASMDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
				attributes.put(IMarker.LOCATION, filename);
				int start = doc.getLineOffset(line - 1) + column - 1;
				MarkerUtilities.setCharStart(attributes, start);
				MarkerUtilities.setCharEnd(attributes, start + length);
				MarkerUtilities.createMarker(((IFileEditorInput)editor.getEditorInput()).getFile(), attributes, markerType);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void removeMarkers(String markerType) {
		ASMEditor editor = getCurrentEditor();
		
		if (editor != null)
			editor.removeMarkers(markerType);
	}

	public static String getAdditionalPluginsFolders() {
		IPreferenceStore prefStore = CoreASMPlugin.getDefault().getPreferenceStore();
		return prefStore.getString(PreferenceConstants.ADDITIONAL_PLUGINS_FOLDERS);
	}
}
