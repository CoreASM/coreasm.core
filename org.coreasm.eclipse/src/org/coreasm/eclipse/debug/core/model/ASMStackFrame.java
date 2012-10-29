package org.coreasm.eclipse.debug.core.model;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
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
	private ASMThread thread;
	private IVariable[] variables;
	private ASMState state;

	public ASMStackFrame(ASMThread thread, ASMState state) {
		super((ASMDebugTarget) thread.getDebugTarget());
		this.thread = thread;
		this.state = state;
		Set<Update> updates = state.getUpdates();
		ArrayList<IVariable> variables = new ArrayList<IVariable>();
		ArrayList<IVariable> universeVariables = new ArrayList<IVariable>();
		ArrayList<IVariable> backgrounds = new ArrayList<IVariable>();
		
		variables.add(new ASMVariable(this, "Step", new ASMValue(this, "" + (getStep() < 0 ? -getStep() - 1 + "*" : getStep())), false));
		variables.add(new ASMVariable(this, "Last Selected Agents", new ASMValue(this, getLastSelectedAgents().toString()), false));
		variables.add(new ASMVariable(this, "Callstack", new ASMValue(this, state.getCallStack().toString()), false));
		
		for (Entry<String, ASMFunctionElement> function : state.getFunctions().entrySet()) {
			String functionName = function.getKey();
			ASMFunctionElement functionElement = function.getValue();
			
			if (functionElement.isModifiable() && !AbstractStorage.FUNCTION_ELEMENT_FUNCTION_NAME.equals(functionName) && !AbstractStorage.RULE_ELEMENT_FUNCTION_NAME.equals(functionName)) {
				for (Location location : functionElement.getLocations(functionName)) {
					boolean valueChanged = false;
					for (Update update : updates) {
						if (update.loc.equals(location)) {
							valueChanged = true;
							break;
						}
					}
					if (AbstractStorage.UNIVERSE_ELEMENT_FUNCTION_NAME.equals(functionName))
						backgrounds.add(new ASMVariable(this, location.toString(), new ASMValue(this, "true"), valueChanged));
					else
						variables.add(new ASMVariable(this, location.toString(), new ASMValue(this, functionElement.getValue(location.args).denotation()), valueChanged));
				}
			}
		}
		for (Entry<String, ASMFunctionElement> universe : state.getUniverses().entrySet()) {
			if (universe.getValue().getFunctionElement() instanceof UniverseElement) {
				String universeName = universe.getKey();
				UniverseElement universeElement = (UniverseElement)universe.getValue().getFunctionElement();
				
				for (Location location : universeElement.getLocations(universeName)) {
					boolean valueChanged = false;
					for (Update update : updates) {
						if (update.loc.equals(location)) {
							valueChanged = true;
							break;
						}
					}
					universeVariables.add(new ASMVariable(this, location.toString(), new ASMValue(this, universeElement.getValue(location.args).denotation()), valueChanged));
				}
				IVariable[] tmp = new IVariable[universeVariables.size()];
				universeVariables.toArray(tmp);
				variables.add(new ASMVariable(this, universeName, new ASMValue(this, tmp), false));
			}
		}
		IVariable[] tmp = new IVariable[backgrounds.size()];
		backgrounds.toArray(tmp);
		variables.add(new ASMVariable(this, "Backgrounds", new ASMValue(this, tmp), false));
		this.variables = new IVariable[variables.size()];
		variables.toArray(this.variables);
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
			// TODO Auto-generated catch block
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
			EngineDebugger.getRunningInstance().dropToState(state);
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
		return variables;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return variables.length > 0;
	}

	@Override
	public int getLineNumber() throws DebugException {
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
	public boolean equals(Object obj) {
		if (obj instanceof ASMStackFrame) {
			ASMStackFrame stackFrame = (ASMStackFrame) obj;
			try {
				if (stackFrame.state.getStep() == this.state.getStep() && getLineNumber() == stackFrame.getLineNumber() && getLastSelectedAgents().equals(stackFrame.getLastSelectedAgents()) && getUpdates().equals(stackFrame.getUpdates()))
					return true;
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (Exception e){
				e.printStackTrace();
			}
//			try {
//				return getLineNumber() == stackFrame.getLineNumber() && getSourceName().equals(stackFrame.getSourceName());
//			} catch (DebugException e) {
//			}
		}
		return false;
	}	
	
	/**
	 * Returns the step of the state assigned to this stack frame.
	 * @return the step of the state assigned to this stack frame
	 */
	public int getStep() {
		return state.getStep();
	}
	
	/**
	 * Returns the last selected agents of the state assigned to this stack frame.
	 * @return the last selected agents of the state assigned to this stack frame
	 */
	public String getLastSelectedAgents() {
		return state.getLastSelectedAgents().toString();
	}
	
	/**
	 * Returns the name of the current rule of the state assigned to this stack frame.
	 * @return the name of the current rule of the state assigned to this stack frame
	 */
	public String getRuleName() {
		if (state.getCallStack().isEmpty())
			return "";
		return state.getCallStack().peek().toString();
	}
	
	/**
	 * Returns the name of the source file of the state assigned to this stack frame.
	 * @return the name of the source file of the state assigned to this stack frame
	 */
	public String getSourceName() {
		return state.getSourceName();
	}
	
	/**
	 * Returns the updates of the state assigned to this stack frame.
	 * @return the updates of the state assigned to this stack frame
	 */
	public Set<Update> getUpdates() {
		return state.getUpdates();
	}
	
	/**
	 * Returns the agents of the state assigned to this stack frame.
	 * @return the agents of the state assigned to this stack frame
	 */
	public Set<? extends Element> getAgents() {
		return state.getAgents();
	}
}
