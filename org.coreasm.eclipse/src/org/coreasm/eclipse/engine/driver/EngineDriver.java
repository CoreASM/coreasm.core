/*
 * EngineDriver.java 	$Revision: 108 $
 *
 * Copyright (C) 2005 Vincenzo Gervasi
 * 
 * Later modified and improved by
 *	 Roozbeh Farahbod
 *	 Daniel Sadilek
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2009-12-15 14:06:24 -0500 (Tue, 15 Dec 2009) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.eclipse.engine.driver;


import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.engine.CoreASMEngineFactory;
import org.coreasm.eclipse.launch.ICoreASMConfigConstants;
import org.coreasm.eclipse.preferences.PreferenceConstants;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.CoreASMWarning;
import org.coreasm.engine.EngineErrorEvent;
import org.coreasm.engine.EngineErrorObserver;
import org.coreasm.engine.EngineEvent;
import org.coreasm.engine.EngineModeEvent;
import org.coreasm.engine.EngineModeObserver;
import org.coreasm.engine.EngineStepObserver;
import org.coreasm.engine.EngineWarningEvent;
import org.coreasm.engine.EngineWarningObserver;
import org.coreasm.engine.Specification;
import org.coreasm.engine.StepFailedEvent;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugins.debuginfo.DebugInfoPlugin.DebugInfoPSI;
import org.coreasm.engine.plugins.io.IOPlugin.IOPluginPSI;
import org.coreasm.engine.plugins.io.InputProvider;
import org.coreasm.util.CoreASMGlobal;
import org.coreasm.util.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;


public class EngineDriver implements Runnable, EngineModeObserver, EngineStepObserver, EngineErrorObserver, EngineWarningObserver {

	private static EngineDriver syntaxInstance=null;
	protected static EngineDriver runningInstance=null;
	private static Set<EngineDriverAction> actions = new HashSet<EngineDriverAction>();
	
	protected CoreASMEngine engine;
	private final boolean isSyntaxEngine;
	//private CoreASMEngine syntaxEngine;
	
	public enum EngineDriverStatus {stopped, running, paused};
	private EngineDriverStatus status = EngineDriverStatus.stopped;
	
	private String abspathname;
	private boolean updateFailed;
	private String stepFailedMsg;
	protected CoreASMError lastError;
	private int stepsLimit;
	private boolean stopOnEmptyUpdates;
	private boolean stopOnStableUpdates;
	private boolean stopOnEmptyActiveAgents;
	private boolean stopOnFailedUpdates;
	private boolean stopOnError;
	private boolean stopOnStepsLimit;
	private boolean dumpUpdates;
	private boolean dumpState;
	private boolean dumpFinal;
	private boolean markSteps;
	private boolean printAgents;
	private IOConsole console;
	private IOConsoleOutputStream consoleStdout;
	private IOConsoleOutputStream consoleStderr;
	private IOConsoleOutputStream consoleStddump;
	private PrintStream stderr;
	private PrintStream stddump;
	private PrintStream systemErr;
	
	private volatile boolean shouldStop = false;
	private volatile boolean shouldPause = false;
	
	public static synchronized EngineDriver getSyntaxInstance() {
		if (syntaxInstance == null || PreferenceConstants.isPrefChanged())
			syntaxInstance = new EngineDriver(true);
		return syntaxInstance;
	}
	
	public static EngineDriver getRunningInstance() {
		return runningInstance;
	}
	
	protected EngineDriver(boolean isSyntaxEngine) {
		super();
		CoreASMGlobal.setRootFolder(CoreASMPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.ROOT_FOLDER));
		engine = CoreASMEngineFactory.createCoreASMEngine();
		shouldStop = false;
		this.isSyntaxEngine = isSyntaxEngine;
		/*
		syntaxEngine=CoreASMEngineFactory.createEngine();
		syntaxEngine.setClassLoader(this.getClass().getClassLoader());
		syntaxEngine.initialize();
		syntaxEngine.waitWhileBusy();
		*/
	}

	public static synchronized void addAction(EngineDriverAction action) {
		actions.add(action);
	}
	
	public synchronized void updateStatus(EngineDriverStatus status) {
		this.status = status;
		for (EngineDriverAction action: actions)
			action.update(status);
	}
	
	public EngineDriverStatus getStatus() {
		return status;
	}
	
	public void setDefaultConfig()
	{
		Logger.verbosityLevel=Logger.ERROR;
		stopOnEmptyUpdates=false;
		stopOnStableUpdates=false;
		stopOnEmptyActiveAgents=true;
		stopOnFailedUpdates=true;
		stopOnError=true;
		stopOnStepsLimit=false; 	// TODO this should probably be false
		stepsLimit=20;			
		dumpUpdates=false;
		dumpState=false;
		dumpFinal=false;
		markSteps=false;
		printAgents=false;
	}
	
	public void setConfig(ILaunchConfiguration config) {
		try {
			Logger.verbosityLevel=config.getAttribute(ICoreASMConfigConstants.VERBOSITY,Logger.ERROR);
			stopOnEmptyUpdates=config.getAttribute(ICoreASMConfigConstants.STOPON_EMPTYUPDATES,false);
			stopOnStableUpdates=config.getAttribute(ICoreASMConfigConstants.STOPON_STABLEUPDATES,false);
			stopOnEmptyActiveAgents = config.getAttribute(ICoreASMConfigConstants.STOPON_EMPTYACTIVEAGENTS, true);
			stopOnFailedUpdates=config.getAttribute(ICoreASMConfigConstants.STOPON_FAILEDUPDATES,true);
			stopOnError=config.getAttribute(ICoreASMConfigConstants.STOPON_ERRORS,true);
			stopOnStepsLimit=config.getAttribute(ICoreASMConfigConstants.STOPON_STEPSLIMIT,true);
			stepsLimit=config.getAttribute(ICoreASMConfigConstants.MAXSTEPS,20);
			dumpUpdates=config.getAttribute(ICoreASMConfigConstants.DUMPUPDATES,false);
			dumpState=config.getAttribute(ICoreASMConfigConstants.DUMPSTATE,false);
			dumpFinal=config.getAttribute(ICoreASMConfigConstants.DUMPFINAL,true);
			markSteps=config.getAttribute(ICoreASMConfigConstants.MARKSTEPS,false);
			printAgents=config.getAttribute(ICoreASMConfigConstants.PRINTAGENTS, false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public void finalize() {
		engine.terminate();
		//syntaxEngine.terminate();
	}
	
	public static void newLaunch(String abspathname) throws CoreException {
		newLaunch(abspathname, null);
	}
	
	public static void newLaunch(String abspathname, ILaunchConfiguration config) throws CoreException {
		if (runningInstance == null) {
			runningInstance = new EngineDriver(false);
			if (config == null)
				runningInstance.setDefaultConfig();
			else
				runningInstance.setConfig(config);
			runningInstance.dolaunch(abspathname);
		} else
			throw new CoreException(new Status(Status.WARNING, CoreASMPlugin.PLUGIN_ID, -1, "Another specification is currently running.", null));
	}

	/*
	public synchronized void launch(String abspathname) {
		setDefaultConfig();
		dolaunch(abspathname);
	}
	
	public synchronized void launch(String abspathname, ILaunchConfiguration config) {
		setConfig(config);
		dolaunch(abspathname);
	}
	*/
	
	public void dolaunch(String abspathname) {
		this.abspathname=abspathname;
		Thread t=new Thread(this);
        try {
            t.setName("CoreASM run of "+abspathname.substring(abspathname.lastIndexOf(File.separator)));
        }
        catch (Throwable e) {
            t.setName("CoreASM run of "+abspathname);
        }
		t.start();
		// TODO should wait until after loadSpecification (due to global abspathname);
	}
	
	public synchronized void stop() {
		shouldStop = true;
	}
	
	public synchronized void pause() {
		shouldPause = true;
	}
	
	public synchronized void resume() {
		shouldPause = false;
	}

	protected void preExecutionCallback() {
		// Empty implementation. Can be overridden by subclasses.
	}

	protected void postExecutionCallback() {
		// Empty implementation. Can be overridden by subclasses.
	}
	
	public void run()
	{
		if (this == runningInstance) 
			updateStatus(EngineDriverStatus.running);
		
		int step=0;
		Exception exception = null;
		
		engine.addObserver(this); // TODO this too prevents more than a single syntaxInstance being run at the same time...
		Set<Update> updates,prevupdates=null;

		try {

			setInputOutputPhase1();

			if (engine.getEngineMode() == EngineMode.emError) {
				engine.recover();
				engine.waitWhileBusy();
			}
			engine.loadSpecification(abspathname);
			engine.waitWhileBusy();
			if (engine.getEngineMode()!=EngineMode.emIdle) {
				handleError();
				return;
			}
			
			if (shouldStop)
				throw new EngineDriverException();
			
			setInputOutputPhase2();
			
			preExecutionCallback();
			
			clearEclipseRuntimeErrors();

			while (engine.getEngineMode()==EngineMode.emIdle) {
				if (shouldPause) {
					if (this == runningInstance)
						updateStatus(EngineDriverStatus.paused);
					
					stderr.println("[!] Run is paused by user. Click on resume to continue...");

					while (shouldPause && !shouldStop)
						Thread.sleep(100);
					
					if (!shouldStop)
						stderr.println("[!] Resuming.");

					if (this == runningInstance && !shouldStop)
						updateStatus(EngineDriverStatus.running);
				}
				
				if (shouldStop) {
					throw new EngineDriverException();
				}
				
				engine.step(); step++;

				while (!shouldStop && engine.isBusy())
					Thread.sleep(50);
				
				if (shouldStop) {
					// give some time to the engine to finish
					if (engine.isBusy())
						Thread.sleep(200);

					throw new EngineDriverException();
				}
				
				updates = engine.getUpdateSet(0);
				if (markSteps)
					stddump.println("#--- end of step " + step);
				if (dumpUpdates)
					stddump.println("Updates at step "+step+": "+updates);
				if (dumpState)
					stddump.println("State at step "+step+":\n"+engine.getState());
				if (printAgents)
					stddump.println("Last selected agents: " + engine.getLastSelectedAgents());
				if (terminated(step,updates,prevupdates))
					break;
				prevupdates=updates;
				
			}
			if (engine.getEngineMode()!=EngineMode.emIdle) 
				handleError();
		} catch (Exception e) {
			exception = e;
		} finally {
			engine.removeObserver(this);
			if (exception != null) 
				if (exception instanceof EngineDriverException)
					stderr.println("[!] Run is terminated by user.");
				else {
					stderr.println("[!] Run is terminated with exception " + exception);
				}

			if (dumpFinal && step > 0) {
				stddump.println("--------------------FINISHED---------------------");
				stddump.println("Final engine mode was "+engine.getEngineMode());
				if (lastError != null)
					stddump.println("Last error was "+lastError);
				if (stepFailedMsg != null)
					stddump.println("Step failed reason was "+stepFailedMsg);
				stddump.println("Final state was:\n"+engine.getState());
				//stddump.println("Output history:\n"+getOutputString());

				// Repeating 
				if (exception != null) 
					if (exception instanceof EngineDriverException)
						stderr.println("[!] Run is terminated by user.");
					else
						stderr.println("[!] Run is terminated with exception " + exception);
			}
			System.setErr(systemErr);
			engine.terminate();
			
			if (this == runningInstance)
				updateStatus(EngineDriverStatus.stopped);
			
			runningInstance.engine.hardInterrupt();
			
			runningInstance = null;
			
			postExecutionCallback();
		}
	}

	private boolean terminated(int step, Set<Update> updates, Set<Update> prevupdates) {
		if (stopOnEmptyUpdates && updates.isEmpty())
			return true;
		if (stopOnStableUpdates && updates.equals(prevupdates))
			return true;
		if (stopOnEmptyActiveAgents && engine.getAgentSet().size() < 1)
			return true;
		if (stopOnFailedUpdates && updateFailed)
			return true;
		if (stopOnError && lastError!=null)
			return true;
		if (stopOnStepsLimit && step>stepsLimit)
			return true;
		return false;
	}

	private void setInputOutputPhase1() {

		console=new IOConsole("CoreASM Specification",null,CoreASMPlugin.getImageDescriptor(CoreASMPlugin.MAIN_ICON_PATH),true);	
		consoleStdout=console.newOutputStream();
		consoleStderr=console.newOutputStream();
		consoleStddump=console.newOutputStream();
		//stdout=new PrintStream(consoleStdout);
		stderr=new PrintStream(consoleStderr);
		stddump=new PrintStream(consoleStddump);
		consoleStdout.setColor(null);
		consoleStderr.setColor(new Color(Display.getCurrent(), IStreamsColorConstants.ERROR));
		consoleStddump.setColor(new Color(Display.getCurrent(), IStreamsColorConstants.DUMP));

		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);

		systemErr = System.err;
		System.setErr(stderr);
	}
	
	private void setInputOutputPhase2() {

		console=new IOConsole("CoreASM "+curspecname(),null,CoreASMPlugin.getImageDescriptor(CoreASMPlugin.MAIN_ICON_PATH),true);	
		consoleStdout=console.newOutputStream();
		consoleStderr=console.newOutputStream();
		consoleStddump=console.newOutputStream();
		//stdout=new PrintStream(consoleStdout);
		stderr=new PrintStream(consoleStderr);
		stddump=new PrintStream(consoleStddump);
		consoleStdout.setColor(null);
		consoleStderr.setColor(new Color(Display.getCurrent(), IStreamsColorConstants.ERROR));
		consoleStddump.setColor(new Color(Display.getCurrent(), IStreamsColorConstants.DUMP));

		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);

		// Setting input/output channels of the IO Plugin
		PluginServiceInterface pi = engine.getPluginInterface("IOPlugin");
		if (pi != null) {
			((IOPluginPSI)pi).setInputProvider(new InputProvider() {
				public String getValue(String message) {
					String input = JOptionPane.showInputDialog(null, message, "");
					return input;
				}
			});
			
			((IOPluginPSI)pi).setOutputStream(new PrintStream(consoleStdout));
		}

		// Setting input/output channels of the IO Plugin
		pi = engine.getPluginInterface("DebugInfoPlugin");
		if (pi != null) {
			((DebugInfoPSI)pi).setOutputStream(new PrintStream(consoleStdout));
		}

	}

	private String curspecname() {
		if (engine.getSpec() != null)
			return engine.getSpec().getName();
		else
			return "";
	}
	
	/*
	private String[] getOutput() {
		PluginServiceInterface pi = engine.getPluginInterface("IOPlugin");
		if (pi != null) {
			String[] el=((IOPluginPSI)pi).getOutputHistory();
			return el;
		}
		return null;
	}
	
	
	private String getOutputString() {
		String[] el=getOutput();
		if (el!=null) {
			StringBuffer sb=new StringBuffer();
			for (int i = 0; i < el.length; i++) {
				sb.append(el[i]);
				if (i<el.length-1)
					sb.append('\n');
			}
			return sb.toString();
		} else
			return "";
	}
	*/
	
	public synchronized Specification getSpec(String text, boolean loadPlugins) {
		if (!isSyntaxEngine)
			return null;
		
		engine.waitWhileBusy();
		if (engine.getEngineMode() == EngineMode.emError) {
			engine.recover();
			return null;
		}
		engine.parseSpecificationHeader(new StringReader(text), loadPlugins);
		engine.waitWhileBusy();
		if (engine.getEngineMode() == EngineMode.emError) {
			engine.recover();
			return null;
		} else
			return engine.getSpec();
	}

//	public synchronized Set<String> getUsedPlugins(String text) {
		/*
		if (engine.getEngineMode() != EngineMode.emIdle)
			return null;
		engine.loadSpecification(new StringReader(text));
		engine.waitIfBusy();
		if (engine.getEngineMode() == EngineMode.emError) {
			engine.recover();
			return null;
		} else
			return engine.getSpec().getPluginNames();
		*/
//		HashSet<String> usedPlugins=new HashSet<String>();
//		RegularExpression re=new RegularExpression("^[ \t]*use[ \t]+[a-zA-Z0-9_]+[ \t]*$");		
//		text.replaceAll("(?s)/\\*.*?\\*/",""); // remove block comments
//		text.replaceAll("//.*?$",""); // remove end-of-line comments 
//		text.replaceAll("^#.*$",""); // remove # comments
//		String lines[]=text.split("\n");
//		usedPlugins.add("Kernel"); 
//		for (int i = 0; i < lines.length; i++) {
//			if (re.matches(lines[i])) {
//				String words[]=lines[i].split("[ \t]+");
//				for (int j = 0; j < words.length; j++) {
//					if (words[j].equals("use")) {
//						usedPlugins.add((words[j+1]));
//						break;
//					}
//				}
//			}
//		}
//		return usedPlugins;
//	}
	
//	public synchronized Set<String> getKeywords(Set<String> usedPlugins) {
//		return engine.getPluginsKeywords(usedPlugins);
//		Engine engine=(Engine)this.engine;
//		HashSet<String> keywords=new HashSet<String>();
//		for (String pn : usedPlugins) {
//			Plugin po=engine.getPlugin(pn);
//			if (po!=null)
//				try {
//					Set<String> kws=po.getKeywords();
//					if (kws!=null)
//						keywords.addAll(kws);
//				} catch (Exception e) {
//					System.err.println("While retrieving keywords for "+pn+": "+e);
//				}
//		}
//		return keywords;
//	}
	
//	public synchronized Set<String> getKeywords(String spec) {
//		return engine.getSpecKeywords(spec);
//	}

	public void update(EngineEvent event) {
		
		if (event instanceof EngineModeEvent) {
			if (((EngineModeEvent)event).getNewMode() == EngineMode.emStepFailed) {
				ControlAPI capi = (ControlAPI) engine;
				for (Update update : capi.getStorage().getLastInconsistentUpdate()) {
					for (ScannerInfo scannerInfo : update.sources) {
						CoreASMError error = new CoreASMError("Inconsistent Update: " + update, scannerInfo.getPos(capi
								.getParser().getPositionMap()));
						error.setContext(capi.getParser(), capi.getSpec());
						showErrorInEclipse(error);
					}
				}
			}
		}
		// Looking for StepFailed
		if (event instanceof StepFailedEvent) {
			StepFailedEvent sEvent = (StepFailedEvent)event;
			synchronized (this) {
				updateFailed = true;
				stepFailedMsg = sEvent.reason;
				
			}
		}
		
		// Looking for errors
		else if (event instanceof EngineErrorEvent) {
			synchronized (this) {
				lastError = ((EngineErrorEvent)event).getError();
			}
			showErrorInEclipse(lastError);
		}
		else if (event instanceof EngineWarningEvent)
			showWarningInEclipse(((EngineWarningEvent)event).getWarning());
	}
	
	private void showErrorInEclipse(CoreASMError error) {
		ASMEditor.createRuntimeErrorMark(error, (ControlAPI)engine);
	}
	
	private void showWarningInEclipse(CoreASMWarning warning) {
		ASMEditor.createRuntimeWarningMark(warning, (ControlAPI)engine);
	}
	
	private void clearEclipseRuntimeErrors() {
		ASMEditor.removeRuntimeProblemMarkers((ControlAPI)engine);
	}
	
	/**
	 * @return Returns the maxsteps.
	 */
	public int getMaxsteps() {
		return stepsLimit;
	}

	/**
	 * @param maxsteps The maxsteps to set.
	 */
	public void setMaxsteps(int maxsteps) {
		this.stepsLimit = maxsteps;
	}

	/**
	 * @return Returns the stopOnEmptyUpdates.
	 */
	public boolean isStopOnEmptyUpdates() {
		return stopOnEmptyUpdates;
	}

	/**
	 * @param stopOnEmptyUpdates The stopOnEmptyUpdates to set.
	 */
	public void setStopOnEmptyUpdates(boolean stopOnEmptyUpdates) {
		this.stopOnEmptyUpdates = stopOnEmptyUpdates;
	}

	/**
	 * @return Returns the stopOnError.
	 */
	public boolean isStopOnError() {
		return stopOnError;
	}

	/**
	 * @param stopOnError The stopOnError to set.
	 */
	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	/**
	 * @return Returns the stopOnFailedUpdates.
	 */
	public boolean isStopOnFailedUpdates() {
		return stopOnFailedUpdates;
	}

	/**
	 * @param stopOnFailedUpdates The stopOnFailedUpdates to set.
	 */
	public void setStopOnFailedUpdates(boolean stopOnFailedUpdates) {
		this.stopOnFailedUpdates = stopOnFailedUpdates;
	}

	public boolean isStopOnEmptyActiveAgents() {
		return stopOnEmptyActiveAgents;
	}
	
	public void setStopOnEmptyActiveAgents(boolean b) {
		stopOnEmptyActiveAgents = b;
	}

	/**
	 * @return Returns the stopOnStableUpdates.
	 */
	public boolean isStopOnStableUpdates() {
		return stopOnStableUpdates;
	}

	/**
	 * @param stopOnStableUpdates The stopOnStableUpdates to set.
	 */
	public void setStopOnStableUpdates(boolean stopOnStableUpdates) {
		this.stopOnStableUpdates = stopOnStableUpdates;
	}

	/**
	 * @return Returns the stopOnStepsLimit.
	 */
	public boolean isStopOnStepsLimit() {
		return stopOnStepsLimit;
	}

	/**
	 * @param stopOnStepsLimit The stopOnStepsLimit to set.
	 */
	public void setStopOnStepsLimit(boolean stopOnStepsLimit) {
		this.stopOnStepsLimit = stopOnStepsLimit;
	}

	/**
	 * @return Returns the stepsLimit.
	 */
	public int getStepsLimit() {
		return stepsLimit;
	}

	/**
	 * @param stepsLimit The stepsLimit to set.
	 */
	public void setStepsLimit(int stepsLimit) {
		this.stepsLimit = stepsLimit;
	}

	/**
	 * @return Returns the lastError.
	 */
	public CoreASMError getLastError() {
		return lastError;
	}

	/**
	 * @return Returns the stepFailedMsg.
	 */
	public String getStepFailedMsg() {
		return stepFailedMsg;
	}

	/**
	 * @return Returns the updateFailed.
	 */
	public boolean isUpdateFailed() {
		return updateFailed;
	}

	protected void handleError() {
		String message = "";
		if (lastError != null)
			message = lastError.showError();
		else
			message = "Enginemode should be " + EngineMode.emIdle + " but is " + engine.getEngineMode();
        
//		JOptionPane.showMessageDialog(null, message, "CoreASM Engine Error", JOptionPane.ERROR_MESSAGE);
        showErrorDialog("CoreASM Engine Error",message);

        lastError = null;
		stepFailedMsg = null;
		engine.recover();     
		engine.waitWhileBusy();
	}
    
    private void showErrorDialog(String title, String message) {
    	//MessageDialog.openError(shell, title, message);
    	stderr.println("\n" + message);
    }

    /*
    private void showErrorDialog(String title, String message) {
        Display d = new Display();
        Shell s = new Shell(d);
        MessageBox errorBox = new MessageBox(s,SWT.ICON_ERROR|SWT.OK);
        errorBox.setText(title);
        errorBox.setMessage(message);
        errorBox.open();
        
        s.dispose();
        while(!s.isDisposed( )){
            if(!d.readAndDispatch( ))
                d.sleep( );
        }
        d.dispose( );
    }
    */
    
    /**
     * An internal exception class.
     */
    private class EngineDriverException extends Exception {
		private static final long serialVersionUID = 1L;

		public EngineDriverException() {
    		
    	}
    }

	public boolean isDumpFinal() {
		return dumpFinal;
	}

	public void setDumpFinal(boolean dumpFinal) {
		this.dumpFinal = dumpFinal;
	}
    
}
