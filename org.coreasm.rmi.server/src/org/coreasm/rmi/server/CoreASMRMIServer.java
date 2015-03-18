/**
 * 
 */
package org.coreasm.rmi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


import java.util.concurrent.TimeUnit;

import org.coreasm.rmi.server.remoteinterfaces.EngineControl;
import org.coreasm.rmi.server.remoteinterfaces.EngineDriverInfo;
import org.coreasm.rmi.server.remoteinterfaces.ServerAdminControl;
import org.coreasm.rmi.server.remoteinterfaces.ServerControl;

/**
 * @author Stephan
 *
 */
public class CoreASMRMIServer extends UnicastRemoteObject implements
		ServerControl, ServerAdminControl {

	HashMap<String, EngineControl> engines = new HashMap<String, EngineControl>();
	private int maxPoolsize = 9;
	private ThreadPoolExecutor pool;
	private BlockingQueue<Runnable> taskQueue;

	protected CoreASMRMIServer() throws RemoteException {
		super();
//		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxPoolsize);
		taskQueue = new LinkedBlockingQueue<Runnable>();
		pool = new ThreadPoolExecutor(maxPoolsize, maxPoolsize, 0, TimeUnit.MILLISECONDS, taskQueue);
	}
	

	private static final long serialVersionUID = 1L;

	public void start() {

	}

	public void stop() {

	}

	/**
	 * 
	 */
	public static void main() {
		String name = "RMIServer";
		CoreASMRMIServer server;
		Registry registry;
		boolean exit;

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {

			server = new CoreASMRMIServer();
			server.start();
			try {
				registry = LocateRegistry.getRegistry();
				registry.list();
				System.out.println("Registry found");
			} catch (RemoteException e) {
				registry = LocateRegistry.createRegistry(1099);
				System.out.println("Registry bound to 1099");
			}
			registry.rebind(name, server);
			
			BufferedReader eingabe = new BufferedReader(new InputStreamReader(
					System.in));
			exit = false;
			while (!exit) {
				try {
					for (int i = 0; i < 10; i++) {
						Thread.sleep(1000);
						try {
							if (eingabe.ready()
									&& eingabe.readLine().equals("exit")) {
								exit = true;
								break;
							}
						} catch (IOException e) {
							System.out.print(e.getMessage());
						}
					}
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
			try {
				registry.unbind(name);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
			registry = null;
			server.stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		System.exit(0);

	}

	@Override
	public EngineControl getNewEngine() throws RemoteException {
		EngineControlImp newEngine = new EngineControlImp(UUID.randomUUID().toString());
		engines.put(newEngine.getIdNr(), newEngine);
		pool.execute(newEngine);
		return newEngine;
	}

	@Override
	public EngineControl connectExistingEngine(String idNr) throws RemoteException {
		return engines.get(idNr);
	}

	@Override
	public ArrayList<EngineDriverInfo> getEngineList() throws RemoteException {
		ArrayList<EngineDriverInfo> lst = new ArrayList<EngineDriverInfo>();
		Iterator<Map.Entry<String, EngineControl>> itr = engines.entrySet().iterator();
		while (itr.hasNext()) {
			lst.add(itr.next().getValue().getDriverInfo());
		}
		return lst;
	}

}
