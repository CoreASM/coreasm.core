package org.coreasm.rmi.webclient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.rmi.server.remoteinterfaces.UpdateSubscription;


public class UpdateSubImp extends UnicastRemoteObject implements
		UpdateSubscription {
	private static final long serialVersionUID = 1L;
	private List<String> updateSets;

	public UpdateSubImp() throws RemoteException {
		updateSets = new ArrayList<String>();
	}

	@Override
	public void newUpdates(String updates) throws RemoteException {
		synchronized (updateSets) {
			updateSets.add(updates);
		}
	}
	@Override
	public void newUpdates(List<String> updates) throws RemoteException {
		synchronized (updateSets) {
			updateSets.addAll(updates);
		}
	}
	
	public List<String> getUpdates(boolean deleteOld) {
		List<String> newList = new ArrayList<String>();
		
		synchronized (updateSets) {
			newList.addAll(updateSets);
			if (deleteOld) {
				updateSets.clear();
			}
			return newList;			
		}
	}
}
