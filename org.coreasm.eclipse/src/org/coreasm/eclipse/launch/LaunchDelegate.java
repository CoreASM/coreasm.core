package org.coreasm.eclipse.launch;

import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.eclipse.engine.driver.EngineDriver;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;


public class LaunchDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode,
		ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (ILaunchManager.RUN_MODE.equals(mode)) { 
			IResource projbase=ResourcesPlugin.getWorkspace().getRoot().findMember(configuration.getAttribute(ICoreASMConfigConstants.PROJECT,""));
			String fullpath=projbase.getLocation().toString()+IPath.SEPARATOR+configuration.getAttribute(ICoreASMConfigConstants.SPEC,"");
			EngineDriver.newLaunch(fullpath, configuration);

			//Shell shell = CoreASMPlugin.getDefault().getShell();
			//if (EngineDriver.getRunningInstance() != null)
		    	//MessageDialog.openError(null, "Cannot Run", "Another specification is currently running.");
			//else
				//EngineDriver.newLaunch(fullpath, configuration);

		}
		else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			IResource projbase=ResourcesPlugin.getWorkspace().getRoot().findMember(configuration.getAttribute(ICoreASMConfigConstants.PROJECT,""));
			String fullpath=projbase.getLocation().toString()+IPath.SEPARATOR+configuration.getAttribute(ICoreASMConfigConstants.SPEC,"");
			EngineDebugger.newLaunch(fullpath, configuration);
			launch.addDebugTarget(new ASMDebugTarget(launch, EngineDebugger.getRunningInstance()));
		}
	}

}
