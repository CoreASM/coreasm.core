package org.coreasm.eclipse.debug.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.coreasm.eclipse.debug.ui.views.ASMUpdate;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDropToFrame;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * This class contains the variables of the current state
 * @author Michael Stegmaier
 *
 */
public class ASMStackFrame extends ASMDebugElement implements IStackFrame, IDropToFrame {
	private final ASMThread thread;
	private final int id;
	
	private String sourceName = "";
	private final int step;

	public ASMStackFrame(ASMThread thread, int id) {
		super((ASMDebugTarget) thread.getDebugTarget());
		this.thread = thread;
		this.id = id;
		step = getStep(getState());
		// Cache the initial source name
		getSourceName();
	}
	
	private static final int getStep(ASMStorage state) {
		if (state == null)
			return -1;
		return state.getStep();
	}

	@Override
	public boolean canStepInto() {
		return getThread().canStepInto();
	}

	@Override
	public boolean canStepOver() {
		return getThread().canStepOver();
	}

	@Override
	public boolean canStepReturn() {
		return getThread().canStepReturn();
	}
	
	@Override
	public boolean canDropToFrame() {
		try {
			return isSuspended() && !this.equals(getThread().getTopStackFrame());
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isStepping() {
		return getThread().isStepping();
	}

	@Override
	public void stepInto() throws DebugException {
		getThread().stepInto();
	}

	@Override
	public void stepOver() throws DebugException {
		getThread().stepOver();
	}

	@Override
	public void stepReturn() throws DebugException {
		getThread().stepReturn();
	}
	
	@Override
	public void dropToFrame() throws DebugException {
		if (EngineDebugger.getRunningInstance() != null)
			EngineDebugger.getRunningInstance().dropToState(getState());
	}

	@Override
	public boolean canResume() {
		return getThread().canResume();
	}

	@Override
	public boolean canSuspend() {
		return getThread().canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return getThread().isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		getThread().resume();
	}

	@Override
	public void suspend() throws DebugException {
		getThread().suspend();
	}

	@Override
	public boolean canTerminate() {
		return getThread().canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return getThread().isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		getThread().terminate();
	}

	@Override
	public IThread getThread() {
		return thread;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		ASMStorage state = getState();
		if (thread.getVariables(state) == null) {
			Set<Location> updateLocations = new HashSet<Location>();
			ArrayList<IVariable> variables = new ArrayList<IVariable>();
			ArrayList<IVariable> backgrounds = new ArrayList<IVariable>();
			
			for (ASMUpdate update : state.getUpdates())
				updateLocations.add(update.getLocation());
			
			variables.add(new ASMVariable(this, "Step", new ASMValue(this, "" + (getStep() < 0 ? -getStep() - 1 + "*" : getStep())), false));
			variables.add(new ASMVariable(this, "Last Selected Agents", new ASMValue(this, getLastSelectedAgents().toString()), false));
			variables.add(new ASMVariable(this, "Callstack", new ASMValue(this, state.getCallStack().toString()), false));
			
			for (Entry<String, Element> envVariable : state.getEnvVars().entrySet())
				variables.add(new ASMVariable(this, envVariable.getKey(), new ASMValue(this, envVariable.getValue()), false));
			
			for (Entry<String, FunctionElement> function : state.getFunctions().entrySet()) {
				String functionName = function.getKey();
				FunctionElement functionElement = function.getValue();
				
				if (functionElement.isReadable() &&
						!AbstractStorage.FUNCTION_ELEMENT_FUNCTION_NAME.equals(functionName) &&
						!AbstractStorage.RULE_ELEMENT_FUNCTION_NAME.equals(functionName)) {
					for (Location location : functionElement.getLocations(functionName)) {
						if (AbstractStorage.UNIVERSE_ELEMENT_FUNCTION_NAME.equals(functionName))
							backgrounds.add(new ASMVariable(this, location.toString(), functionElement, new ASMValue(this, BooleanElement.TRUE), updateLocations.contains(location)));
						else
							variables.add(new ASMVariable(this, location.toString(), functionElement, new ASMValue(this, functionElement.getValue(location.args)), updateLocations.contains(location)));
					}
				}
			}
			
			for (Entry<String, AbstractUniverse> universe : state.getUniverses().entrySet()) {
				if (universe.getValue().isModifiable()) {
					ArrayList<IVariable> universeVariables = new ArrayList<IVariable>();
					String universeName = universe.getKey();
					FunctionElement universeElement = universe.getValue();
					
					boolean containingValueChanged = false;
					for (Location location : universeElement.getLocations(universeName)) {
						if (updateLocations.contains(location))
							containingValueChanged = true;
						universeVariables.add(new ASMVariable(this, location.toString(), universeElement, new ASMValue(this, universeElement.getValue(location.args)), updateLocations.contains(location)));
					}
					variables.add(new ASMVariable(this, universeName, universeElement, new ASMValue(this, universeVariables.toArray(new IVariable[universeVariables.size()])), containingValueChanged));
				}
			}
			variables.add(new ASMVariable(this, "Backgrounds", new ASMValue(this, backgrounds.toArray(new IVariable[backgrounds.size()])), false));
			thread.setVariables(state, variables.toArray(new IVariable[variables.size()]));
		}
		return thread.getVariables(state);
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return getVariables() != null && getVariables().length > 0;
	}

	@Override
	public int getLineNumber() throws DebugException {
		ASMStorage state = getState();
		if (state == null)
			return -1;
		return state.getLineNumber();
	}

	@Override
	public int getCharStart() throws DebugException {
		return -1;
	}

	@Override
	public int getCharEnd() throws DebugException {
		return -1;
	}

	@Override
	public String getName() throws DebugException {
		return getSourceName();
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return null;
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASMStackFrame other = (ASMStackFrame) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * Returns the state of the state assigned to this stack frame.
	 * @return the state of the state assigned to this stack frame
	 */
	public ASMStorage getState() {
		return thread.getState(this);
	}

	/**
	 * Returns the step of the state assigned to this stack frame.
	 * @return the step of the state assigned to this stack frame
	 */
	public int getStep() {
		return step;
	}
	
	/**
	 * Returns the last selected agents of the state assigned to this stack frame.
	 * @return the last selected agents of the state assigned to this stack frame
	 */
	public String getLastSelectedAgents() {
		ASMStorage state = getState();
		if (state == null)
			return "";
		return state.getLastSelectedAgents().toString();
	}
	
	/**
	 * Returns the name of the current rule of the state assigned to this stack frame.
	 * @return the name of the current rule of the state assigned to this stack frame
	 */
	public String getRuleName() {
		ASMStorage state = getState();
		if (state == null || state.getCallStack().isEmpty())
			return "";
		return state.getCallStack().peek().toString();
	}
	
	/**
	 * Returns the name of the source file of the state assigned to this stack frame.
	 * @return the name of the source file of the state assigned to this stack frame
	 */
	public String getSourceName() {
		ASMStorage state = getState();
		if (state == null)
			return sourceName;
		return sourceName = state.getSourceName();
	}
	
	/**
	 * Returns the updates of the state assigned to this stack frame.
	 * @return the updates of the state assigned to this stack frame
	 */
	public Set<ASMUpdate> getUpdates() {
		ASMStorage state = getState();
		if (state == null)
			return Collections.emptySet();
		return state.getUpdates();
	}
	
	/**
	 * Returns the agents of the state assigned to this stack frame.
	 * @return the agents of the state assigned to this stack frame
	 */
	public Set<? extends Element> getAgents() {
		ASMStorage state = getState();
		if (state == null)
			return Collections.emptySet();
		return state.getAgents();
	}
}
