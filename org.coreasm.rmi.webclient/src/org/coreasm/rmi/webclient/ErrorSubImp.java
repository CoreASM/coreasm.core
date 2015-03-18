/**
 * 
 */
package org.coreasm.rmi.webclient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.rmi.server.remoteinterfaces.ErrorSubscription;

/**
 * @author Stephan
 *
 */
public class ErrorSubImp extends UnicastRemoteObject implements
		ErrorSubscription {
	private static final long serialVersionUID = 1L;
	private List<String> errors;

	public ErrorSubImp() throws RemoteException {
		errors = new ArrayList<String>();
	}

	public List<String> getErrors(boolean deleteOld) {
		List<String> newList = new ArrayList<String>();
		
		synchronized (errors) {
			newList.addAll(errors);
			if (deleteOld) {
				errors.clear();
			}
			return newList;			
		}
	}

	@Override
	public void newError(String error) throws RemoteException {
		synchronized (errors) {
			errors.add(error);
		}		
	}

}
