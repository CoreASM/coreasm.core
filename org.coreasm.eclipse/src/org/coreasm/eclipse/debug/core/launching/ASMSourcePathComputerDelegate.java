package org.coreasm.eclipse.debug.core.launching;

import org.coreasm.eclipse.launch.ICoreASMConfigConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;

/**
 * Source path computer delegate for ASM source files. It is responsible for finding source files inside the workspace.
 * @author Michael Stegmaier
 *
 */
public class ASMSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	@Override
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		String path = configuration.getAttribute(ICoreASMConfigConstants.PROJECT, (String)null);
		ISourceContainer sourceContainer = null;
		if (path != null) {
			path += IPath.SEPARATOR + configuration.getAttribute(ICoreASMConfigConstants.SPEC, "");
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (resource != null) {
				IContainer container;
				if (resource instanceof IContainer)
					container = (IContainer)resource;
				else
					container = resource.getParent();
				if (container.getType() == IResource.PROJECT)
					sourceContainer = new ProjectSourceContainer((IProject)container, false);
				else if (container.getType() == IResource.FOLDER)
					sourceContainer = new FolderSourceContainer(container, false);
			}
		}
		if (sourceContainer == null)
			sourceContainer = new WorkspaceSourceContainer();
		return new ISourceContainer[] { sourceContainer };
	}
}
