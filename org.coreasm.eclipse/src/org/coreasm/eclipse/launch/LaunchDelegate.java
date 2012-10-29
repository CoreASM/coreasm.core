package org.coreasm.eclipse.launch;

import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.eclipse.engine.driver.EngineDriver;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * A launch delegate for launching the CoreASM engine. 
 * @author Michael Stegmaier
 *
 */
public class LaunchDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String project = configuration.getAttribute(ICoreASMConfigConstants.PROJECT, (String)null);
		String spec = configuration.getAttribute(ICoreASMConfigConstants.SPEC, (String)null);
		String abspathname = null;
		
		if (project != null && spec != null)
			abspathname = ResourcesPlugin.getWorkspace().getRoot().findMember(project + IPath.SEPARATOR + spec).getLocation().toString();
		else
			return;
		
		saveAllEditors();
		
		if (ILaunchManager.RUN_MODE.equals(mode))
			EngineDriver.newLaunch(abspathname, configuration);
		else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			EngineDebugger.newLaunch(abspathname, configuration);
			launch.addDebugTarget(new ASMDebugTarget(launch, EngineDebugger.getRunningInstance()));
		}
	}
	
	private void saveAllEditors() {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true);
			}
		});
	}
}
