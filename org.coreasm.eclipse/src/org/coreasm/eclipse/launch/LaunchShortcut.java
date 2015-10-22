package org.coreasm.eclipse.launch;

import java.util.HashMap;
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
	
	private ILaunchConfiguration findLaunchConfiguration(IFile file, HashMap<String, ILaunchConfiguration> launchConfigurations, HashSet<IFile> consideredFiles) {
		if (consideredFiles.contains(file))
			return null;
		String project = IPath.SEPARATOR + file.getProject().getName();
		String spec = file.getFullPath().toString().replaceFirst(project, "").substring(1);
		ILaunchConfiguration launchConfiguration = launchConfigurations.get(spec);
		if (launchConfiguration != null)
			return launchConfiguration;
		consideredFiles.add(file);
		for (IFile parentFile : ASMIncludeWatcher.getIncludingFiles(file)) {
			launchConfiguration = findLaunchConfiguration(parentFile, launchConfigurations, consideredFiles);
			if (launchConfiguration != null)
				return launchConfiguration;
		}
		return null;
	}
	
	private IFile findMainSpecification(IFile file, HashSet<IFile> consideredFiles) {
		if (consideredFiles.contains(file))
			return null;
		if (isMainSpecification(file))
			return file;
		consideredFiles.add(file);
		for (IFile parentFile : ASMIncludeWatcher.getIncludingFiles(file)) {
			IFile mainSpecification = findMainSpecification(parentFile, consideredFiles);
			if (mainSpecification != null)
				return mainSpecification;
		}
		return null;
	}

	private void launch(IFile file, String mode) {
		String project = file.getProject().getName();
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("org.coreasm.eclipse.launchConfigurationType");
		
		HashMap<String, ILaunchConfiguration> launchConfigurations = new HashMap<String, ILaunchConfiguration>();
		
		try {
			for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations(type)) {
				if (project.equals(configuration.getAttribute(ICoreASMConfigConstants.PROJECT, (String)null)))
					launchConfigurations.put(configuration.getAttribute(ICoreASMConfigConstants.SPEC, (String)null), configuration);
			}
		} catch (CoreException e) {
			return;
		}
		
		ILaunchConfiguration launchConfiguration = findLaunchConfiguration(file, launchConfigurations, new HashSet<IFile>());
		if (launchConfiguration != null) {
			DebugUITools.launch(launchConfiguration, mode);
			return;
		}
		
		IFile mainSpec = findMainSpecification(file, new HashSet<IFile>());
		if (mainSpec != null)
			file = mainSpec;
		
		try {
			ILaunchConfigurationWorkingCopy wCopy = type.newInstance(null, file.getName());
			LaunchCommon.setDefaults(wCopy);
			wCopy.setAttribute(ICoreASMConfigConstants.PROJECT, file.getProject().getName());
			wCopy.setAttribute(ICoreASMConfigConstants.SPEC, file.getProjectRelativePath().toString());
			wCopy.setMappedResources(new IResource[] { file });
			ILaunchConfiguration configuration = wCopy.doSave();
			DebugUITools.launch(configuration, mode);
		} catch (CoreException e) {
			return;
		}
	}
	
	private static boolean isMainSpecification(IFile file) {
		IEditorPart editor = Utilities.getEditor(file);
		if (editor instanceof ASMEditor) {
			ASMParser parser = ((ASMEditor)editor).getParser();
			if (parser.getRootNode() != null && "CoreASM".equals(parser.getRootNode().getGrammarRule()))
				return true;
		}
		return false;
	}
}
