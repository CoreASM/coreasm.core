package org.coreasm.eclipse.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class LaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public LaunchConfigurationTabGroup() {
		super();
	}

	// TODO we may need to add both Java and ASM tabs here
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs= new ILaunchConfigurationTab[] {
				new SourceTab(),
				new CommonTab()
		};
		setTabs(tabs);
	}
}
