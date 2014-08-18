package org.coreasm.engine.test;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.Engine;
import org.coreasm.engine.EngineErrorEvent;
import org.coreasm.engine.EngineErrorObserver;
import org.coreasm.engine.EngineEvent;
import org.coreasm.engine.EngineProperties;
import org.coreasm.engine.EngineStepObserver;
import org.coreasm.engine.StepFailedEvent;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugins.debuginfo.DebugInfoPlugin.DebugInfoPSI;
import org.coreasm.engine.plugins.io.IOPlugin.IOPluginPSI;
import org.coreasm.util.CoreASMGlobal;
import org.coreasm.util.Logger;
import org.coreasm.util.Tools;

public class TestEngineDriver implements Runnable, EngineStepObserver, EngineErrorObserver {

	protected static List<TestEngineDriver> runningInstances = null;

	protected Engine engine;

	public enum TestEngineDriverStatus {
		stopped, running, paused
	};

	private TestEngineDriverStatus status = TestEngineDriverStatus.stopped;

	private boolean updateFailed;
	protected CoreASMError lastError;
	private int stepsLimit;
	private boolean stopOnEmptyUpdates;
	private boolean stopOnStableUpdates;
	private boolean stopOnEmptyActiveAgents;
	private boolean stopOnFailedUpdates;
	private boolean stopOnStepsLimit;
	private boolean shouldStop;
	private boolean shouldPause;

	private TestEngineDriver() {
		if (runningInstances == null)
			runningInstances = new LinkedList<TestEngineDriver>();
		runningInstances.add(this);
		CoreASMGlobal.setRootFolder(Tools.getRootFolder());
		engine = (Engine) org.coreasm.engine.CoreASMEngineFactory.createEngine();
		engine.addObserver(this);
		shouldStop = false;
		shouldPause = true;
		status = TestEngineDriverStatus.paused;

		String pluginFolders = Tools.getRootFolder(Engine.class)+"/plugins";
		if (System.getProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY) != null)
			pluginFolders += EngineProperties.PLUGIN_FOLDERS_DELIM
					+ System.getProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY);
		engine.setProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY, pluginFolders);
		engine.setClassLoader(CoreASMEngineFactory.class.getClassLoader());
		engine.initialize();
		engine.waitWhileBusy();
	}

	public TestEngineDriverStatus getStatus() {
		return status;
	}

	public void setOutputStream(PrintStream outputStream) {
		PluginServiceInterface pi = engine.getPluginInterface("IOPlugin");
		if (pi != null)
			((IOPluginPSI)pi).setOutputStream(outputStream);
		pi = engine.getPluginInterface("DebugInfoPlugin");
		if (pi != null)
			((DebugInfoPSI)pi).setOutputStream(outputStream);
	}

	public void setDefaultConfig()
	{
		Logger.verbosityLevel = Logger.ERROR;
		stopOnEmptyUpdates = false;
		stopOnStableUpdates = false;
		stopOnEmptyActiveAgents = true;
		stopOnFailedUpdates = false;
		stopOnStepsLimit = false; 	// TODO this should probably be false
		stepsLimit = -1; //means infinite steps
	}

	public Engine getEngine() {
		return engine;
	}

	public boolean isRunning() {
		return runningInstances.contains(this);
	}

	public static TestEngineDriver newLaunch(String abspathname) {
		TestEngineDriver td = new TestEngineDriver();
		td.setDefaultConfig();
		td.dolaunch(abspathname);
		return td;
	}

	public void dolaunch(String abspathname) {
		Thread t = new Thread(this);

		try {
			t.setName("CoreASM run of " +
					abspathname.substring(abspathname.lastIndexOf(File.separator)));
		}
		catch (Throwable e) {
			t.setName("CoreASM run of " + abspathname);
		}

		if (engine.getEngineMode() == EngineMode.emError) {
			engine.recover();
			engine.waitWhileBusy();
		}

		engine.loadSpecification(abspathname);
		engine.waitWhileBusy();

		t.start();
	}

	@Override
	public void run()
	{
		int step = 0;
		Exception exception = null;

		Set<Update> updates, prevupdates = null;

		try {

			if (engine.getEngineMode() != EngineMode.emIdle) {
				handleError();
				return;
			}

			while (engine.getEngineMode() == EngineMode.emIdle) {

				//set current mode
				if (shouldStop && stepsLimit <= 0) {
					engine.waitWhileBusy();
					break;
				}
				else if (shouldPause || stepsLimit == 0) {
					status = TestEngineDriverStatus.paused;
					Thread.sleep(100);
				}
				else
				{
					status = TestEngineDriverStatus.running;

					//execute a step
					engine.waitWhileBusy();
					engine.step();
					step++;
					engine.waitWhileBusy();

					updates = engine.getUpdateSet(0);
					if (terminated(step, updates, prevupdates))
						break;
					prevupdates = updates;
					stepsLimit--;
				}

			}
			if (engine.getEngineMode() != EngineMode.emIdle)
				handleError();
		}
		catch (Exception e) {
			exception = e;
			e.printStackTrace();
		}
		finally {
			if (runningInstances != null && runningInstances.contains(this)) {
				runningInstances.remove(this);
				this.engine.removeObserver(this);

				if (exception != null)
					System.err.println("[!] Run is terminated with exception " + exception);

				this.engine.terminate();
				this.engine.hardInterrupt();

				status = TestEngineDriverStatus.stopped;
			}
		}
	}

	/**
	 * starts the engine and resets
	 */
	public void start() {
		shouldPause = false;
		shouldStop = false;
		stopOnStepsLimit = false;
		resume();
	}

	public void restart() {
		shouldPause = false;
		shouldStop = false;
		stopOnStepsLimit = false;
		stepsLimit = -1;
		while (getStatus() == TestEngineDriverStatus.running)
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void resume() {
		if (stepsLimit == 0)
			stepsLimit = -1;
		shouldPause = false;
		while (getStatus() == TestEngineDriverStatus.paused)
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void stop() {
		shouldStop = true;
		while (getStatus() != TestEngineDriverStatus.stopped)
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void pause() {
		shouldPause = true;
		while (getStatus() == TestEngineDriverStatus.running)
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void executeSteps(int numberOfSteps) {
		stepsLimit = numberOfSteps;
		stopOnStepsLimit = true;
		resume();
		while (stepsLimit > 0 && getStatus() != TestEngineDriverStatus.stopped)
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
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
		if (stopOnStepsLimit && stepsLimit <= 0 && shouldStop)
			return true;
		return false;
	}

	@Override
	public void update(EngineEvent event) {

		// Looking for StepFailed
		if (event instanceof StepFailedEvent) {
			synchronized (this) {
				updateFailed = true;
			}
		}

		// Looking for errors
		else if (event instanceof EngineErrorEvent) {
			synchronized (this) {
				lastError = ((EngineErrorEvent) event).getError();
			}
		}

	}

	protected void handleError() {
		String message = "";
		if (lastError != null)
			message = lastError.showError();
		else
			message = "Enginemode should be " + EngineMode.emIdle + " but is " + engine.getEngineMode();

		showErrorDialog("CoreASM Engine Error", message);

		lastError = null;
		engine.recover();
		engine.waitWhileBusy();
	}

	private void showErrorDialog(String title, String message) {
		System.err.println(title + "\n" + message);
	}
}
