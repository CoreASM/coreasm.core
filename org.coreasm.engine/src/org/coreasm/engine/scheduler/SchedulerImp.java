/*	
 * SchedulerImp.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 George Ma
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.EngineProperties;
import org.coreasm.engine.InvalidSpecificationException;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EDU.oswego.cs.dl.util.concurrent.FJTaskRunnerGroup;

/**
 * Implemetation of scheduler.
 * 
 * @author George Ma
 * 
 */
public class SchedulerImp implements Scheduler {

	/** Maximum number of agents selected in each round */
	public static final int MAX_SELECTED_AGENTS = 10;

	protected static final Logger logger = LoggerFactory
			.getLogger(SchedulerImp.class);

	private ControlAPI capi;

	private UpdateMultiset updateInstructions;
	private Set<Update> updateSet;

	private Set<Element> agentSet;
	private Set<Element> selectedAgentSet;

	private Element initAgent;

	// private FJTaskRunnerGroup runnerGroup = null;
	private int batchSize = -1;
	private int numberOfCPUs = -1;
	private SchedulingPolicy schedulingPolicy = null;
	private Iterator<Set<Element>> schedule = null;
	private boolean shouldPrintProcessorStats = false;

	// TODO: may want to define it as a long
	private int stepCount;

	/* to cache the list with minimum change to the code */
	private Set<Element> lastSelectedAgents;

	private AgentContextMap agentContextMap;

	public SchedulerImp(ControlAPI engine) {
		this.capi = engine;
		updateInstructions = new UpdateMultiset();
		updateSet = new HashSet<Update>();
		agentSet = null;
		selectedAgentSet = new HashSet<Element>();
		lastSelectedAgents = null;
		agentContextMap = new AgentContextMap();
	}

	public void prepareInitialState() throws InvalidSpecificationException {
		Interpreter interpreter = capi.getInterpreter();

		// prepare the initial agent
		interpreter.prepareInitialState();

		loadSchedulingPolicy();

		shouldPrintProcessorStats = (capi.getProperty(
				EngineProperties.PRINT_PROCESSOR_STATS_PROPERTY, "no")
				.toUpperCase().equals("YES"));

		agentContextMap = new AgentContextMap();

		logger.debug("Done preparing the initial state.");
	}

	@Deprecated
	public void executeInitialization() throws InvalidSpecificationException {
		prepareInitialState();
	}

	public Set<Update> getUpdateSet() {
		return updateSet;
	}

	public UpdateMultiset getUpdateInstructions() {
		return updateInstructions;
	}

	public synchronized Set<Element> getAgentSet() {
		return new HashSet<Element>(agentSet);
	}

	public Set<Element> getSelectedAgentSet() {
		return selectedAgentSet;
	}

	/*
	 * public Element getChosenAgent() { return chosenAgent; }
	 */

	public synchronized void startStep() {
		updateInstructions = new UpdateMultiset();
		// changed by Roozbeh
		// updateSet.clear();
		updateSet = new HashSet<Update>();
		agentSet = null;
		selectedAgentSet.clear();
	}

	public synchronized void retrieveAgents() {
		// debugged by Roozbeh Farahbod, 17-Jan-2006
		AbstractStorage storage = capi.getStorage();

		FunctionElement agentSetFlat = storage
				.getUniverse(AbstractStorage.AGENTS_UNIVERSE_NAME);

		if (agentSetFlat instanceof Enumerable) {
			if (stepCount < 1) {
				agentSet = new HashSet<Element>();
				agentSet.add(initAgent);
			} else {
				agentSet = new HashSet<Element>();
				// pick only those that have a non-null program
				for (Element agent : ((Enumerable) agentSetFlat).enumerate()) {
					Location loc = new Location(
							AbstractStorage.PROGRAM_FUNCTION_NAME,
							ElementList.create(agent));
					try {
						if (!storage.getValue(loc).equals(Element.UNDEF))
							agentSet.add(agent);
					} catch (InvalidLocationException e) {
						capi.error("Cannot get the value of lcoation " + loc
								+ ".");
						logger.error("Cannot get the value of lcoation " + loc
								+ ".");
					}
				}
			}

		} else {
			String msg = "Value of \"Agents\" is not enumerable. Cannot determine the agent set.";
			logger.error(msg);
			throw new EngineError(msg);
		}

		/*
		 * We want to group the schedules for the whole run, so we set the group
		 * handle to be this instance of scheduling policy that we are using in
		 * this run.
		 */
		schedule = schedulingPolicy.getNewSchedule(schedulingPolicy, agentSet);
	}

