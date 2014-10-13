package org.coreasm.rmi.server.remoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import org.coreasm.engine.absstorage.*;

/**
 * @author Stephan
 *
 */
public interface UpdateSubscription extends Remote {
	
	public void newUpdates(Set<Update> updates) throws RemoteException;

}
