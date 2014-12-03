package org.coreasm.rmi.server.remoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 *
 */
public interface ErrorSubscription extends Remote {
	public void newError(String error) throws RemoteException;
}
