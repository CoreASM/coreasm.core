package org.coreasm.rmi.server.remoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 *
 */
public interface UpdateSubscription extends Remote {	
	public void newUpdates(String updates) throws RemoteException;
}
