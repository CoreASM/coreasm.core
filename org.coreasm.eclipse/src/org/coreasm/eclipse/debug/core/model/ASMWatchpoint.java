package org.coreasm.eclipse.debug.core.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IWatchpoint;

/**
 * Implementation of a watchpoint. That's a breakpoint triggered by the access or modification of a function/variable
 * @author Michael Stegmaier
 *
 */
public class ASMWatchpoint extends ASMLineBreakpoint implements IWatchpoint {
	
	public ASMWatchpoint() {
	}
	
	public ASMWatchpoint(final IResource resource, final int lineNumber, final String functionName, final String functionType) throws DebugException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = resource.createMarker("asm.markerType.watchpoint");
				setMarker(marker);
				marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IMarker.MESSAGE, "Watchpoint: " + resource.getName() + " [line: " + lineNumber + "]");
				marker.setAttribute("FUNCTION_NAME", functionName);
				marker.setAttribute("FUNCTION_TYPE", functionType);
				setAccess(supportsAccess());
				setModification(supportsModification());
			}
		};
		run(getMarkerRule(resource), runnable);
	}
	
	@Override
	public boolean isAccess() throws CoreException {
		return getMarker().getAttribute("ACCESS", true);
	}

	@Override
	public void setAccess(boolean access) throws CoreException {
		setAttribute("ACCESS", access);
	}

	@Override
	public boolean isModification() throws CoreException {
		return getMarker().getAttribute("MODIFICATION", true);
	}

	@Override
	public void setModification(boolean modification) throws CoreException {
		setAttribute("MODIFICATION", modification);
	}

	@Override
	public boolean supportsAccess() {
		return true;
	}

	@Override
	public boolean supportsModification() {
		return !"EnumerationDefinition".equals(getFunctionType()) && !"DerivedFunctionDeclaration".equals(getFunctionType());
	}

	/**
	 * Returns the name of the function assigned to this watchpoint.
	 * @return the name of the function assigned to this watchpoint
	 */
	public String getFuctionName() {
		return getMarker().getAttribute("FUNCTION_NAME", (String)null);
	}
	
	/**
	 * Returns the type of the function assigned to this watchpoint.
	 * @return the type of the function assigned to this watchpoint
	 */
	public String getFunctionType() {
		return getMarker().getAttribute("FUNCTION_TYPE", (String)null);
	}
}
