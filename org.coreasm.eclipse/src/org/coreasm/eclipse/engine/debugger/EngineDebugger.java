package org.coreasm.eclipse.engine.debugger;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;
import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.debug.core.model.ASMLineBreakpoint;
import org.coreasm.eclipse.debug.core.model.ASMMethodBreakpoint;
import org.coreasm.eclipse.debug.core.model.ASMStorage;
import org.coreasm.eclipse.debug.core.model.ASMWatchpoint;
import org.coreasm.eclipse.debug.ui.views.ASMUpdate;
import org.coreasm.eclipse.debug.util.ASMDebugUtils;
import org.coreasm.eclipse.engine.driver.EngineDriver;
import org.coreasm.eclipse.launch.ICoreASMConfigConstants;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.EngineErrorEvent;
import org.coreasm.engine.EngineEvent;
import org.coreasm.engine.EngineModeEvent;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Interpreter.CallStackElement;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.InterpreterListener;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.UpdateRuleNode;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugins.number.NumberElement;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.string.StringElement;
import org.coreasm.engine.plugins.turboasm.SeqRuleNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.swt.widgets.Display;

/**
 * This is the main class of the the debugger. It controls the engine and provides access to it's state.
 * @author Michael Stegmaier
 *
 */
public class EngineDebugger extends EngineDriver implements InterpreterListener {
	
	private ControlAPI capi = (ControlAPI)engine;
	private WatchExpressionAPI wapi;
	private Stack<ASMStorage> states = new Stack<ASMStorage>();
	private ASMDebugTarget debugTarget;
	private String sourceName;
	private String specPath;
	private Element currentAgent;
	private int lineNumber;
	private ASMStorage stateToDropTo;
	private ASTNode stepOverPos;
	private ASTNode stepReturnPos;
	private ASTNode prevPos;
	private Stack<Map<ASTNode, String>> ruleArgs = new Stack<Map<ASTNode, String>>();
	private Set<ASMUpdate> updates = new HashSet<ASMUpdate>();
	private IBreakpoint prevWatchpoint;
	private boolean stepSucceeded = false;
	private volatile boolean shouldStep = false;
	private volatile boolean shouldStepReturn = false;
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
	public synchronized void pause() {
		updateState(null);
		super.pause();
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
	}
	
	/**
	 * Executes the rest of the engine step
	 */
	public synchronized void stepReturn() {
		shouldStep = true;
		super.resume();
		shouldStepReturn = true;
		stepReturnPos = stepOverPos;
		while (stepReturnPos != null && stepReturnPos.getParent() instanceof SeqRuleNode) {
			stepReturnPos = stepReturnPos.getParent();
			// If the first CSTNode is not an ASTNode this is the top of the seqblock, breaking here avoids stepping out of more than one seqblock at a time
			if (stepReturnPos instanceof SeqRuleNode && !(stepReturnPos.getFirstCSTNode() instanceof ASTNode))
				break;
		}
	}
	
	/**
	 * Returns the stack of states.
	 * @return the stack of states
	 */
	public ASMStorage[] getStates() {
		ASMStorage[] states = new ASMStorage[this.states.size()];
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
	
	public Element evaluateExpression(String expression, ASMStorage storage) throws ParserException, InterpreterException {
		ParserTools parserTools = ParserTools.getInstance(capi);
		Parser<Node> termParser = ((ParserPlugin)capi.getPlugin("Kernel")).getParser("Term");
		Parser<Node> parser = termParser.from(parserTools.getTokenizer(), parserTools.getIgnored());
		
		return wapi.evaluateExpression((ASTNode)parser.parse(expression), currentAgent, storage);
	}
	
	public boolean isStepFailed() {
		return capi.getStorage().getLastInconsistentUpdate() != null;
	}
	
	public boolean isUpdateConsistent(ASMUpdate update) {
		Set<Update> lastInconsistentUpdate = capi.getStorage().getLastInconsistentUpdate();
		return lastInconsistentUpdate == null || !lastInconsistentUpdate.contains(update.getUpdate());
	}
	
	public Set<ASMUpdate> getLastInconsistentUpdate() {
		return ASMUpdate.wrapUpdateSet(capi.getStorage().getLastInconsistentUpdate(), false, capi);
	}
	
	/**
	 * Returns the current updates as a set of ASMUpdate.
	 * @return the current updates as a set of ASMUpdate
	 */
	public Set<ASMUpdate> getUpdates() {
		return ASMUpdate.wrapUpdateSet(capi);
	}
	
	/**
	 * Returns the current step.
	 * @return the current step
	 */
	public int getStep() {
		return capi.getStepCount();
	}
	
	@Override
	protected void preExecutionCallback() {
		debugTarget.fireCreationEvent();
		wapi = new WatchExpressionAPI(capi);
	};
	
	@Override
	protected void postExecutionCallback() {
		cleanUp();
	}
	
	private void cleanUp() {
		for (ASMStorage storage : states)
			storage.clearState();
		debugTarget.fireTerminateEvent();
		debugTarget.cleanUp();
		states.clear();
		updates.clear();
		ruleArgs.clear();
		wapi.dispose();
		capi = null;
		prevPos = null;
		stepOverPos = null;
		stepReturnPos = null;
		stateToDropTo = null;
		System.gc();
	}
	
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
					else if (shouldStepReturn)
						debugTarget.fireResumeEvent(DebugEvent.STEP_RETURN);
					else
						debugTarget.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
				}
				else if (oldStatus == EngineDriverStatus.running && status == EngineDriverStatus.paused) {
					if (shouldStep) {
						if (shouldStepInto || shouldStepOver || shouldStepReturn)
							debugTarget.fireSuspendEvent(DebugEvent.STEP_END);
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
			runningInstance.setConfig(config);
			String specPath = config.getAttribute(ICoreASMConfigConstants.PROJECT, (String)null);
			if (specPath != null)
				specPath += IPath.SEPARATOR + config.getAttribute(ICoreASMConfigConstants.SPEC, "");
			((EngineDebugger)runningInstance).specPath = specPath;
			runningInstance.dolaunch(abspathname);
		} else
			throw new CoreException(new Status(Status.WARNING, CoreASMPlugin.PLUGIN_ID, -1, "Another specification is currently running.", null));
	}
	
