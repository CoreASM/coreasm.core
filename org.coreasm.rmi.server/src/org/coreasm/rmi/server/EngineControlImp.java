package org.coreasm.rmi.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.EngineErrorEvent;
import org.coreasm.engine.EngineErrorObserver;
import org.coreasm.engine.EngineEvent;
import org.coreasm.engine.EngineModeEvent;
import org.coreasm.engine.EngineModeObserver;
import org.coreasm.engine.EngineStepObserver;
import org.coreasm.engine.StepFailedEvent;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterListener;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.rmi.server.remoteinterfaces.EngineControl;
import org.coreasm.rmi.server.remoteinterfaces.EngineDriverInfo;
import org.coreasm.rmi.server.remoteinterfaces.ErrorSubscription;
import org.coreasm.rmi.server.remoteinterfaces.UpdateSubscription;

//import org.coreasm.engine.plugins.schedulingpolicies.SchedulingPoliciesPlugin;

public class EngineControlImp extends UnicastRemoteObject implements Runnable,
		EngineControl, EngineStepObserver, EngineErrorObserver,
		EngineModeObserver, InterpreterListener {
	private static final long serialVersionUID = 1L;
	private ControlAPI engine;
	private byte[] spec = null;
	private boolean updateFailed;
	protected CoreASMError lastError;

	private boolean stopOnEmptyUpdates;
	private boolean stopOnStableUpdates;
	private boolean stopOnEmptyActiveAgents;
	private boolean stopOnFailedUpdates;
	private boolean stopOnError;
	private boolean stopOnStepsLimit;
	private int stepsLimit;

	private volatile boolean shouldStop = false;
	private volatile boolean shouldPause = true;
	private volatile boolean takeSingleStep = false;
	private volatile boolean shouldReset = true;
	private volatile EngineDriverInfo driverInfo;

	private List<Set<Update>> previousUpdates;
	private Map<String, ArrayList<String>> pendingUpdates;
	private Map<String, ArrayList<String>> updateMap;
	private Map<ASTNode, ASTNode> parentCache;
	private UpdateMultiset pendingChanges;
	private UpdateMultiset queuedChanges;
	private List<UpdateSubscription> newUpdateSubscriptions;
	private List<UpdateSubscription> updateSubscriptions;
	private List<ErrorSubscription> errorSubscriptions;

	private EngineControlImp() throws RemoteException {
		super();
		initEngine();

		stopOnEmptyUpdates = false;
		stopOnStableUpdates = false;
		stopOnEmptyActiveAgents = true;
		stopOnFailedUpdates = true;
		stopOnError = true;
		stopOnStepsLimit = false;
		stepsLimit = 20;

		previousUpdates = new ArrayList<Set<Update>>();
		newUpdateSubscriptions = new ArrayList<UpdateSubscription>();
		updateSubscriptions = new ArrayList<UpdateSubscription>();
		errorSubscriptions = new ArrayList<ErrorSubscription>();
		driverInfo = new EngineDriverInfo("", EngineDriverStatus.empty);

		pendingUpdates = new HashMap<String, ArrayList<String>>();
		updateMap = new HashMap<String, ArrayList<String>>();
		parentCache = new HashMap<ASTNode, ASTNode>();

		pendingChanges = new UpdateMultiset();
		queuedChanges = new UpdateMultiset();
	}

	private void initEngine() {
		engine = (ControlAPI) CoreASMEngineFactory.createEngine();
		engine.setClassLoader(CoreASMEngineFactory.class.getClassLoader());
		engine.initialize();
		engine.waitWhileBusy();
		engine.addObserver(this);
		engine.addInterpreterListener(this);
	}

	public EngineControlImp(String id) throws RemoteException {
		this();
		driverInfo.setId(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coreasm.engine.EngineObserver#update(org.coreasm.engine.EngineEvent)
	 */
	@Override
	public void update(EngineEvent event) {
		// Looking for StepFailed
		if (event instanceof StepFailedEvent) {
			synchronized (this) {
				updateFailed = true;
			}
			if (!errorSubscriptions.isEmpty()) {
				synchronized (errorSubscriptions) {
					propagateError("Update Failed!");
				}
			}
		}

		// Looking for errors
		else if (event instanceof EngineErrorEvent) {
			synchronized (this) {
				lastError = ((EngineErrorEvent) event).getError();
			}
			if (!errorSubscriptions.isEmpty()) {
				synchronized (errorSubscriptions) {
					propagateError(lastError);
				}
			}
		}

		// Pushing changes
		else if (event instanceof EngineModeEvent) {
			if (((EngineModeEvent) event).getNewMode() == EngineMode.emAggregation) {
				UpdateMultiset updates = engine.getUpdateInstructions();
				updates.addAll(pendingChanges);
				pendingChanges.clear();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#getIdNr()
	 */
	@Override
	public String getIdNr() throws RemoteException {
		return driverInfo.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#start()
	 */
	@Override
	public void start() throws RemoteException {
		if (shouldPause) {
			shouldPause = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#pause()
	 */
	@Override
	public void pause() throws RemoteException {
		shouldPause = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#stop()
	 */
	@Override
	public void stop() throws RemoteException {
		shouldStop = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.rmi.server.remoteinterfaces.EngineControl#load(byte[])
	 */
	@Override
	public void load(byte[] specification) throws RemoteException {
		spec = specification;
		driverInfo.setStatus(EngineDriverStatus.paused);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.coreasm.rmi.server.remoteinterfaces.EngineControl#subscribe(org.coreasm
	 * .rmi.server.remoteinterfaces.UpdateSubscription)
	 */
	@Override
	public void subscribeUpdates(UpdateSubscription sub) throws RemoteException {
		if (driverInfo.getStatus() == EngineDriverStatus.paused) {
			// prevents updates till subscription finishes
			synchronized (updateSubscriptions) {
				synchronized (previousUpdates) {
					if (!previousUpdates.isEmpty()) {
						ArrayList<String> updtLst = new ArrayList<String>();
						for (Set<Update> updt : previousUpdates) {
							updtLst.add(getUpdateString(updt));
						}
						try {
							sub.newUpdates(updtLst);
						} catch (RemoteException e) {
							return;
						}
					}
				}
				updateSubscriptions.add(sub);
			}
		} else {
			synchronized (newUpdateSubscriptions) {
				newUpdateSubscriptions.add(sub);
			}
		}
	}

	@Override
	public void subscribeErrors(ErrorSubscription sub) throws RemoteException {
		synchronized (errorSubscriptions) {
			errorSubscriptions.add(sub);
		}
	}

	public void run() {
		while (shouldReset) {
			if (shouldStop) { //set if reset(...) got called
			   shouldStop = false;
			   shouldPause = true;
			   takeSingleStep = false;
			   initEngine();
			}
			int step = 0;
			Exception exception = null;

			Set<Update> updates, prevupdates = null;

			try {

				if (engine.getEngineMode() == EngineMode.emError) {
					engine.recover();
					engine.waitWhileBusy();
				}
				while (spec == null) {
					Thread.sleep(500);
				}

				shouldReset = false;
				ByteArrayInputStream in = new ByteArrayInputStream(spec);
				engine.loadSpecification(new BufferedReader(
						new InputStreamReader(in)));
				engine.waitWhileBusy();
				if (engine.getEngineMode() != EngineMode.emIdle) {
					handleError();
					return;
				}

				if (shouldStop)
					throw new EngineDriverException();

				while (engine.getEngineMode() == EngineMode.emIdle) {
					if (!previousUpdates.isEmpty()
							&& !newUpdateSubscriptions.isEmpty()) {
						UpdateSubscription sub;
						ArrayList<String> updtLst = new ArrayList<String>();
						synchronized (previousUpdates) {
							for (Set<Update> updt : previousUpdates) {
								updtLst.add(getUpdateString(updt));
							}
						}
						synchronized (newUpdateSubscriptions) {
							Iterator<UpdateSubscription> subItr = newUpdateSubscriptions
									.iterator();
							while (subItr.hasNext()) {
								sub = subItr.next();
								try {
									sub.newUpdates(updtLst);
								} catch (RemoteException e) {
									subItr.remove();
									continue;
								}
								subItr.remove();
								updateSubscriptions.add(sub);
							}
						}
					}
					if (shouldPause) {
						driverInfo.setStatus(EngineDriverStatus.paused);
						System.err
								.println("[!] Run is paused by user. Click on resume to continue...");
						int pausecount = 0;
						while (shouldPause && !takeSingleStep && !shouldStop) {
							Thread.sleep(100);
							pausecount++;
							if (pausecount == 18000)
								shouldStop = true;
						}
						if (!shouldStop)
							System.err.println("[!] Resuming.");

					}

					if (shouldStop) {
						throw new EngineDriverException();
					}

					takeSingleStep = false;

					synchronized (queuedChanges) {
						pendingChanges.addAll(queuedChanges);
						queuedChanges.clear();
					}

					synchronized (updateMap) {
						if (!pendingUpdates.isEmpty()) {
							updateMap.putAll(pendingUpdates);
							pendingUpdates.clear();
						}
					}

					driverInfo.setStatus(EngineDriverStatus.running);

					engine.step();
					step++;

					while (!shouldStop && engine.isBusy())
						Thread.sleep(50);

					if (shouldStop) {
						// give some time to the engine to finish
						if (engine.isBusy())
							Thread.sleep(200);

						throw new EngineDriverException();
					}

					updates = engine.getUpdateSet(0);

					if (terminated(step, updates, prevupdates))
						break;
					prevupdates = updates;
					synchronized (previousUpdates) {
						previousUpdates.add(updates);
					}

					resetParents();

					propagateUpdate(updates);
				}
				if (engine.getEngineMode() != EngineMode.emIdle)
					handleError();
			} catch (Exception e) {
				exception = e;
			} finally {
				engine.removeObserver(this);
				if (exception != null)
					if (exception instanceof EngineDriverException)
						System.err.println("[!] Run is terminated by user.");
					else {
						System.err
								.println("[!] Run is terminated with exception "
										+ exception);
					}

				// Repeating
				if (exception != null)
					if (exception instanceof EngineDriverException)
						System.err.println("[!] Run is terminated by user.");
					else
						System.err
								.println("[!] Run is terminated with exception "
										+ exception);
			}
			engine.terminate();
			driverInfo.setStatus(EngineDriverStatus.stopped);
		}
	}

	private void resetParents() {
		for (Map.Entry<ASTNode, ASTNode> nodePair : parentCache.entrySet()) {
			nodePair.getKey().setParent(nodePair.getValue());
		}
		parentCache.clear();
	}

	private boolean terminated(int step, Set<Update> updates,
			Set<Update> prevupdates) {
		if (stopOnEmptyUpdates && updates.isEmpty())
			return true;
		if (stopOnStableUpdates && updates.equals(prevupdates))
			return true;
		if (stopOnEmptyActiveAgents && engine.getAgentSet().size() < 1)
			return true;
		if (stopOnFailedUpdates && updateFailed)
			return true;
		if (stopOnError && lastError != null)
			return true;
		if (stopOnStepsLimit && step > stepsLimit)
			return true;
		return false;
	}

	protected void handleError() {
		String message = "";
		if (lastError != null)
			message = lastError.showError();
		else
			message = "Enginemode should be " + EngineMode.emIdle + " but is "
					+ engine.getEngineMode();

		// JOptionPane.ERROR_MESSAGE);
		System.out.println("CoreASM Engine Error");
		System.out.println(message);

		lastError = null;
		engine.recover();
		engine.waitWhileBusy();
	}

	private class EngineDriverException extends Exception {
		private static final long serialVersionUID = 1L;

		public EngineDriverException() {

		}
	}

	public void finalize() {
		engine.terminate();
	}

	private String getUpdateString(Set<Update> updates) {
		Iterator<Update> itrUpdt;
		Update updt;
		StringBuilder updateBuilder = new StringBuilder();
		updateBuilder.append('[');
		itrUpdt = updates.iterator();
		while (itrUpdt.hasNext()) {
			updt = itrUpdt.next();
			updateBuilder.append("{\"location\":\"" + updt.getLocationString()
					+ '"');
			updateBuilder.append(", \"value\":\""
					+ updt.getValueString().replaceAll("(\\r|\\n)", "") + '\"');
			updateBuilder.append(", \"action\":\"" + updt.getActionString()
					+ "\"}");
			if (itrUpdt.hasNext()) {
				updateBuilder.append(", ");
			}
		}
		updateBuilder.append(']');
		return updateBuilder.toString();
	}

	private void propagateUpdate(Set<Update> updates) {
		synchronized (updateSubscriptions) {
			Iterator<UpdateSubscription> itrSub = updateSubscriptions
					.iterator();
			String update = getUpdateString(updates);
			UpdateSubscription sub;
			while (itrSub.hasNext()) {
				sub = itrSub.next();
				try {
					sub.newUpdates(update);
				} catch (RemoteException e) {
					itrSub.remove();
				}
			}
			if (updateSubscriptions.isEmpty()
					&& newUpdateSubscriptions.isEmpty()) {
				shouldStop = true;
			}
		}
	}

	private void propagateError(CoreASMError error) {
		propagateError(error.showError());
	}

	private void propagateError(String error) {
		synchronized (errorSubscriptions) {
			Iterator<ErrorSubscription> itrSub = errorSubscriptions.iterator();
			ErrorSubscription sub;
			while (itrSub.hasNext()) {
				sub = itrSub.next();
				try {
					sub.newError(error);
				} catch (RemoteException e) {
					itrSub.remove();
				}
			}
		}
	}

	@Override
	public EngineDriverStatus getDriverStatus() throws RemoteException {
		return driverInfo.getStatus();
	}

	@Override
	public EngineDriverInfo getDriverInfo() throws RemoteException {
		return driverInfo;
	}

	@Override
	public void addUpdate(String value, String agent) throws RemoteException {
		synchronized (updateMap) {
			ArrayList<String> updtLst = pendingUpdates.get(agent);
			if (updtLst == null) {
				updtLst = new ArrayList<String>();
				pendingUpdates.put(agent, updtLst);
			}
			updtLst.add(value);
		}
	}

	@Override
	public void singleStep() throws RemoteException {
		takeSingleStep = true;
	}

	@Override
	public String getAgentlist() throws RemoteException {
		StringBuilder agentList = new StringBuilder();
		Element el;
		agentList.append('[');
		if (engine.getStepCount() > 0) {
			Iterator<? extends Element> itr = engine.getAgentSet().iterator();
			if (itr.hasNext()) {
				el = itr.next();
				if (el instanceof EnumerationElement)
					agentList.append("{\"name\":\""
							+ ((EnumerationElement) el).getName() + "\"}");
			}
			while (itr.hasNext()) {
				agentList.append(", ");
				el = itr.next();
				agentList.append("{\"name\":\""
						+ ((EnumerationElement) el).getName() + "\"}");
			}
		}
		agentList.append(']');
		return agentList.toString();
	}

	@Override
	public void beforeNodeEvaluation(ASTNode pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterNodeEvaluation(ASTNode pos) {
		// TODO Auto-generated method stub

	}

	// only called on evaluation of an agent
	@Override
	public void initProgramExecution(Element agent, RuleElement program) {
		ArrayList<String> updtLst = null;
		synchronized (updateMap) {
			if ((updateMap.size() != 0)
					&& (agent instanceof EnumerationElement)) {
				String agentName = ((EnumerationElement) agent).getName();
				updtLst = updateMap.remove(agentName);
			}
		}

		// generating a par-block containing all updates queued for the passed
		// agent
		if (updtLst != null && !updtLst.isEmpty()) {
			String updtCode = "par\n";
			for (String update : updtLst) {
				updtCode += update + '\n';
			}
			updtCode += "endpar\n";

			// parsing the generated block
			Parser<Node> blockParser = ((ParserPlugin) engine
					.getPlugin("BlockRulePlugin")).getParsers().get("Rule").parser;
			ParserTools parserTools = ParserTools.getInstance(engine);
			Parser<Node> parser = blockParser.from(parserTools.getTokenizer(),
					parserTools.getIgnored());
			ASTNode updtTree = null;
			try {
				updtTree = (ASTNode) parser.parse(updtCode);
			} catch (ParserException e) {
				propagateError(e.getMessage());
				updtTree = null;
			}

			if (updtTree != null) {
				// inserting the block as root of the calling interpreter
				Interpreter intr = engine.getInterpreter()
						.getInterpreterInstance();
				ASTNode node = intr.getPosition();
				parentCache.put(node, node.getParent());
				updtTree.addChildAfter(null, "", node);
				intr.setPosition(updtTree);
			}
		}
	}

	@Override
	public void onRuleCall(RuleElement rule, List<ASTNode> args, ASTNode pos,
			Element agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRuleExit(RuleElement rule, List<ASTNode> args, ASTNode pos,
			Element agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeValue(String location, String value)
			throws RemoteException {
		String name = location.substring(0, location.indexOf('('));
		FunctionElement function = engine.getState().getFunction(name);

		Element val = null;
		try {
			val = NumberElement.getInstance(Double.parseDouble(value));
		} catch (NumberFormatException e) {
			if (BooleanElement.TRUE_NAME.equals(value))
				val = BooleanElement.TRUE;
			else if (BooleanElement.FALSE_NAME.equals(value))
				val = BooleanElement.FALSE;
			else if (value.startsWith("\"") && value.endsWith("\""))
				val = new StringElement(value);
			else if (Character.isLetterOrDigit(value.charAt(0)))
				val = new EnumerationElement(value);
		}
		synchronized (queuedChanges) {
			for (Location loc : function.getLocations(name)) {
				if (loc.toString().equals(location)) {
					queuedChanges.add(new Update(loc, val,
							Update.UPDATE_ACTION, (Element) null, null));
				}
			}
		}
	}

	@Override
	public void reset(boolean keepSpec) throws RemoteException {
		if (!keepSpec) {
			spec = null;
		}
		shouldReset = true;
		shouldStop = true;

	}

}
