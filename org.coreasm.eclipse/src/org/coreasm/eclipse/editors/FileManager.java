package org.coreasm.eclipse.editors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.coreasm.util.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This class provides static helper method for providing information about files
 * within the Workspace
 * @author Markus M�ller
 *
 */
public class FileManager 
{
	private static final String INVALID_CHARS_WIN = "\\:*?\"<>|";
	private static final String INVALID_CHARS_MAC = ":";
	private static final String INVALID_CHARS_UNIX = "";
	private static final String INVALID_CHARS = getInvalidChars();
	
	/**
	 * Returns a reference to the project the currently edited file belongs to. 
	 */
	public static IProject getActiveProject()
	{
		WorbenchWindowRetriever wwr = new WorbenchWindowRetriever();
		Display.getDefault().syncExec(wwr);
		IWorkbenchWindow win = wwr.wbwin;
		
		IWorkbenchPage page = win.getActivePage();
		if (page == null)
			return null;
		// This check is necessary because there is no page when eclipse is starting
		// and is opening an editor which was open when eclipse was closed last time.
		// However, the document will be parsed again later, when there is a page.
		// Can this behavior be changed?
		
		FileEditorInput fileInput = (FileEditorInput) page.getActiveEditor().getEditorInput();
		return fileInput.getFile().getProject();
	}
	
	/**
	 * Returns a reference to the active editor.
	 */
	public static IEditorPart getActiveEditor()
	{
		WorbenchWindowRetriever wwr = new WorbenchWindowRetriever();
		Display.getDefault().syncExec(wwr);
		IWorkbenchWindow win = wwr.wbwin;
		
		IWorkbenchPage page = win.getActivePage();
		if (page == null) return null;
		
		return page.getActiveEditor();
	}
	
	
	private static String getInvalidChars()
	{
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0)
			return INVALID_CHARS_WIN;
		if (os.indexOf("mac") >= 0)
			return INVALID_CHARS_MAC;
		if (os.indexOf("nix") >= 0)
			return INVALID_CHARS_UNIX;
		return "";
	}

	/**
	 * Returns the project to which a certain file belongs to. 
	 */
	public static IProject getProject(IFile file)
	{
		return file.getProject();
	}
	
	/**
	 * Translates the name of a file, which is relative to another file, into a name
	 * which is relative to the project both files belong to. Returns null if the 
	 * filename leads out of the project.
	 */
	public static String getFilenameRelativeToProject(String filename, IFile sourcefile)
	{
		if (filename.startsWith("/"))
//			return filename;
			filename = filename.substring(1);
			
		IPath folder = sourcefile.getParent().getProjectRelativePath();
		
		while (filename.startsWith("../")) {
			// check if we're leaving the project root
			if (folder.lastSegment() ==  null)
				return null;
			folder = folder.removeLastSegments(1);
			filename = filename.substring(3);
		}
		
		String foldername = folder.toString();
		
		if (foldername == null || foldername.equals(""))
			return filename;
		else
			return foldername + "/" + filename;
		 
	}
	
	/**
	 * Returns an IFile object for a file given by its name within a project
	 * @param filename	the name of the file, relative to the project root
	 * @param project	a reference to the project to which the file belongs
	 * @return			the IFile object representing the file.
	 */
	public static IFile getFile(String filename, IProject project)
	{
		if (filename == null)
			return null;
		
		IFile file = project.getFile(filename);
		return file;
	}
	
	/**
	 * Creates a new file, given by its name, in a certain project, and fills it
	 * with a default content ("<code>// CoreASM specification</code>")
	 * @param filename	the name of the new file, relative to the project root
	 * @param project	a reference to the project to which the file belongs
	 * @return			the IFile object representing the file.
	 */
	public static IFile createFile(String filename, IProject project)
	{
		IFile newfile = project.getFile(filename);
		String text = "// CoreASM specification";
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			// create folders if needed
			boolean folderWasCreated = false;
			IPath path = newfile.getLocation().removeLastSegments(1);
			for (int i=path.segmentCount(); i>=0; i--) {
				IPath fPath = path.removeLastSegments(i);
				
				java.io.File jFile = new File(fPath.toString());
				if (jFile.exists() && jFile.isDirectory()==false)
					throw new RuntimeException("cannot create directory");
				if (!jFile.exists())
					folderWasCreated = jFile.mkdir();
			}
			
			if (folderWasCreated) {
				getActiveProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				Logger.log(Logger.INFORMATION, Logger.ui, "created directory: " + newfile.getProjectRelativePath().removeLastSegments(1).toString());	
			}
			
			newfile.create(is,  false, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return newfile;
	}
	
	/**
	 * Checks if a certain file, given by its IFile object, is existing
	 */
	public static boolean fileExists(IFile file)
	{
		if (file == null)
			return false;
		java.io.File jFile = file.getLocation().toFile();
		return jFile.exists();
	}
	
	/**
	 * Checks if a certain file, given by its filename and its object, is existing
	 * @param filename	the name of the file, relative to the project root
	 * @param project	a reference to the project
	 */
	public static boolean fileExits(String filename, IProject project)
	{
		return fileExists(getFile(filename, project));
	}
	
	/**
	 * Checks if a filename is valid within the current operating system.
	 */
	public static boolean isFilenameValid(String filename)
	{
		for (int i=0; i<filename.length(); i++)
			if (INVALID_CHARS.indexOf(filename.charAt(i)) >= 0)
				return false;
		return true;
	}
	
	/**
	 * Opens an editor for a file specified by its filename and its project.
	 */
	public static void openEditor(String filename, IProject project)
	{
		WorbenchWindowRetriever wwr = new WorbenchWindowRetriever();
		Display.getDefault().syncExec(wwr);
		IWorkbenchWindow win = wwr.wbwin;
		IWorkbenchPage page = win.getActivePage();

		IFile file = getFile(filename, project);
		
		if (!fileExists(file)) {
			MessageBox mb = new MessageBox(win.getShell(), SWT.ICON_ERROR);
			mb.setText("File not found");
			mb.setMessage("File \"" + filename + "\" was not found.");
			mb.open();
			return;
		}
		
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename);
		if (desc == null)
			return;
		try {
			page.openEditor(new FileEditorInput(file), desc.getId());
		} catch (PartInitException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Helper class for retrieving a reference to the active WorkbenchWindow
	 * which must be done from within the SWT thread, via (a)syncExec()
	 */
	private static class WorbenchWindowRetriever implements Runnable
	{
		IWorkbenchWindow wbwin;
		
		@Override
		public void run() {
			wbwin = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
	}
}


