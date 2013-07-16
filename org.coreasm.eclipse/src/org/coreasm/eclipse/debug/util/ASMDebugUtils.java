package org.coreasm.eclipse.debug.util;

import java.io.File;
import java.util.HashMap;

import org.coreasm.eclipse.debug.ui.views.ASMUpdateViewElement;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.Specification;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
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
	
	public static String getFileName(Update update, ControlAPI capi) {
		if (capi != null) {
			Parser parser = capi.getParser();
			CharacterPosition charPos = ((ScannerInfo)update.sources.toArray()[0]).getPos(parser.getPositionMap());
			if (charPos != null) {
				Specification spec = capi.getSpec();
				if (spec != null) {
					String fileName = spec.getLine(charPos.line).fileName;
					int lastIndexOfSeperator = fileName.lastIndexOf(File.separator);
					if (lastIndexOfSeperator >= 0)
						fileName = fileName.substring(lastIndexOfSeperator + 1);
					return fileName;
				}
			}
		}
		return null;
	}
	
	public static int getLineNumber(Update update, ControlAPI capi) {
		if (capi != null) {
			Parser parser = capi.getParser();
			CharacterPosition charPos = ((ScannerInfo)update.sources.toArray()[0]).getPos(parser.getPositionMap());
			if (charPos != null) {
				Specification spec = capi.getSpec();
				int line = charPos.line;
				if (spec != null)
					line = spec.getLine(charPos.line).line;
				return line;
			}
		}
		return -1;
	}
	
	public static String getFileName(Node node, ControlAPI capi) {
		if (capi != null) {
			Parser parser = capi.getParser();
			CharacterPosition charPos = node.getCharPos(parser);
			if (charPos == null && node != null && node.getScannerInfo() != null)
				charPos = node.getScannerInfo().getPos(parser.getPositionMap());
			if (charPos != null) {
				Specification spec = capi.getSpec();
				if (spec != null) {
					String fileName = spec.getLine(charPos.line).fileName;
					int lastIndexOfSeperator = fileName.lastIndexOf(File.separator);
					if (lastIndexOfSeperator >= 0)
						fileName = fileName.substring(lastIndexOfSeperator + 1);
					return fileName;
				}
			}
		}
		return null;
	}
	
	public static int getLineNumber(Node node, ControlAPI capi) {
		if (capi != null) {
			Parser parser = capi.getParser();
			CharacterPosition charPos = node.getCharPos(parser);
			if (charPos == null && node != null && node.getScannerInfo() != null)
				charPos = node.getScannerInfo().getPos(parser.getPositionMap());
			if (charPos != null) {
				Specification spec = capi.getSpec();
				int line = charPos.line;
				if (spec != null)
					line = spec.getLine(charPos.line).line;
				return line;
			}
		}
		return -1;
	}
	
	private static int indexOfCasmFilename(String context) {
		int index;
		if (context.contains(".coreasm") && context.indexOf(".coreasm") != context.indexOf(".coreasm."))
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
		
		beginIndex = context.lastIndexOf(File.separator);
		
		if (beginIndex >= 0)
			context = context.substring(beginIndex + 1);
		
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
