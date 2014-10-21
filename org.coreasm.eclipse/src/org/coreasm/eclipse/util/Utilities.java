package org.coreasm.eclipse.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMIncludeWatcher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.MarkerUtilities;

public final class Utilities {
	
	private static ArrayList<OutlineContentProvider> outlineContentProviders = new ArrayList<OutlineContentProvider>();
	
	public static IEditorPart openEditor(IFile file) throws PartInitException {
		return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true);
	}
	
	public static IEditorPart getEditor(Object object) {
		if (object instanceof IFileEditorInput)
			return getEditor((IFileEditorInput)object);
		if (object instanceof IResource)
			return getEditor((IResource)object);
		if (object instanceof IMarker)
			return getEditor((IMarker)object);
		return null;
	}
	
	public static IEditorPart getEditor(IMarker marker) {
		return getEditor(marker.getResource());
	}
	
	public static IEditorPart getEditor(IResource resource) {
		if (resource instanceof IFile)
			return getEditor((IFile)resource);
		return null;
	}
	
	public static IEditorPart getEditor(String filename) {
		return getEditor(getFile(filename));
	}
	
	private static IFile getFile(String filename) {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
	}
	
	public static IEditorPart getEditor(IFile file) {
		if (file != null)
			return getEditor(new FileEditorInput(file));
		return null;
	}
	
	public static IEditorPart getEditor(IFileEditorInput input) {
		if (input != null) {
			final IWorkbenchWindow[] pointer = new IWorkbenchWindow[1];
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					pointer[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				}
			});
			IWorkbenchWindow window = pointer[0];
			IWorkbenchPage page = window.getActivePage();
			if (page != null)
				return page.findEditor(input);
		}
		return null;
	}
	
	public static Set<IFile> getIncludedFiles(IFile file, boolean transitive) {
		return ASMIncludeWatcher.getIncludedFiles(file, transitive);
	}
	
	public static void createMarker(String markerType, String filename, int line, int column, int length, Map<String, Object> attributes) {
		ASMEditor editor = (ASMEditor)getEditor(filename);
		
		if (editor != null) {
			try {
				ASMDocument doc = (ASMDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
				attributes.put(IMarker.LOCATION, filename);
				int start = doc.getLineOffset(line - 1) + column - 1;
				MarkerUtilities.setCharStart(attributes, start);
				MarkerUtilities.setCharEnd(attributes, start + length);
				MarkerUtilities.createMarker(((IFileEditorInput)editor.getEditorInput()).getFile(), attributes, markerType);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void removeMarkers(String markerType, String filename) {
		ASMEditor editor = (ASMEditor)getEditor(filename);
		
		if (editor != null)
			editor.removeMarkers(markerType);
	}

	public static ArrayList<OutlineContentProvider> getOutlineContentProviders() {
		return outlineContentProviders;
	}
	
	public static void addOutlineContentProvider(OutlineContentProvider provider) {
		if (!outlineContentProviders.contains(provider))
			outlineContentProviders.add(provider);
	}
	
	public static void removeOutlineContentProvider(OutlineContentProvider provider) {
		outlineContentProviders.remove(provider);
	}
}
