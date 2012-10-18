package org.coreasm.eclipse.engine.debugger;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.debug.core.model.ASMLineBreakpoint;
import org.coreasm.eclipse.debug.core.model.ASMMethodBreakpoint;
import org.coreasm.eclipse.debug.core.model.ASMState;
import org.coreasm.eclipse.debug.core.model.ASMWatchpoint;
import org.coreasm.eclipse.debug.ui.views.ASMUpdate;
import org.coreasm.eclipse.debug.util.ASMDebugUtils;
import org.coreasm.eclipse.engine.driver.EngineDriver;
import org.coreasm.eclipse.launch.ICoreASMConfigConstants;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.EngineEvent;
import org.coreasm.engine.EngineModeEvent;
import org.coreasm.engine.EngineModeObserver;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.InterpreterListener;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.coreasm.engine.kernel.UpdateRuleNode;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.engine.plugins.turboasm.SeqBlockRuleNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * This is the main class of the the debugger. It controls the engine and provides access to it's state.
 * @author Michael Stegmaier
 *
 */
public class EngineDebugger extends EngineDriver implements EngineModeObserver, InterpreterListener {
	
	private ControlAPI capi = (ControlAPI) engine;
	private Stack<ASMState> states = new Stack<ASMState>();
	private ASMDebugTarget debugTarget;
	private String sourceName;
	private String specPath;
	private Element currentAgent;
	private int lineNumber;
	private ASTNode prevPos;
	private Set<Update> updates = new HashSet<Update>();
	private boolean stepSucceeded = false;
	private boolean accessBreakpointHit = false;
	private volatile boolean shouldStep = false;
	private volatile boolean shouldStepOver = false;
	private volatile boolean shouldStepInto = false;

	protected EngineDebugger(boolean isSyntaxEngine) {
		super(isSyntaxEngine);
		capi.addInterpreterListener(this);
	}
	
	/**
	 * Returns the running instance of the debugger.
	 * @return the running instance of the debugger
	 */
	public static EngineDebugger getRunningInstance() {
		if (runningInstance instanceof EngineDebugger)
			return (EngineDebugger) runningInstance;
		return null;
	}
	
	@Override
	public synchronized void stop() {
		shouldStep = false;
		super.stop();
	}
	
	@Override
	public synchronized void resume() {
		shouldStep = false;
		super.resume();
	}
	
	/**
	 * Sets the stepping mode of the debugger
	 * @param shouldStep value to set the stepping mode to
	 */
	public synchronized void setStepping(boolean shouldStep) {
		this.shouldStep = shouldStep;
	}
	
	/**
	 * Returns whether the debugger is stepping.
	 * @return whether the debugger is stepping
	 */
	public synchronized boolean isStepping() {
		return shouldStep;
	}
	
	/**
	 * Returns whether the debugger is stepping into.
	 * @return whether the debugger is stepping into
	 */
	public synchronized boolean isSteppingInto() {
		return shouldStepInto;
	}
	
	/**
	 * Executes a single engine step
	 */
	public synchronized void stepOver() {
		shouldStep = true;
		super.resume();
		shouldStepOver = true;
	}
	
	/**
	 * Executes a single interpreter step
	 */
	public synchronized void stepInto() {
		shouldStep = true;
		super.resume();
		shouldStepInto = true;
		shouldStepOver = true;
	}
	
	/**
	 * Executes the rest of the engine step
	 */
	public synchronized void stepReturn() {
		shouldStep = true;
		super.resume();
		shouldStepInto = false;
		shouldStepOver = true;
	}
	
	public ASMState[] getStates() {
		ASMState[] states = new ASMState[this.states.size()];
		this.states.toArray(states);
		return states;
	}
	
