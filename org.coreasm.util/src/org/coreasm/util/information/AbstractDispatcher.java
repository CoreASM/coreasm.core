/**
 *
 */
package org.coreasm.util.information;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.coreasm.util.information.InformationObject.VerbosityLevel;

/**
 * @author Marcel Dausend
 *
 */
abstract class AbstractDispatcher {

	static class Action {
		static final String CREATION = "CREATION";
		static final String CLEAR = "CLEAR";
	}

	public enum DistributionMode {
		COMMIT, AUTOCOMMIT;
	};
	
	private final String id;

	/** thread specific information */
	private LinkedList<DispatcherContext> newActions;
	private DistributionMode distributionMode;

	private static HashMap<String, InformationObserver> observers = new HashMap<String, InformationObserver>();

	/**
	 * creates an Dispatcher with <code>DistributionMode.AUTOCOMMIT</code>
	 */
	public AbstractDispatcher(String id) {
		this(id, DistributionMode.AUTOCOMMIT);
	}

	public AbstractDispatcher(String id, DistributionMode mode) {
		this.id = id;
		newActions = new LinkedList<DispatcherContext>();
		distributionMode = mode;
	}

	/**
	 * add an observer to the list of observers
	 *
	 * @param iStorageAndDispatchObserver
	 *
	 * \todo introduce priorities
	 */
	protected static void addSuperObserver(InformationObserver iStorageAndDispatchObserver) {
		if (! observers.containsKey(iStorageAndDispatchObserver.getClass().getCanonicalName()))
			observers.put(iStorageAndDispatchObserver.getClass().getCanonicalName(), iStorageAndDispatchObserver);
	}

	/**
	 * remove an observer from the list of observers
	 *
	 * @param iStorageAndDispatchObserver
	 */
	protected static void deleteSuperObserver(InformationObserver iStorageAndDispatchObserver) {
		if (observers.containsKey(iStorageAndDispatchObserver.getClass().getCanonicalName()))
			observers.remove(iStorageAndDispatchObserver.getClass().getCanonicalName());
	}

	/**
	 * remove all observers
	 */
	protected static void deleteSuperObervers() {
		observers.clear();
	}
	
	public synchronized void createInformation(String message, ResponseHandler responseHandler) {
		createInformation(new InformationObject(this, message, responseHandler));
	}
	
	public synchronized void createInformation(String message) {
		createInformation(new InformationObject(this, message));
	}
	
	public synchronized void createInformation(String message, Map<String, String> data, ResponseHandler responseHandler) {
		createInformation(new InformationObject(this, message, data, responseHandler));
	}
	
	public synchronized void createInformation(String message, Map<String, String> data) {
		createInformation(new InformationObject(this, message, data));
	}
	
	public synchronized void createInformation(String message, VerbosityLevel verbosity, Map<String, String> data, ResponseHandler responseHandler) {
		createInformation(new InformationObject(this, message, verbosity, data, responseHandler));
	}
	
	public synchronized void createInformation(String message, VerbosityLevel verbosity, Map<String, String> data) {
		createInformation(new InformationObject(this, message, verbosity, data));
	}

	private synchronized void createInformation(InformationObject info) {
		//Instantiate new DispatcherContext with given information object
		DispatcherContext newInfoDispObject = new DispatcherContext(info, Action.CREATION);

		//add new DisplayContext to list of actions
		this.newActions.add(newInfoDispObject);
		if (this.getDistributionMode().equals(DistributionMode.AUTOCOMMIT)) {
			this.notifyObservers();
		} else if (this.getDistributionMode().equals(DistributionMode.COMMIT)) {
			//wait for call of the commit method;
		}
	}

	public synchronized void clearInformation(String message) {
		//Instantiate new DispatcherContext with a null-Object as information object
		DispatcherContext newInfoDispObject = new DispatcherContext(new InformationObject(this, message), Action.CLEAR);

		newActions.add(newInfoDispObject);
		if (this.getDistributionMode().equals(DistributionMode.AUTOCOMMIT)) {
			notifyObservers();
		} else if (this.getDistributionMode().equals(DistributionMode.COMMIT)) {
			//already added to added into new actions
		}
	}

	public synchronized void commit() {
		if (this.getDistributionMode().equals(DistributionMode.COMMIT))
			this.notifyObservers();
	}

	public synchronized void setDistributionMode(DistributionMode distMode) {
		this.distributionMode = distMode;
	}

	public synchronized DistributionMode getDistributionMode() {
		return this.distributionMode;
	}
	
	public String getId() {
		return id;
	}

	/**
	 * call both interface functions of IStorageAndDispatchObserver foreach
	 * InformationDispatch belonging to the current thread. Add or remove each
	 * InformationObject from the information list of StorageAndDispatch. Clear
	 * the newActions entry from the newActions HashMap.
	 */
	private synchronized void notifyObservers() {
		if (!this.newActions.isEmpty()) {
			for (DispatcherContext dispInfo : this.newActions) {
				for (InformationObserver obs : observers.values()) {
					try {
						if (dispInfo.getAction().equals(Action.CREATION)) {
							obs.informationCreated(dispInfo.getInformation());
						} else if (dispInfo.getAction().equals(Action.CLEAR)) {
							obs.clearInformation(dispInfo.getInformation());
						}
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
			this.newActions.clear();
		}
	}
}
