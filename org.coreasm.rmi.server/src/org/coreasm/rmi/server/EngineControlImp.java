/**
 * 
 */
package org.coreasm.rmi.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.EngineErrorEvent;
import org.coreasm.engine.EngineErrorObserver;
import org.coreasm.engine.EngineEvent;
import org.coreasm.engine.EngineStepObserver;
import org.coreasm.engine.StepFailedEvent;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.rmi.server.remoteinterfaces.EngineControl;
import org.coreasm.rmi.server.remoteinterfaces.EngineDriverInfo;
import org.coreasm.rmi.server.remoteinterfaces.UpdateSubscription;

/**
 * @author Stephan
 *
 */
public class EngineControlImp extends UnicastRemoteObject implements
		Runnable, EngineControl, EngineStepObserver, EngineErrorObserver {
	private static final long serialVersionUID = 1L;
	private CoreASMEngine engine;
	private BufferedReader spec = null;
	private boolean updateFailed;
	protected CoreASMError lastError;

	private boolean stopOnEmptyUpdates;
	private boolean stopOnStableUpdates;
	private boolean stopOnEmptyActiveAgents;
	private boolean stopOnFailedUpdates;
	private boolean stopOnError;
	private boolean stopOnStepsLimit;
	private int stepsLimit;

	private volatile boolean shouldStop = false;
	private volatile boolean shouldPause = true;
	private volatile EngineDriverInfo driverInfo;

	private List<Set<Update>> previousUpdates;
	private List<UpdateSubscription> subscriptions;


	private EngineControlImp() throws RemoteException {
		super();
		engine = CoreASMEngineFactory.createEngine();
		engine.setClassLoader(CoreASMEngineFactory.class.getClassLoader());
		engine.initialize();
		engine.waitWhileBusy();

		stopOnEmptyUpdates = false;
		stopOnStableUpdates = false;
		stopOnEmptyActiveAgents = true;
		stopOnFailedUpdates = true;
		stopOnError = true;
		stopOnStepsLimit = false;
		stepsLimit = 20;

		previousUpdates = new ArrayList<Set<Update>>();
		subscriptions = new ArrayList<UpdateSubscription>();
		driverInfo = new EngineDriverInfo("", EngineDriverStatus.empty);
	}

	public EngineControlImp(String id) throws RemoteException {
		this();
		driverInfo.setId(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coreasm.engine.EngineObserver#update(org.coreasm.engine.EngineEvent)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#getIdNr()
	 */
	@Override
	public String getIdNr() throws RemoteException {
		return driverInfo.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#start()
	 */
	@Override
	public void start() throws RemoteException {
		if (shouldPause) {
			shouldPause = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#pause()
	 */
	@Override
	public void pause() throws RemoteException {
		shouldPause = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#stop()
	 */
	@Override
	public void stop() throws RemoteException {
		shouldStop = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#load(byte[])
	 */
	@Override
	public void load(byte[] specification) throws RemoteException {
		ByteArrayInputStream in = new ByteArrayInputStream(specification);
		spec = new BufferedReader(new InputStreamReader(in));
		driverInfo.setStatus(EngineDriverStatus.paused);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coreasm.rmi.server.remoteinterfaces.EngineControl#subscribe(org.coreasm
	 * .rmi.server.remoteinterfaces.UpdateSubscription)
	 */
	@Override
	public void subscribe(UpdateSubscription sub) throws RemoteException {
		synchronized (subscriptions) {
			subscriptions.add(sub);
		}

	}

	public void run() {

		int step = 0;
		Exception exception = null;

		engine.addObserver(this);
		Set<Update> updates, prevupdates = null;

		try {

			if (engine.getEngineMode() == EngineMode.emError) {
				engine.recover();
				engine.waitWhileBusy();
			}
			while (spec == null) {
				Thread.sleep(500);
			}
			engine.loadSpecification(spec);
			engine.waitWhileBusy();
			if (engine.getEngineMode() != EngineMode.emIdle) {
				handleError();
				return;
			}

			if (shouldStop)
				throw new EngineDriverException();

			while (engine.getEngineMode() == EngineMode.emIdle) {
				if (shouldPause) {
					driverInfo.setStatus(EngineDriverStatus.paused);
					System.err
							.println("[!] Run is paused by user. Click on resume to continue...");

					while (shouldPause && !shouldStop)
						Thread.sleep(100);

					if (!shouldStop)
						System.err.println("[!] Resuming.");

				}

				if (shouldStop) {
					throw new EngineDriverException();
				}
				driverInfo.setStatus(EngineDriverStatus.running);
				
				engine.step();
				step++;

				while (!shouldStop && engine.isBusy())
					Thread.sleep(50);

				if (shouldStop) {
					// give some time to the engine to finish
					if (engine.isBusy())
						Thread.sleep(200);

					throw new EngineDriverException();
				}

				updates = engine.getUpdateSet(0);

				if (terminated(step, updates, prevupdates))
					break;
				prevupdates = updates;
				previousUpdates.add(updates);

				synchronized (subscriptions) {
					Iterator<UpdateSubscription> itr = subscriptions.iterator();

					UpdateSubscription sub;
					while (itr.hasNext()) {
						sub = itr.next();
						try {
							sub.newUpdates(prevupdates.toString());
						} catch (RemoteException e) {
							itr.remove();
						}
					}
					if (subscriptions.isEmpty()) {
						shouldStop = true;
					}
				}

			}
			if (engine.getEngineMode() != EngineMode.emIdle)
				handleError();
		} catch (Exception e) {
			exception = e;
		} finally {
			engine.removeObserver(this);
			if (exception != null)
				if (exception instanceof EngineDriverException)
					System.err.println("[!] Run is terminated by user.");
				else {
					System.err.println("[!] Run is terminated with exception "
							+ exception);
				}

			// Repeating
			if (exception != null)
				if (exception instanceof EngineDriverException)
					System.err.println("[!] Run is terminated by user.");
				else
					System.err.println("[!] Run is terminated with exception "
							+ exception);
		}
		engine.terminate();
		driverInfo.setStatus(EngineDriverStatus.stopped);
	}

	private boolean terminated(int step, Set<Update> updates,
			Set<Update> prevupdates) {
		if (stopOnEmptyUpdates && updates.isEmpty())
			return true;
		if (stopOnStableUpdates && updates.equals(prevupdates))
			return true;
		if (stopOnEmptyActiveAgents && engine.getAgentSet().size() < 1)
			return true;
		if (stopOnFailedUpdates && updateFailed)
			return true;
		if (stopOnError && lastError != null)
			return true;
		if (stopOnStepsLimit && step > stepsLimit)
			return true;
		return false;
	}

	protected void handleError() {
		String message = "";
		if (lastError != null)
			message = lastError.showError();
		else
			message = "Enginemode should be " + EngineMode.emIdle + " but is "
					+ engine.getEngineMode();

		// JOptionPane.showMessageDialog(null, message, "CoreASM Engine Error",
		// JOptionPane.ERROR_MESSAGE);
		System.out.println("CoreASM Engine Error");
		System.out.println(message);

		lastError = null;
		engine.recover();
		engine.waitWhileBusy();
	}

	private class EngineDriverException extends Exception {
		private static final long serialVersionUID = 1L;

		public EngineDriverException() {

		}
	}

	public void finalize() {
		engine.terminate();
	}

	@Override
	public EngineDriverStatus getDriverStatus() throws RemoteException {		
		return driverInfo.getStatus();
	}
	
	@Override
	public EngineDriverInfo getDriverInfo() throws RemoteException {		
		return driverInfo;
	}
}
