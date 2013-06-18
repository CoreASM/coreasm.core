/**
 *
 */
package org.coreasm.engine.informationHandler;

import java.util.HashMap;
import java.util.LinkedList;

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

	/** thread specific information */
	private LinkedList<DispatcherContext> newActions;
	private DistributionMode distributionMode;

	HashMap<String, IInformationDispatchObserver> observers = new HashMap<String, IInformationDispatchObserver>();

	/**
	 * creates an Dispatcher with <code>DistributionMode.AUTOCOMMIT</code>
	 */
	public AbstractDispatcher() {
		newActions = new LinkedList<DispatcherContext>();
		distributionMode = DistributionMode.AUTOCOMMIT;
	}

	public AbstractDispatcher(DistributionMode mode) {
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
	protected void addSuperObserver(IInformationDispatchObserver iStorageAndDispatchObserver) {
		if (! this.observers.containsKey(iStorageAndDispatchObserver.getClass().getCanonicalName()))
			this.observers.put(iStorageAndDispatchObserver.getClass().getCanonicalName(), iStorageAndDispatchObserver);
	}

	/**
	 * remove an observer from the list of observers
	 *
	 * @param iStorageAndDispatchObserver
	 */
	protected void deleteSuperObserver(IInformationDispatchObserver iStorageAndDispatchObserver) {
		if (this.observers.containsKey(iStorageAndDispatchObserver.getClass().getCanonicalName()))
		this.observers.remove(iStorageAndDispatchObserver.getClass().getCanonicalName());
	}

	/**
	 * remove all observers
	 */
	protected void deleteSuperObervers() {
		this.observers.clear();
	}

	public synchronized void createInformation(InformationObject info) {
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

	public synchronized void clearInformation() {
		//Instantiate new DispatcherContext with a null-Object as information object
		DispatcherContext newInfoDispObject = new DispatcherContext(null, Action.CLEAR);

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

	/**
	 * call both interface functions of IStorageAndDispatchObserver foreach
	 * InformationDispatch belonging to the current thread. Add or remove each
	 * InformationObject from the information list of StorageAndDispatch. Clear
	 * the newActions entry from the newActions HashMap.
	 */
	private synchronized void notifyObservers() {
		if (!this.newActions.isEmpty()) {
			for (DispatcherContext dispInfo : this.newActions) {
				for (IInformationDispatchObserver obs : observers.values()) {
					if (dispInfo.getAction().equals(Action.CREATION)) {
						obs.informationCreated(dispInfo.getInformation());
					} else if (dispInfo.getAction().equals(Action.CLEAR)) {
						obs.clearInformation();
					}
				}
				this.newActions.remove(dispInfo);
			}
		}
	}
}
