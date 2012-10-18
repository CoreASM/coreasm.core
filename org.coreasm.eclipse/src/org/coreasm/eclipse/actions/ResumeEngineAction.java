/*
 * ResumeEngineAction.java 		$Revision: 8 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-01-28 03:32:43 -0500 (Wed, 28 Jan 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.eclipse.actions;

import org.coreasm.eclipse.engine.driver.EngineDriver;
import org.coreasm.eclipse.engine.driver.EngineDriverAction;
import org.coreasm.eclipse.engine.driver.EngineDriver.EngineDriverStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;


/**
 * Action to resume a paused instance of the engine.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class ResumeEngineAction extends ActionDelegate implements 
		IWorkbenchWindowActionDelegate, EngineDriverAction {

	private IWorkbenchWindow window;
	private IAction action;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void init(IAction action) {
		super.init(action);
		this.action = action;
		EngineDriver.addAction(this);
		action.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (EngineDriver.getRunningInstance() != null) {
			EngineDriver.getRunningInstance().resume();
		} else
			MessageDialog.openWarning(
					window.getShell(),
					"CoreASM Plug-in",
					"No engine is running." + action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	public void update(EngineDriverStatus newStatus) {
		if (this.action != null)
			action.setEnabled(newStatus == EngineDriverStatus.paused);
	}
}
