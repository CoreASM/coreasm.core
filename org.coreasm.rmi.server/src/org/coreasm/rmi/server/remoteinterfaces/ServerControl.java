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
public interface ServerControl extends Remote {
	EngineControl getNewEngine() throws RemoteException;
	EngineControl connectExistingEngine(String idNr) throws RemoteException;
}
