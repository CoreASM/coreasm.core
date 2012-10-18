package org.coreasm.eclipse.debug.util;

import java.util.HashMap;

import org.coreasm.eclipse.debug.ui.views.ASMUpdateViewElement;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Utilities for the ASM debugger
 * @author Michael Stegmaier
 *
 */
public class ASMDebugUtils {
	private ASMDebugUtils() {
	}
	
	private static int indexOfCasmFilename(String context) {
		int index;
		if (context.contains(".coreasm"))
			index = context.substring(0, context.indexOf(".coreasm")).lastIndexOf(' ') + 1;
		else if (context.contains(".casm"))
			index = context.substring(0, context.indexOf(".casm")).lastIndexOf(' ') + 1;
		else
			return -1;
		if (index < 0)
			return 0;
		return index;
	}
	
	public static String parseSourceName(String context) {
		int beginIndex = ASMDebugUtils.indexOfCasmFilename(context);
		
		if (beginIndex < 0)
			return null;
		
		context = context.substring(beginIndex);
		return context.substring(0, context.indexOf(":"));
	}
	
	public static int parseLineNumber(String context) {
		int beginIndex = ASMDebugUtils.indexOfCasmFilename(context);
		
		if (beginIndex < 0)
			return -1;
		
		context = context.substring(beginIndex);
		
		return Integer.parseInt(context.substring(context.indexOf(":") + 1, context.indexOf(",")));
	}
	
	private static IResource findFile(IResource res, String filename) {
		if (res == null)
			res = ResourcesPlugin.getWorkspace().getRoot();
		if (res.getName().equals(filename))
			return res;
		
		if (res instanceof IContainer) {
			try {
				for (IResource member : ((IContainer)res).members()) {
					IResource file = findFile(member, filename); 
					if (file != null)
						return file;
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static IFile findFile(String filename) {
		String path = null;
		EngineDebugger debugger = EngineDebugger.getRunningInstance();
		if (debugger != null)
			path = debugger.getSpecPath();
		IResource resource = null;
		if (path != null)
			resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		return (IFile)findFile(resource, filename);
	}
	
	/**
	 * Opens an editor for the specified ASM Update View element.
	 * @param updateViewElement element to open to view for
	 */
	public static void openEditor(ASMUpdateViewElement updateViewElement) {
		openEditor(updateViewElement.getSourceName(), updateViewElement.getLineNumber());
	}
	
	private static void openEditor(final String filename, final int lineNumber) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				IFile file = findFile(filename);
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if (file == null || page == null)
					return;
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				map.put(IMarker.LINE_NUMBER, lineNumber);
				IMarker marker;
				try {
					marker = file.createMarker(IMarker.TEXT);
					marker.setAttributes(map);
					IDE.openEditor(page, marker);
					marker.delete();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