	/**
	 * Restores the given state
	 * @param stateToDropTo State to drop to
	 */
	public void dropToState(ASMStorage stateToDropTo) {
		this.stateToDropTo = stateToDropTo;
	}
	
	private void dropToState() {
		try {
			for (ASMStorage state = states.peek(); !stateToDropTo.equals(states.peek()); state = states.pop()) {
				if (state.getStep() < 0)
					capi.getInterpreter().getInterpreterInstance().clearTree(state.getPosition());
				else {
					for (ASMUpdate update : state.getUpdates()) {
						if (stateToDropTo.getFunction(update.getLocation().name) != null)
							capi.getState().setValue(update.getLocation(), stateToDropTo.getValue(update.getLocation()));
						else
							capi.getState().setValue(update.getLocation(), Element.UNDEF);
					}
				}
				state.clearState();
			}
			ASTNode pos = stateToDropTo.getPosition();
			if (pos == null) {
				pos = capi.getInterpreter().getInterpreterInstance().getPosition();
				while (pos != null && pos.getParent() != null)
					pos = pos.getParent();
			}
			if (pos != null) {
				capi.getInterpreter().getInterpreterInstance().clearTree(pos);
				capi.getInterpreter().getInterpreterInstance().setPosition(pos);
			}
			debugTarget.fireChangeEvent(DebugEvent.CONTENT);
			this.stateToDropTo = null;
		} catch (InvalidLocationException e) {
		}
	}
	
	@Override
	public void update(EngineEvent event) {
		super.update(event);
		if (event instanceof EngineModeEvent) {
			if (((EngineModeEvent)event).getNewMode() == EngineMode.emStepSucceeded) {
				updates = ASMUpdate.wrapUpdateSet(capi);
				shouldStepOver = false;
				shouldStepReturn = false;
				stepSucceeded = true;
			}
			else if (((EngineModeEvent)event).getNewMode() == EngineMode.emStepFailed)
				onBreakpointHit(null);
		}
		else if (event instanceof EngineErrorEvent) {
			EngineErrorEvent errorEvent = (EngineErrorEvent)event;
			onBreakpointHit((ASTNode)errorEvent.getError().node);
		}
	}
	
	@Override
	protected void handleError() {
		// lastError is needed by the ASM Update View, it must not be set to null at this point
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
	 * @param pos the current node
	 */
	private void waitForStep(ASTNode pos) {
		if (shouldStep) {
			stepOverPos = pos;
			
			if (this == runningInstance)
				updateStatus(EngineDriverStatus.paused);
			
			while (shouldStep && !shouldStepOver && !shouldStepInto && !shouldStepReturn) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				if (stateToDropTo != null)
					dropToState();
			}
			
			if (this == runningInstance)
				updateStatus(EngineDriverStatus.running);
			shouldStepInto = false;
		}
	}
	
	/**
	 * This method is called whenever a breakpoint is hit.
	 * @param pos the node that the breakpoint was attached to
	 */
	private void onBreakpointHit(ASTNode pos) {
		if (currentAgent != null) {
			setStepping(true);
			shouldStepInto = false;
			shouldStepOver = false;
			shouldStepReturn = false;
			updateState(pos);
			waitForStep(pos);
		}
	}
	
