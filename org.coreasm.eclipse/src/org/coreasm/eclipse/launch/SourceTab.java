package org.coreasm.eclipse.launch;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.preferences.PreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;


public class SourceTab extends AbstractLaunchConfigurationTab implements
		ILaunchConfigurationTab {

	private SourceTabComposite2 comp; 
	private Image image=null;
	
	public SourceTab() {
		super();
	}

	public void createControl(Composite parent) {
		comp = new SourceTabComposite2(parent, SWT.NONE);
		
		comp.getBrowseProjectButton().addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				browseProject();
			}
		});

		comp.getBrowseSpecButton().addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				browseSpec();
			}
		});
		
		ModifyListener tl=new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		};
		SelectionListener cl=new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		};
		
		comp.getProject().addModifyListener(tl);
		comp.getSpec().addModifyListener(tl);
		comp.getStopOnEmptyUpdates().addSelectionListener(cl);
		comp.getStopOnErrors().addSelectionListener(cl);
		comp.getStopOnFailedUpdates().addSelectionListener(cl);
		comp.getStopOnMaxSteps().addSelectionListener(cl);
        comp.getMaxSteps().addModifyListener(tl);
        comp.getStopOnStableUpdates().addSelectionListener(cl);
        comp.getStopOnEmptyActiveAgents().addSelectionListener(cl);
        
		comp.getDumpFinal().addSelectionListener(cl);
		comp.getDumpState().addSelectionListener(cl);
		comp.getDumpUpdates().addSelectionListener(cl);
		comp.getMarkSteps().addSelectionListener(cl);
		comp.getPrintAgents().addSelectionListener(cl);
		
		String [] levels={"No log", "Fatal", "Error", "Warning","Information"};
		comp.getLogLevel().setItems(levels);
		comp.getLogLevel().addSelectionListener(cl);
		
		setControl(comp);	
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		LaunchCommon.setDefaults(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		if (image!=null)
			return image;
		else {
            String root = CoreASMPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.ROOT_FOLDER);
//			ImageDescriptor id = CoreASMPlugin.getImageDescriptor(root + CoreASMPlugin.MAIN_ICON_PATH);
//			return id.createImage();
			return new Image(Display.getCurrent(), root+CoreASMPlugin.MAIN_ICON_PATH);
		}
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			comp.getProject().setText(configuration.getAttribute(ICoreASMConfigConstants.PROJECT,""));
			comp.getSpec().setText(configuration.getAttribute(ICoreASMConfigConstants.SPEC,""));
			comp.getStopOnEmptyUpdates().setSelection(configuration.getAttribute(ICoreASMConfigConstants.STOPON_EMPTYUPDATES,true));
			comp.getStopOnErrors().setSelection(configuration.getAttribute(ICoreASMConfigConstants.STOPON_ERRORS,true));
			comp.getStopOnErrors().setSelection(configuration.getAttribute(ICoreASMConfigConstants.STOPON_ERRORS,true));
			comp.getStopOnFailedUpdates().setSelection(configuration.getAttribute(ICoreASMConfigConstants.STOPON_FAILEDUPDATES,true));
			comp.getStopOnMaxSteps().setSelection(configuration.getAttribute(ICoreASMConfigConstants.STOPON_STEPSLIMIT,true));
			comp.getStopOnStableUpdates().setSelection(configuration.getAttribute(ICoreASMConfigConstants.STOPON_STABLEUPDATES,true));
			comp.getStopOnEmptyActiveAgents().setSelection(configuration.getAttribute(ICoreASMConfigConstants.STOPON_EMPTYACTIVEAGENTS, true));
			comp.getMaxSteps().setSelection(configuration.getAttribute(ICoreASMConfigConstants.MAXSTEPS,200));
			comp.getLogLevel().setText(comp.getLogLevel().getItem(configuration.getAttribute(ICoreASMConfigConstants.VERBOSITY,1)));
			comp.getDumpUpdates().setSelection(configuration.getAttribute(ICoreASMConfigConstants.DUMPUPDATES,false));
			comp.getDumpState().setSelection(configuration.getAttribute(ICoreASMConfigConstants.DUMPSTATE,false));
			comp.getDumpFinal().setSelection(configuration.getAttribute(ICoreASMConfigConstants.DUMPFINAL,false));
			comp.getMarkSteps().setSelection(configuration.getAttribute(ICoreASMConfigConstants.MARKSTEPS,false));
			comp.getPrintAgents().setSelection(configuration.getAttribute(ICoreASMConfigConstants.PRINTAGENTS,false));
		} catch (CoreException e) {
			// TODO handle exception
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ICoreASMConfigConstants.PROJECT,comp.getProject().getText());
		configuration.setAttribute(ICoreASMConfigConstants.SPEC,comp.getSpec().getText());
		configuration.setAttribute(ICoreASMConfigConstants.STOPON_EMPTYUPDATES,comp.getStopOnEmptyUpdates().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.STOPON_ERRORS,comp.getStopOnErrors().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.STOPON_FAILEDUPDATES,comp.getStopOnFailedUpdates().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.STOPON_STABLEUPDATES,comp.getStopOnStableUpdates().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.STOPON_EMPTYACTIVEAGENTS, comp.getStopOnEmptyActiveAgents().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.STOPON_STEPSLIMIT,comp.getStopOnMaxSteps().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.MAXSTEPS,comp.getMaxSteps().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.VERBOSITY,comp.getLogLevel().getSelectionIndex());
		configuration.setAttribute(ICoreASMConfigConstants.DUMPSTATE,comp.getDumpState().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.DUMPUPDATES,comp.getDumpUpdates().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.DUMPFINAL,comp.getDumpFinal().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.MARKSTEPS,comp.getMarkSteps().getSelection());
		configuration.setAttribute(ICoreASMConfigConstants.PRINTAGENTS, comp.getPrintAgents().getSelection());
	}

	public String getName() {
		return "Specification";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return super.canSave();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		if (comp.getProject().getText().isEmpty()) {
			setErrorMessage("You must choose a project.");
			return false;
		}
		if (comp.getSpec().getText().isEmpty()) {
			setErrorMessage("You must choose a specification (." 
					+ CoreASMPlugin.COREASM_FILE_EXT_1 + " or ."
					+ CoreASMPlugin.COREASM_FILE_EXT_2 + ") file.");
			return false;
		}
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(comp.getProject().getText());
		if (!(resource instanceof IProject)) {
			setErrorMessage("'" + comp.getProject().getText() + "' is no project");
			return false;
		}
		IProject project = (IProject)resource;
		resource = project.findMember(comp.getSpec().getText());
		if (resource == null) {
			setErrorMessage("Cannot find specification '" + comp.getSpec().getText() + "' inside project '" + project.getName() + "'");
			return false;
		}
		return super.isValid(launchConfig);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		if (image!=null)
			image.dispose();
		super.finalize();
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */
	
	private void browseProject() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember((Path)result[0]);
			IProject project = resource.getProject();
			comp.getProject().setText(project.getFullPath().toString());
		}
	}

	/**
	 * Uses the standard resource selection dialog to choose the new value for
	 * the spec field.
	 */
	
	private void browseSpec() {
		IWorkspaceRoot root=ResourcesPlugin.getWorkspace().getRoot();
		IResource proj=root.findMember(comp.getProject().getText());
		ResourceSelectionDialog dialog = new ResourceSelectionDialog(
				getShell(), 
				proj!=null?proj:root,
				"Select a CoreASM specification");
		if (dialog.open() == ResourceSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				comp.getSpec().setText(((IFile) result[0]).getProjectRelativePath().toString());
			}
		}
	}
	
}
