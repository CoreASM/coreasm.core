/*
 * StopEngineAction.java 	1.0 	$Revision: 8 $
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2009-01-28 03:32:43 -0500 (Wed, 28 Jan 2009) $.
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
 * Implements an action to stop an already running CoreASM engine.
 * 
 * @author Roozbeh Farahbod
 * @version 1.0, Last Modified: $Date: 2009-01-28 03:32:43 -0500 (Wed, 28 Jan 2009) $ by $Author: rfarahbod $
 * @see IWorkbenchWindowActionDelegate
 */
public class StopEngineAction extends ActionDelegate 
		implements IWorkbenchWindowActionDelegate, EngineDriverAction {
	
	private IWorkbenchWindow window;
	private IAction action;

	/**
	 * The constructor.
	 */
	public StopEngineAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		if (EngineDriver.getRunningInstance() != null) {
			EngineDriver.getRunningInstance().stop();
		} else
			MessageDialog.openWarning(
					window.getShell(),
					"CoreASM Plug-in",
					"No engine is running.");
	}

	@Override
	public void init(IAction action) {
		super.init(action);
		this.action = action;
		EngineDriver.addAction(this);
		action.setEnabled(false);
	}


	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void update(EngineDriverStatus newStatus) {
		if (this.action != null)
			action.setEnabled(newStatus == EngineDriverStatus.running 
					|| newStatus == EngineDriverStatus.paused);
	}
}