	public boolean selectAgents() {
		if (agentsCombinationExists()) {
			selectedAgentSet = schedule.next();
			logger.debug("Selected Agent Set is '{}'.", selectedAgentSet);
			lastSelectedAgents = Collections.unmodifiableSet(selectedAgentSet);
			return true;
		} else {
			selectedAgentSet = Collections.emptySet();
			lastSelectedAgents = selectedAgentSet;
			return false;
		}

	}

	/*
	 * Removed from the concurrent version of the engine.
	 * 
	 * public void chooseAgent() { // if there are no more agents, we are done
	 * this step if (selectedAgentSet.size() == 0) { chosenAgent = null; } else
	 * if (selectedAgentSet.size() == 1) { ArrayList<Element> agentsList = new
	 * ArrayList<Element>(selectedAgentSet); chosenAgent = agentsList.get(0); }
	 * else { Random random = new Random();
	 * 
	 * // chose an agent from the selected agent set randomly int
	 * chosenAgentIndex = random.nextInt(selectedAgentSet.size());
	 * 
	 * // this may be inefficient ArrayList<Element> agentsList = new
	 * ArrayList<Element>(selectedAgentSet); chosenAgent =
	 * agentsList.get(chosenAgentIndex); }
	 * 
	 * // debugged by Roozbeh Farahbod, 18-Jan-2006 if (chosenAgent != null) {
	 * selectedAgentSet.remove(chosenAgent);
	 * capi.getStorage().setChosenAgent(chosenAgent); } }
	 */

	public void executeAgentPrograms() throws EngineException {
		ArrayList<Element> agentsList = new ArrayList<Element>(selectedAgentSet);

		/*
		 * Old Code
		 * 
		 * Before, we would keep a copy of the runnerGroup and reuse it. This
		 * appeared to be problamatic with regard to memory issues. Changing it
		 * to one instance per time...
		 * 
		 * if (runnerGroup == null) { //TODO the number of available processes
		 * may change! int cpus = getNumberOfProcessorsToBeUsed(); batchSize =
		 * getThreadBatchSize(); runnerGroup = new FJTaskRunnerGroup(cpus); if
		 * (Logger.verbosityLevel == Logger.INFORMATION) {
		 * Logger.log(Logger.INFORMATION, Logger.scheduler, "Using " + cpus +
		 * " thread(s) on " + Runtime.getRuntime().availableProcessors() +
		 * " processors."); Logger.log(Logger.INFORMATION, Logger.scheduler,
		 * "Using a batch size of " + batchSize + " agent(s) per thread."); } }
		 */

		if (batchSize == -1) {
			numberOfCPUs = getNumberOfProcessorsToBeUsed();
			batchSize = getThreadBatchSize();
			if (logger.isDebugEnabled()) {
				logger.debug("Using " + numberOfCPUs + " thread(s) on "
						+ Runtime.getRuntime().availableProcessors()
						+ " processors.");
				logger.debug("Using a batch size of " + batchSize
						+ " agent(s) per thread.");
			}
		}
		final FJTaskRunnerGroup runnerGroup = new FJTaskRunnerGroup(
				numberOfCPUs);

		ConcurrentProgramEvaluator cpe = new ConcurrentProgramEvaluator(capi,
				agentContextMap, agentsList, 0, agentsList.size(), batchSize);
		try {
			runnerGroup.invoke(cpe);
		} catch (InterruptedException e) {
			runnerGroup.interruptAll();
			throw new EngineException(
					"Could not finish program evaluation due to "
							+ "the following interrupted exception: " + e);
		}

		if (shouldPrintProcessorStats)
			runnerGroup.stats();

		UpdateMultiset updates = cpe.getResultantUpdateSet();

		if (updates == null) {
			if (cpe.getError() == null)
				throw new EngineException("A fatal error occurred that could not be caught.");
			logger.error(cpe.getError().toString());
			StackTraceElement[] trace = cpe.getError().getStackTrace();
			for (StackTraceElement ste : trace)
				logger.error(ste.toString());
			
			throw new EngineException(cpe.getError());
			// capi.error(cpe.getError());
		}

		runnerGroup.interruptAll();
		updateInstructions = updates;
	}

	/*
	 * removed from the concurrent version of the Engine
	 * 
	 * @SuppressWarnings("unchecked") public void accumulateUpdates() { //
	 * Edited by Roozbeh Farahbod, 18-Jan-2006 Logger.log(Logger.INFORMATION,
	 * Logger.scheduler, "Accumulating updates."); Collection c =
	 * chosenProgram.getBody().getUpdates();
	 * 
	 * // 'if' Added by Roozbeh Farahbod, 29-Sep-2006 if (c == null) { String
	 * msg = "Rule '" + chosenProgram.getName() + "' provides no updates.";
	 * Logger.log(Logger.ERROR, Logger.scheduler, msg); throw new
	 * EngineError(msg); }
	 * 
	 * updateInstructions.addAll(c); Logger.log(Logger.INFORMATION,
	 * Logger.scheduler, "Updates are: " + c.toString()); }
	 */

