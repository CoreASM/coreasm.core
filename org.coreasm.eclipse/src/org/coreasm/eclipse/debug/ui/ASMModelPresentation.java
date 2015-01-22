package org.coreasm.eclipse.debug.ui;

import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.debug.core.model.ASMMethodBreakpoint;
import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.coreasm.eclipse.debug.core.model.ASMThread;
import org.coreasm.eclipse.debug.core.model.ASMWatchpoint;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This class provides the text presentation for the elements of the debug model
 * @author Michael Stegmaier
 *
 */
public class ASMModelPresentation extends LabelProvider implements IDebugModelPresentation {

	@Override
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IFile)
			return new FileEditorInput((IFile)element);
		if (element instanceof ILineBreakpoint)
			return new FileEditorInput((IFile)((ILineBreakpoint)element).getMarker().getResource());
		return null;
	}

	@Override
	public String getEditorId(IEditorInput input, Object element) {
		if (element instanceof IFile || element instanceof ILineBreakpoint)
			return "org.coreasm.eclipse.ASMEditor";
		return null;
	}

	@Override
	public void setAttribute(String attribute, Object value) {
	}
	
	@Override
	public String getText(Object element) {
		try {
			if (element instanceof ASMDebugTarget)
				return (((ASMDebugTarget) element).isTerminated() ? "<terminated>" : "") + ((ASMDebugTarget)element).getName();
			else if (element instanceof ASMThread)
				return ((ASMThread)element).getName() + (((ASMThread)element).isSuspended() ? " (Suspended)" : " (Running)");
			else if (element instanceof ASMStackFrame) {
				int step = ((ASMStackFrame)element).getStep();
				return ((ASMStackFrame)element).getName() + ":" + ((ASMStackFrame)element).getRuleName() + " line: " + ((ASMStackFrame)element).getLineNumber() + " STEP " + (step < 0 ? -step - 1 + "*" : step) + " " +  ((ASMStackFrame)element).getLastSelectedAgents();
			}
			else if (element instanceof ASMWatchpoint)
				return ((ASMWatchpoint)element).getMarker().getResource().getName() + (((ASMWatchpoint)element).isAccess() && ((ASMWatchpoint)element).isModification() ? " [access and modification]" : (((ASMWatchpoint)element).isAccess() ? "[access]" : ((ASMWatchpoint)element).isModification() ? "[modification]" : "")) + " - " + ((ASMWatchpoint)element).getFuctionName();
			else if (element instanceof ASMMethodBreakpoint)
				return ((ASMMethodBreakpoint)element).getMarker().getResource().getName() + " [line: " + ((ASMMethodBreakpoint)element).getLineNumber() + "] - " + ((ASMMethodBreakpoint)element).getRuleName();
		} catch (CoreException e) {
		}
		return null;
	}

	@Override
	public void computeDetail(IValue value, IValueDetailListener listener) {
		String result = "";
		
		try {
			result = value.getValueString();
		} catch (DebugException e) {
		}
		
		listener.detailComputed(value, result);
	}
}
