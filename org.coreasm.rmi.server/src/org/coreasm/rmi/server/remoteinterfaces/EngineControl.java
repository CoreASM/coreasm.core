/**
 * 
 */
package org.coreasm.rmi.server.remoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 *
 */
public interface EngineControl extends Remote {
	public enum EngineDriverStatus {
		stopped, running, paused, empty
	};
	String getIdNr() throws RemoteException;
	void start() throws RemoteException;
	void pause() throws RemoteException;
	void stop() throws RemoteException;
	void singleStep() throws RemoteException;
	void load(byte[] specification) throws RemoteException;
	void subscribeUpdates(UpdateSubscription sub) throws RemoteException;
	void subscribeErrors(ErrorSubscription sub) throws RemoteException;
	void addUpdate(String value, String agent) throws RemoteException;
	EngineDriverStatus getDriverStatus() throws RemoteException;
	EngineDriverInfo getDriverInfo() throws RemoteException;
	String getAgentlist() throws RemoteException;
}
