package org.coreasm.eclipse.debug.core.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

/**
 * Implementation of a line breakpoint
 * @author Michael Stegmaier
 *
 */
public class ASMLineBreakpoint extends LineBreakpoint {

	public ASMLineBreakpoint() {
	}

	public ASMLineBreakpoint(final IResource resource, final int lineNumber) throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = resource.createMarker("asm.markerType.lineBreakpoint");
				setMarker(marker);
				marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IMarker.MESSAGE, "Line Breakpoint: " + resource.getName() + " [line: " + lineNumber + "]");
			}
		};
		run(getMarkerRule(resource), runnable);
	}
	
	/**
	 * Returns the name of the specification assigned to this method breakpoint.
	 * @return the name of the specification assigned to this method breakpoint
	 */
	public String getSpecName() {
		return getMarker().getResource().getName();
	}
	
	@Override
	public String getModelIdentifier() {
		return "org.coreasm.eclipse.debug";
	}
}
