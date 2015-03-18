package org.coreasm.rmi.server.remoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Stephan
 *
 */
public interface UpdateSubscription extends Remote {	
	void newUpdates(String updates) throws RemoteException;
	void newUpdates(List<String> updates) throws RemoteException;
}
