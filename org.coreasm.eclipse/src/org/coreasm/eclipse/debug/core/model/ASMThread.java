package org.coreasm.eclipse.debug.core.model;

import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

/**
 * This class is needed by the eclipse debug framework. Incoming calls are redirected to the debug target.
 * @author Michael Stegmaier
 *
 */
public class ASMThread extends ASMDebugElement implements IThread {
	private IStackFrame[] stackFrames;

	public ASMThread(ASMDebugTarget debugTarget) {
		super(debugTarget);
	}

	@Override
	public boolean canResume() {
		return getDebugTarget().canResume();
	}

	@Override
	public boolean canSuspend() {
		return getDebugTarget().canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return getDebugTarget().isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		getDebugTarget().resume();
	}

	@Override
	public void suspend() throws DebugException {
		getDebugTarget().suspend();
	}

	@Override
	public boolean canStepInto() {
		return isSuspended();
	}

	@Override
	public boolean canStepOver() {
		return isSuspended();
	}

	@Override
	public boolean canStepReturn() {
		return isSuspended();
	}

	@Override
	public boolean isStepping() {
		if (EngineDebugger.getRunningInstance()==null)
			return false;
		return EngineDebugger.getRunningInstance().isStepping();
	}

	@Override
	public void stepInto() throws DebugException {
		EngineDebugger.getRunningInstance().stepInto();
	}

	@Override
	public void stepOver() throws DebugException {
		EngineDebugger.getRunningInstance().stepOver();
	}

	@Override
	public void stepReturn() throws DebugException {
		EngineDebugger.getRunningInstance().stepReturn();
	}

	@Override
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

	@Override
	public IStackFrame[] getStackFrames() throws DebugException {
		if (isSuspended()) {
			ASMStorage[] states = EngineDebugger.getRunningInstance().getStates();
			IStackFrame[] frames = new IStackFrame[states.length];
			
			if (stackFrames == null) {
				for (int i = 0; i < frames.length; i++)
					frames[frames.length - i - 1] = new ASMStackFrame(this, states[i]);
			}
			else {
				// if there are at least as many states has we have stackframes in cache, just copy all stackframes from cache
				if (states.length >= stackFrames.length) {
					for (int i = 0; i < stackFrames.length; i++)
						frames[i + states.length - stackFrames.length] = stackFrames[i];
				}
				else {
					for (int i = 0; i < frames.length; i++)
						frames[i] = stackFrames[i + stackFrames.length - states.length];
				}
				frames[0] = new ASMStackFrame(this, states[states.length - 1]);
			}
			stackFrames = frames;
			
			return frames;
		}
		return new IStackFrame[0];
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
		return getTopStackFrame() != null;
	}

	@Override
	public int getPriority() throws DebugException {
		return 0;
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		IStackFrame[] frames = getStackFrames();
		if (frames.length > 0)
			return frames[0];
		return null;
	}

	@Override
	public String getName() throws DebugException {
		return "Thread [main]";
	}
	
	@Override
	public IBreakpoint[] getBreakpoints() {
		// TODO Auto-generated method stub
		return null;
	}
}
