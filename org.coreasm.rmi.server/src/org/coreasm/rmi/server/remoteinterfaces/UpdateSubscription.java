package org.coreasm.rmi.server.remoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Stephan
 *
 */
public interface UpdateSubscription extends Remote {	
	public void newUpdates(String updates) throws RemoteException;
	public void newUpdates(List<String> updates) throws RemoteException;
}
