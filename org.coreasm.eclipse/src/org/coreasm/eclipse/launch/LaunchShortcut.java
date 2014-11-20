package org.coreasm.eclipse.launch;

import java.util.HashSet;

import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMIncludeWatcher;
import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.eclipse.util.Utilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * This class enables launch shortcuts.
 * @author Michael Stegmaier
 *
 */
public class LaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {
		launch((IFile)((IStructuredSelection)selection).getFirstElement(), mode);
	}

	public void launch(IEditorPart editor, String mode) {
		launch((IFile)editor.getEditorInput().getAdapter(IFile.class), mode);
	}

	private void launch(IFile file, String mode) {
		String project = IPath.SEPARATOR + file.getProject().getName();
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("org.coreasm.eclipse.launchConfigurationType");
		HashSet<IFile> consideredFiles = new HashSet<IFile>();
		while (isCoreModule(file)) {
			if (!consideredFiles.contains(file)) {
				IFile[] files = ASMIncludeWatcher.getIncludingFiles(file);
				for (int i = 0; i < files.length && isCoreModule(file); i++) {
					file = files[i];
					consideredFiles.add(file);
				}
			}
		}
		String spec = file.getFullPath().toString().replaceFirst(project, "").substring(1);
		try {
			for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations(type)) {
				if (project.equals(configuration.getAttribute(ICoreASMConfigConstants.PROJECT, (String)null))) {
					if (spec.equals(configuration.getAttribute(ICoreASMConfigConstants.SPEC, (String)null))) {
						DebugUITools.launch(configuration, mode);
						return;
					}
				}
			}
		} catch (CoreException e) {
			return;
		}
		
		try {
			ILaunchConfigurationWorkingCopy wCopy = type.newInstance(null, file.getName());
			LaunchCommon.setDefaults(wCopy);
			wCopy.setAttribute(ICoreASMConfigConstants.PROJECT, project);
			wCopy.setAttribute(ICoreASMConfigConstants.SPEC, spec);
			wCopy.setMappedResources(new IResource[] { file });
			ILaunchConfiguration configuration = wCopy.doSave();
			DebugUITools.launch(configuration, mode);
		} catch (CoreException e) {
			return;
		}
	}
	
	private static boolean isCoreModule(IFile file) {
		IEditorPart editor = Utilities.getEditor(file);
		if (editor instanceof ASMEditor) {
			ASMParser parser = ((ASMEditor)editor).getParser();
			if (parser.getRootNode() != null && "CoreModule".equals(parser.getRootNode().getGrammarRule()))
				return true;
		}
		return ASMIncludeWatcher.getIncludingFiles(file).length > 0;
	}
}