	/**
	 * Sets the value of the given function.
	 * @param functionName name of the function of which the value should be changed
	 * @param expression a string representation of the value
	 * @return the value that the function actually got after setting
	 */
	public String setValue(String functionName, String expression) {
		String name = functionName.substring(0, functionName.indexOf('('));
		FunctionElement function = capi.getState().getFunction(name);
		for (Location location : function.getLocations(name)) {
			if (location.toString().equals(functionName)) {
				try {
					try {
						function.setValue(location.args, NumberElement.getInstance(Double.parseDouble(expression)));
					} catch (NumberFormatException e) {
						if (BooleanElement.TRUE_NAME.equals(expression))
							function.setValue(location.args, BooleanElement.TRUE);
						else if (BooleanElement.FALSE_NAME.equals(expression))
							function.setValue(location.args, BooleanElement.FALSE);
						else if (expression.startsWith("\"") && expression.endsWith("\""))
							function.setValue(location.args, new StringElement(expression));
						else if (Character.isLetterOrDigit(expression.charAt(0)))
							function.setValue(location.args, new EnumerationElement(expression));
					}
					debugTarget.fireChangeEvent(DebugEvent.CONTENT);
					return function.getValue(location.args).denotation();
				} catch (UnmodifiableFunctionException e) {
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the current updates as a set of ASMUpdate.
	 * @return the current updates as a set of ASMUpdate
	 */
	public Set<ASMUpdate> getUpdates() {
		return ASMUpdate.wrapUpdateSet(capi.getUpdateSet(0));
	}
	
	/**
	 * Returns the current step.
	 * @return the current step
	 */
	public int getStep() {
		return capi.getStepCount();
	}
	
	/**
	 * Returns the context of the given update.
	 * @param update the update of which the context should be returned
	 * @return the context of the given update
	 */
	public String getUpdateContext(Update update) {
		return ((ScannerInfo)update.sources.toArray()[0]).getContext(capi.getParser(), capi.getSpec());
	}
	
	@Override
	protected void preExecutionCallback() {
		debugTarget.fireCreationEvent();
	};
	
	@Override
	public synchronized void updateStatus(EngineDriverStatus status) {
		EngineDriverStatus oldStatus = getStatus();
		super.updateStatus(status);
		if (debugTarget != null) {
			if (status == EngineDriverStatus.stopped)
				debugTarget.fireTerminateEvent();
			else {
				if (oldStatus == EngineDriverStatus.paused && status == EngineDriverStatus.running) {
					if (shouldStepInto)
						debugTarget.fireResumeEvent(DebugEvent.STEP_INTO);
					else if (shouldStepOver)
						debugTarget.fireResumeEvent(DebugEvent.STEP_OVER);
					else
						debugTarget.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
				}
				else if (oldStatus == EngineDriverStatus.running && status == EngineDriverStatus.paused) {
					if (shouldStep) {
						if (shouldStepOver) {
							debugTarget.fireSuspendEvent(DebugEvent.STEP_END);
							shouldStepOver = false;
						}
						else
							debugTarget.fireSuspendEvent(DebugEvent.BREAKPOINT);
					}
					else {
						debugTarget.fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new launch for the given path and configuration.
	 * @param abspathname path of the launch to create
	 * @param config configuration for the launch to create
	 * @throws CoreException when another specification is currently running
	 */
	public static void newLaunch(String abspathname, ILaunchConfiguration config) throws CoreException {
		if (runningInstance == null) {
			runningInstance = new EngineDebugger(false);
			if (config == null)
				runningInstance.setDefaultConfig();
			else
				runningInstance.setConfig(config);
			String specPath = config.getAttribute(ICoreASMConfigConstants.PROJECT, (String)null);
			if (specPath != null)
				specPath += Path.SEPARATOR + config.getAttribute(ICoreASMConfigConstants.SPEC, "");
			((EngineDebugger)runningInstance).specPath = specPath;
			runningInstance.dolaunch(abspathname);
		} else
			throw new CoreException(new Status(Status.WARNING, CoreASMPlugin.PLUGIN_ID, -1, "Another specification is currently running.", null));
	}
	
	/**
	 * Restores the given state
	 * @param stateToDropTo State to drop to
	 */
	public void dropToState(ASMState stateToDropTo) {
		try {
			for (ASMState state = states.peek(); !stateToDropTo.equals(states.peek()); state = states.pop()) {
				if (state.getStep() < 0)
					continue;
				for (Update update : state.getUpdates()) {
					if (stateToDropTo.getFunction(update.loc.name) != null)
						capi.getState().setValue(update.loc, stateToDropTo.getValue(update.loc));
					else {
						// TODO Function/Universe that isn't in the state to drop should be removed, unfortunately getFunctions and getUniverses only return copies
//						capi.getState().getFunctions().remove(update.loc.name);
//						capi.getState().getUniverses().remove(update.loc.name);
					}
				}
			}
			debugTarget.fireChangeEvent(DebugEvent.CONTENT);
		} catch (InvalidLocationException e) {
		}
	}
	
	@Override
	public void update(EngineEvent event) {
		if (event instanceof EngineModeEvent) {
			if (((EngineModeEvent)event).getNewMode() == EngineMode.emStepSucceeded) {
				updates = capi.getUpdateSet(0);
				stepSucceeded = true;
			}
		}
		else
			super.update(event);
	}
	
	@Override
	protected void handleError() {
		// lastError is needed by the ASM Update View, it may not be set to null at this point
		CoreASMError error = lastError;
		super.handleError();
		lastError = error;
	}
	
	/**
	 * Sets the debug target.
	 * @param debugTarget debug target to set
	 */
	public void setDebugTarget(ASMDebugTarget debugTarget) {
		this.debugTarget = debugTarget;
	}
	
	/**
	 * Returns the path of the running specification.
	 * @return the path of the running specification
	 */
	public String getSpecPath() {
		return specPath;
	}
	
	/**
	 * Pauses the execution until the user resumes it.
	 */
	private void onStep() {
		if (shouldStep) {
			if (this == runningInstance)
				updateStatus(EngineDriverStatus.paused);
			
			while (shouldStep && !shouldStepOver) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			
			if (this == runningInstance)
				updateStatus(EngineDriverStatus.running);
		}
	}
	
	/**
	 * This method is called whenever a rule is being evaluated.
	 * @param rule rule that is being evaluated
	 */
	private void onRuleEvaluation(String rule) {
		for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
			try {
				if (!breakpoint.isEnabled())
					continue;
				if (breakpoint instanceof ASMMethodBreakpoint && ((ASMMethodBreakpoint) breakpoint).getRuleName().equals(rule)) {
					try {
						sourceName = ((ASMLineBreakpoint)breakpoint).getSpecName();
						lineNumber = ((ASMLineBreakpoint)breakpoint).getLineNumber();
						onBreakpointHit();
						break;
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (CoreException e) {
			}
		}
	}
	
	/**
	 * This method is called whenever a seq block is being evaluated.
	 * @param seqBlock the node of the seq block that is being evaluated
	 */
	private void onSeqBlockEvaluation(ASTNode seqBlock) {
		if (shouldStepInto) {
			if (shouldStep)
				updateState();
			onStep();
		}
	}
	
	/**
	 * This method is called whenever a breakpoint is hit.
	 */
	private void onBreakpointHit() {
		setStepping(true);
		shouldStepInto = true;
		updateState();
		onStep();
	}
	
	public void updateState() {
		ASMState state = null;
		if (!states.isEmpty() && states.peek().getStep() < 0)
			state = new ASMState(states.peek());
		Set<Element> agent = new HashSet<Element>();
		agent.add(currentAgent);
		if (state == null)
			state = new ASMState(-capi.getStepCount() - 1, agent, capi.getState(), updates, capi.getAgentSet(), sourceName, lineNumber);
		state.updateState(agent, updates, sourceName, lineNumber);
		updates = new HashSet<Update>();
		states.add(state);
	}

	@Override
	public void beforeNodeEvaluation(ASTNode pos) {
		if (prevPos == null || pos != prevPos.getParent()) {
			String context = pos.getContext(capi.getParser(), capi.getSpec());
			String prevSourceName = sourceName;
			int prevLineNumber = lineNumber;
			sourceName = ASMDebugUtils.parseSourceName(context);
			lineNumber = ASMDebugUtils.parseLineNumber(context);
			if (sourceName == null || lineNumber < 0)
				return;
			
			if (stepSucceeded) {
				while (!states.isEmpty() && states.peek().getStep() < 0)
					states.pop();
				states.add(new ASMState(capi.getStepCount(), capi.getLastSelectedAgents(), capi.getState(), updates, capi.getAgentSet(), sourceName, lineNumber));
				shouldStepInto = false;
				onStep();
				stepSucceeded = false;
			}
			else if (!sourceName.equals(prevSourceName) || lineNumber != prevLineNumber) {
				for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
					try {
						if (!breakpoint.isEnabled())
							continue;
						if (breakpoint instanceof ASMLineBreakpoint && ((ASMLineBreakpoint) breakpoint).getSpecName().equals(sourceName) && ((ASMLineBreakpoint) breakpoint).getLineNumber() == lineNumber) {
							onBreakpointHit();
							break;
						}
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			if (pos instanceof FunctionRuleTermNode) {
				ASTNode parent = pos.getParent();
				if (!(parent instanceof UpdateRuleNode) || pos != parent.getFirst()) {
					FunctionRuleTermNode frNode = (FunctionRuleTermNode) pos;
					if (frNode.hasName()) {
						for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
							try {
								if (!breakpoint.isEnabled())
									continue;
								if (breakpoint instanceof ASMWatchpoint && ((ASMWatchpoint)breakpoint).isAccess() && ((ASMWatchpoint)breakpoint).getFuctionName().equals(frNode.getName())) {
									onBreakpointHit();
									accessBreakpointHit = true;
									break;
								}
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			if (pos instanceof MacroCallRuleNode && pos.getFirst() instanceof FunctionRuleTermNode) {
				FunctionRuleTermNode frNode = (FunctionRuleTermNode)pos.getFirst();
				if (frNode.hasName())
					onRuleEvaluation(frNode.getName());
			}
			else if (pos != null && !(pos instanceof SeqBlockRuleNode) && pos.getParent() instanceof SeqBlockRuleNode)
				onSeqBlockEvaluation(pos);
		}
		prevPos = pos;
	}

	@Override
	public void afterNodeEvaluation(ASTNode pos) {
		if (!pos.getUpdates().isEmpty()) {
			if (!accessBreakpointHit) {
				String context = pos.getContext(capi.getParser(), capi.getSpec());
				String prevSourceName = sourceName;
				int prevLineNumber = lineNumber;
				sourceName = ASMDebugUtils.parseSourceName(context);
				lineNumber = ASMDebugUtils.parseLineNumber(context);
				if (sourceName == null || lineNumber < 0 || !sourceName.equals(prevSourceName) || lineNumber != prevLineNumber)
					return;
				
				boolean breakpointHit = false;
				for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
					try {
						if (!breakpoint.isEnabled())
							continue;
						for (Update update : pos.getUpdates()) {
							if (breakpoint instanceof ASMWatchpoint && ((ASMWatchpoint)breakpoint).isModification() && ((ASMWatchpoint)breakpoint).getFuctionName().equals(update.loc.name)) {
								breakpointHit = true;
								break;
							}
						}
						if (breakpointHit) {
							onBreakpointHit();
							break;
						}
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				accessBreakpointHit = false;
			}
		}
		updates.addAll(pos.getUpdates());
	}

	@Override
	public void initProgramExecution(Element agent, Element program) {
		currentAgent = agent;
		onRuleEvaluation(program.denotation().substring(1));
	}
}
