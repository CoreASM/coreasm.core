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
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.coreasm.rmi.server.remoteinterfaces.*;

/**
 * @author Stephan
 *
 */
public class CoreASMRMIServer extends UnicastRemoteObject implements
		ServerControl {

	HashMap<String, EngineControl> engines = new HashMap<String, EngineControl>();
	private int maxPoolsize = 9;
	private ExecutorService pool;

	protected CoreASMRMIServer() throws RemoteException {
		super();
		pool = Executors.newFixedThreadPool(maxPoolsize);
	}

	private static final long serialVersionUID = 1L;

	public void Start() {

	}

	public void Stop() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String name = "RMIServer";
		CoreASMRMIServer server;
		Registry registry;
		boolean exit;

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {

			server = new CoreASMRMIServer();
			server.Start();
			try {
				registry = LocateRegistry.getRegistry();
				registry.list();
			} catch (RemoteException e) {
				registry = LocateRegistry.createRegistry(1099);
			}
			registry.rebind(name, server);
			System.out.println("Registry bound to 1099");
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
						}
					}
				} catch (InterruptedException e) {
				}
			}
			try {
				registry.unbind(name);
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
			registry = null;
			server.Stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		System.exit(0);

	}

	@Override
	public EngineControl getNewEngine() throws RemoteException {
		EngineControlImp newEngine = new EngineControlImp(UUID.randomUUID().toString());
		pool.execute(newEngine);
		return newEngine;
	}

	@Override
	public EngineControl connectExistingEngine(String idNr) throws RemoteException {
		return engines.get(idNr);
	}

}
