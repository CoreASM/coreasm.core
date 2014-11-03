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
	public String getIdNr() throws RemoteException;
	public void start() throws RemoteException;
	public void pause() throws RemoteException;
	public void stop() throws RemoteException;
	public void load(byte[] specification) throws RemoteException;
	public void subscribe(UpdateSubscription sub) throws RemoteException;
	public EngineDriverStatus getDriverStatus() throws RemoteException;
	public EngineDriverInfo getDriverInfo() throws RemoteException;
}