	/**
	 * Updates/Creates the current state.
	 * @param pos Current position of the state
	 */
	public void updateState(ASTNode pos) {
		ASMStorage state = null;
		Stack<CallStackElement> callStack = capi.getInterpreter().getInterpreterInstance().getCurrentCallStack();
		Map<String, Element> envVars = capi.getInterpreter().getInterpreterInstance().getEnvVars();
		if (stepSucceeded) {
			if (!states.isEmpty() && capi.getStepCount() == states.peek().getStep())
				return;
			while (!states.isEmpty() && states.peek().getStep() < 0)
				states.pop().clearState();
			state = new ASMStorage(wapi, capi.getStorage(), capi.getStepCount(), capi.getLastSelectedAgents(), envVars, updates, capi.getAgentSet(), callStack, sourceName, lineNumber);
		}
		else {
			if (!states.isEmpty() && states.peek().getStep() < 0) {
				if (updates.isEmpty() && states.peek().getUpdates().isEmpty() && envVars.equals(states.peek().getEnvVars()))
					state = states.pop();
				else
					state = new ASMStorage(states.peek());
			}
			Set<Element> agent = new HashSet<Element>();
			agent.add(currentAgent);
			if (state == null)
				state = new ASMStorage(wapi, capi.getStorage(), -capi.getStepCount() - 1, agent, envVars, updates, capi.getAgentSet(), callStack, sourceName, lineNumber);
			state.updateState(pos, agent, envVars, updates, callStack, sourceName, lineNumber);
		}
		if (!ruleArgs.isEmpty()) {
			Map<ASTNode, String> ruleArgs = this.ruleArgs.peek();
			if (ruleArgs != null) {
				final ASMStorage[] statePointer = new ASMStorage[] { state };
				for (Entry<ASTNode, String> arg : ruleArgs.entrySet()) {
					final ASTNode node = (ASTNode)arg.getKey().cloneTree();
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							try {
								wapi.evaluateExpression(node, currentAgent, statePointer[0]);
							} catch (InterpreterException e) {
							}
						}
					});
					if (node.isEvaluated())
						envVars.put(arg.getValue(), node.getValue());
				}
			}
		}
		states.add(state);
		updates = new HashSet<ASMUpdate>();
	}
	
	/**
	 * Returns whether pos has been visited before or not.
	 * @param pos ASTNode to test
	 * @return whether pos has been visited before or not
	 */
	private boolean isUnvisited(ASTNode pos) {
		if (prevPos != null && pos == prevPos.getParent() && prevPos.isEvaluated()) {
			prevPos = pos;
			return false;
		}
		prevPos = pos;
		for (ASTNode child : pos.getAbstractChildNodes()) {
			if (child.isEvaluated())
				return false;
		}
		return true;
	}
	
	/**
	 * Returns whether frNode hits a breakpoint.
	 * @param frNode ASTNode to test
	 * @return whether frNode hits a breakpoint
	 */
	private boolean isWatchpointHit(FunctionRuleTermNode frNode) {
		if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled())
			return false;
		ASTNode parent = frNode.getParent();
		if (!(parent instanceof UpdateRuleNode) || frNode != parent.getFirst()) {
			for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
				try {
					if (!breakpoint.isEnabled())
						continue;
					if (breakpoint instanceof ASMWatchpoint && ((ASMWatchpoint)breakpoint).isAccess() && ((ASMWatchpoint)breakpoint).getFuctionName().equals(frNode.getName())) {
						prevWatchpoint = breakpoint;
						return true;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	private boolean isLineBreakpointHit() {
		if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled())
			return false;
		for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
			try {
				if (!breakpoint.isEnabled() || breakpoint instanceof ASMMethodBreakpoint)
					continue;
				if (breakpoint instanceof ASMLineBreakpoint && new Path(((ASMLineBreakpoint) breakpoint).getSpecName()).equals(new Path(sourceName)) && ((ASMLineBreakpoint) breakpoint).getLineNumber() == lineNumber)
					return true;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void beforeNodeEvaluation(ASTNode pos) {
		if (isUnvisited(pos)) {
			sourceName = ASMDebugUtils.getFileName(pos, capi);
			lineNumber = ASMDebugUtils.getLineNumber(pos, capi);
			if (sourceName == null || lineNumber < 0)
				return;
			
			if (stepSucceeded) {
				updateState(pos);
				if (isLineBreakpointHit())
					onBreakpointHit(pos);
				else
					waitForStep(pos);
				stepSucceeded = false;
				return;
			}
			
//			handle watchpoints (access)
			if (Kernel.GR_FUNCTION_RULE_TERM.equals(pos.getGrammarRule())) {
				FunctionRuleTermNode frNode = (FunctionRuleTermNode) pos;
				if (frNode.hasName()) {
					if (isWatchpointHit(frNode)) {
						onBreakpointHit(frNode);
						return;
					}
				}
			}
			
//			handle line breakpoints
			if (ASTNode.RULE_CLASS.equals(pos.getGrammarClass())) {
				if  (!(pos.getParent() instanceof SeqRuleNode) || !(pos instanceof SeqRuleNode)) {	// if parent is SeqRuleNode -> pos may not be SeqRuleNode (this avoids breaking twice on seqblock children)
					if (isLineBreakpointHit()) {
						onBreakpointHit(pos);
						return;
					}
				}
				
//				handle stepping on seq rules
				if (pos.getParent() instanceof SeqRuleNode && (!(pos instanceof SeqRuleNode) || pos.getFirstASTNode() != pos.getFirstCSTNode()) // if parent is SeqRuleNode AND pos is SeqRuleNode -> pos must be keyword 'seq'
				|| pos instanceof SeqRuleNode && pos.getFirstASTNode() != pos.getFirstCSTNode()) { // OR if pos is keyword 'seq' (first CSTNode will be the keyword)
					if (!shouldStepOver && !shouldStepReturn) {
						if (shouldStep)
							updateState(pos);
						waitForStep(pos);
					}
				}
			}
		}
	}

	@Override
	public void afterNodeEvaluation(ASTNode pos) {
		if (pos == stepReturnPos) {
			shouldStepReturn = false;
			stepReturnPos = null;
		}
		else if (pos == stepOverPos) {
			shouldStepOver = false;
			stepOverPos = null;
		}
		if (!pos.getUpdates().isEmpty()) {
			sourceName = ASMDebugUtils.getFileName(pos, capi);
			lineNumber = ASMDebugUtils.getLineNumber(pos, capi);
			if (sourceName == null || lineNumber < 0)
				return;
			
//			handle watchpoints (modification)
			boolean breakpointHit = false;
			if (DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
				for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
					try {
						if (!breakpoint.isEnabled() || breakpoint == prevWatchpoint)
							continue;
						for (Update update : pos.getUpdates()) {
							String updateSourceName = ASMDebugUtils.getFileName(update, capi);
							int updateLineNumber = ASMDebugUtils.getLineNumber(update, capi);
							
							if (!sourceName.equals(updateSourceName) || lineNumber != updateLineNumber)
								continue;
							
							if (breakpoint instanceof ASMWatchpoint && ((ASMWatchpoint)breakpoint).isModification() && ((ASMWatchpoint)breakpoint).getFuctionName().equals(update.loc.name)) {
								breakpointHit = true;
								break;
							}
						}
						if (breakpointHit) {
							onBreakpointHit(pos);
							break;
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
			prevWatchpoint = null;
			updates.addAll(ASMUpdate.wrapUpdateSet(pos.getUpdates().toSet(), true, capi));
		}
	}

	@Override
	public void onRuleCall(RuleElement rule, List<ASTNode> args, ASTNode pos, Element agent) {
		currentAgent = agent;
		if (DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
			for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
				try {
					if (!breakpoint.isEnabled())
						continue;
					if (breakpoint instanceof ASMMethodBreakpoint && ((ASMMethodBreakpoint) breakpoint).getRuleName().equals(rule.getName())) {
						try {
							String ruleSourceName = ASMDebugUtils.getFileName(pos, capi);
							
							if (!ruleSourceName.equals(((ASMLineBreakpoint)breakpoint).getSpecName()))
								continue;
							
							sourceName = ruleSourceName;
							lineNumber = ASMDebugUtils.getLineNumber(pos, capi);
							if (sourceName == null || lineNumber < 0) {
								sourceName = ((ASMLineBreakpoint)breakpoint).getSpecName();
								lineNumber = ((ASMLineBreakpoint)breakpoint).getLineNumber();
							}
							onBreakpointHit(pos);
							break;
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
				} catch (CoreException e) {
				}
			}
		}
		Map<ASTNode, String> ruleArgs = new IdentityHashMap<ASTNode, String>();
		this.ruleArgs.push(ruleArgs);
		if (rule.getParam() != null) {
			int i = 0;
			for (String param : rule.getParam())
				ruleArgs.put(args.get(i++), param);
		}
	}
	
	@Override
	public void onRuleExit(RuleElement rule, List<ASTNode> args, ASTNode pos, Element agent) {
		currentAgent = agent;
		if (!ruleArgs.isEmpty())
			ruleArgs.pop();
	}
	
	@Override
	public void initProgramExecution(Element agent, RuleElement program) {
		onRuleCall(program, null, program.getDeclarationNode(), agent);
	}
}
