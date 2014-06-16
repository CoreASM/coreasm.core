package org.coreasm.eclipse.debug.core.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.eclipse.engine.driver.EngineDriver.EngineDriverStatus;
import org.coreasm.eclipse.launch.ICoreASMConfigConstants;
import org.coreasm.engine.CoreASMError;

/**
 * Implementation of the ASM debug target
 * @see IDebugTarget
 * @author Michael Stegmaier
 *
 */
public class ASMDebugTarget extends ASMDebugElement implements IDebugTarget {
	private ILaunch launch;
	private EngineDebugger debugger;
	private String name;
	private IThread[] threads;

	public ASMDebugTarget(ILaunch launch, EngineDebugger debugger) {
		super(null);
		this.launch = launch;
		this.debugger = debugger;
		IThread thread = new ASMThread(this);
		threads = new IThread[] { thread };
//		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		debugger.setDebugTarget(this);
	}
	
	public boolean isUpdateFailed() {
		return debugger.isUpdateFailed();
	}
	
	public String getStepFailedMsg() {
		return debugger.getStepFailedMsg();
	}
	
	public CoreASMError getLastError() {
		return debugger.getLastError();
	}
	
	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public boolean canTerminate() {
		return debugger.getStatus() == EngineDriverStatus.running || debugger.getStatus() == EngineDriverStatus.paused;
	}

	@Override
	public boolean isTerminated() {
		return debugger.getStatus() == EngineDriverStatus.stopped;
	}

	@Override
	public void terminate() throws DebugException {
		debugger.stop();
	}

	@Override
	public boolean canResume() {
		return !isTerminated() && isSuspended();
	}

	@Override
	public boolean canSuspend() {
		return !isTerminated() && !isSuspended();
	}

	@Override
	public boolean isSuspended() {
		return debugger.getStatus() == EngineDriverStatus.paused;
	}

	@Override
	public void resume() throws DebugException {
		debugger.resume();
	}

	@Override
	public void suspend() throws DebugException {
		debugger.pause();
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		// TODO Auto-generated method stub
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean canDisconnect() {
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
	}

	@Override
	public boolean isDisconnected() {
		return false;
	}

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	@Override
	public IProcess getProcess() {
		return null;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		return threads;
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return !isTerminated();
	}

	@Override
	public String getName() throws DebugException {
		if (name == null) {
			name = "";
			try {
				name = getLaunch().getLaunchConfiguration().getAttribute(ICoreASMConfigConstants.PROJECT, "");
				while (!Character.isLetterOrDigit(name.charAt(0)))
					name = name.substring(1);
			} catch (CoreException e) {
			}
		}
		return name;
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		if (!isTerminated() && breakpoint.getModelIdentifier().equals(getModelIdentifier())) {
			try {
				String program = getLaunch().getLaunchConfiguration().getAttribute(ICoreASMConfigConstants.PROJECT, (String)null);
				if (program != null) {
					IMarker marker = breakpoint.getMarker();
					if (marker != null) {
						IPath p = new Path(program);
						return marker.getResource().getFullPath().equals(p);
					}
				}
			} catch (CoreException e) {
			}
		}
		return false;
	}
	
	@Override
	public ILaunch getLaunch() {
		return launch;
	}
	
	@Override
	public void fireEvent(DebugEvent event) {
		// All events that are fired on the debug target should be fired on the thread as well.
		try {
			DebugEvent threadEvent = new DebugEvent(getThreads()[0], event.getKind(), event.getDetail());
			if (DebugPlugin.getDefault() != null)
				DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event,threadEvent});
		} catch (DebugException e) {
		}
	}
}
