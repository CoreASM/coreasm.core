package org.coreasm.eclipse.launch;

import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.eclipse.engine.driver.EngineDriver;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;


public class LaunchShortcut implements ILaunchShortcut {

	private Shell shell;

	public void launch(ISelection selection, String mode) {
		IFile file=null;
		IPath fullpath=null;
		if (selection instanceof IStructuredSelection) {
			try {
				file=(IFile)((IStructuredSelection)selection).getFirstElement(); // We have a guarantee that the list length is 1 (see plugin.xml)
				fullpath=file.getLocation();
				doLaunch(fullpath,mode);
			} catch (CoreException e) {
				warn(e.getMessage());
			} catch (Exception e) {
				abort("Cannot launch "+(fullpath!=null?fullpath.toString():(file!=null?file.toString():" specification.")),e);
			}
		}
	}

	private void doLaunch(IPath fullpath, String mode) throws CoreException {
//		System.out.println("Launching "+fullpath+" in mode "+mode);
		if (ILaunchManager.RUN_MODE.equals(mode)) {
			String fp=fullpath.toOSString();
			EngineDriver.newLaunch(fp);
		}
		else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			String fp=fullpath.toOSString();
			EngineDebugger.newLaunch(fp);
		}
	}

	private void abort(String msg, Exception e) {
		MessageDialog.openError(getShell(), "CoreASM Specification Launch", msg+"\n\n("+e+")");	
	}

	private void warn(String msg) {
		MessageDialog.openWarning(getShell(), "CoreASM Specification Launch", msg);	
	}

	private Shell getShell() {
		if (shell==null)
			shell=new Shell();
		return shell;
	}

	public void launch(IEditorPart editor, String mode) {
		if (editor.isDirty()) {
			editor.doSave(null); // TODO ask the user before saving
		}
		IEditorInput input=editor.getEditorInput();
		IFile file=(IFile)input.getAdapter(IFile.class);
		IPath fullpath=file.getLocation();
		try {
			doLaunch(fullpath,mode);
		} catch (CoreException e) {
			warn(e.getMessage());
		} catch (Exception e) {
			abort("Cannot launch "+(fullpath!=null?fullpath.toString():(file!=null?file.toString():" specification.")),e);
		}
	}

}