	/*
	 * removed from the concurrent version of the Engine
	 * 
	 * public void initiateExecution() {
	 * capi.getInterpreter().setPosition(chosenProgram.getBody()); //
	 * 'initiateProgramExecution' replaces the clearTree method call // to allow
	 * the interpreter to perform internal initialization // prior to program
	 * execution capi.getInterpreter().initProgramExecution();
	 * //capi.getInterpreter().clearTree(capi.getInterpreter().getPosition()); }
	 */

	public void handleFailedUpdate() {
		// does not need to do anything.
	}

	/*
	 * public RuleElement getChosenProgram() { return chosenProgram; }
	 */

	public boolean isSingleAgentInconsistent() {
		// check to see if the inconsistency is from one single agent
		// or due to a combination of agents -- Roozbeh F., March 2008
		Set<Update> inconsistentUpdates = capi.getStorage()
				.getLastInconsistentUpdate();
		boolean result = false;

		if (inconsistentUpdates != null) {
			Set<Element> agents = new HashSet<Element>();
			for (Update u : inconsistentUpdates) {
				if (u.agents != null)
					agents.addAll(u.agents);
			}
			if (agents.size() == 1)
				result = true;
		}

		return result;
	}

	public boolean agentsCombinationExists() {
		return schedule.hasNext();
	}

	/*
	 * public void setChosenProgram(RuleElement program) { chosenProgram =
	 * program; }
	 */

	public Element getInitAgent() {
		return initAgent;
	}

	public void setInitAgent(Element agent) {
		initAgent = agent;
	}

	public int getStepCount() {
		return stepCount;
	}

	public void incrementStepCount() {
		stepCount++;
	}

	public void setStepCount(int count) {
		stepCount = count;
	}

	public Set<Element> getLastSelectedAgents() {
		if (lastSelectedAgents != null)
			return Collections.unmodifiableSet(lastSelectedAgents);
		else
			return Collections.emptySet();
	}

	/*
	 * Looks into scheduler plugins and find a scheduling policy or creates a
	 * default one.
	 */
	private void loadSchedulingPolicy() {
		List<SchedulingPolicy> possiblePolicies = new ArrayList<SchedulingPolicy>();
		Set<Plugin> providers = new HashSet<Plugin>();
		for (Plugin p : capi.getPlugins())
			if (p instanceof SchedulerPlugin) {
				SchedulingPolicy policy = ((SchedulerPlugin) p).getPolicy();
				if (policy != null) {
					possiblePolicies.add(policy);
					providers.add(p);
				}
			}
		if (possiblePolicies.size() == 0)
			schedulingPolicy = new DefaultSchedulingPolicy();
		else if (possiblePolicies.size() == 1)
			schedulingPolicy = possiblePolicies.get(0);
		else if (possiblePolicies.size() > 1) {
			throw new EngineError(
					"Conflicting scheduling policies provided by " + providers
							+ ".");
		}
	}

	/*
	 * Based on the number of processors on the machine and the limit set by the
	 * user, returns the number of processors to be used for simulation.
	 */
	private int getNumberOfProcessorsToBeUsed() {
		// int cpus = Runtime.getRuntime().availableProcessors();
		int limit = 1;
		String limitStr = capi.getProperty(EngineProperties.MAX_PROCESSORS);
		if (limitStr != null) {
			try {
				limit = Integer.valueOf(limitStr);
			} catch (NumberFormatException e) {
				logger.warn("Invalid value for \""
						+ EngineProperties.MAX_PROCESSORS
						+ "\" engine property (" + limitStr + ").");
			}
		}
		return limit;
	}

	/*
	 * Returns the user defined value for the minimum number of agents to be
	 * assigned to every execution thread during the simulation.
	 */
	private int getThreadBatchSize() {
		int size = 1;
		String sizeStr = capi.getProperty(
				EngineProperties.AGENT_EXECUTION_THREAD_BATCH_SIZE, "1");
		try {
			size = Integer.valueOf(sizeStr);
		} catch (NumberFormatException e) {
			logger.warn("Invalid value for \""
					+ EngineProperties.AGENT_EXECUTION_THREAD_BATCH_SIZE
					+ "\" engine property (" + sizeStr + ").");
		}
		return size;
	}

	@Override
	public void dispose() {
		agentContextMap.clear();
	}
}
