package org.coreasm.rmi.server.remoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerAdminControl extends Remote {
	public ArrayList<EngineDriverInfo> getEngineList() throws RemoteException;
}
