package org.coreasm.eclipse.debug.core.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Implementation of a method breakpoint. That's a breakpoint triggered when a rule is being evaluated
 * @author Michael Stegmaier
 *
 */
public class ASMMethodBreakpoint extends ASMLineBreakpoint {

	public ASMMethodBreakpoint() {
	}
	
	public ASMMethodBreakpoint(final IResource resource, final int lineNumber, final String ruleName) throws DebugException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = resource.createMarker("asm.markerType.methodBreakpoint");
				setMarker(marker);
				marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IMarker.MESSAGE, "Method Breakpoint: " + resource.getName() + " [line: " + lineNumber + "]");
				marker.setAttribute("RULE_NAME", ruleName);
			}
		};
		run(getMarkerRule(resource), runnable);
	}
	
	/**
	 * Returns the name of the rule assigned to this method breakpoint.
	 * @return the name of the rule assigned to this method breakpoint
	 */
	public String getRuleName() {
		return getMarker().getAttribute("RULE_NAME", (String)null);
	}
}
