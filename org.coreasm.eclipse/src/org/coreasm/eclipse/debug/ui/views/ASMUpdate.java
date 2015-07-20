package org.coreasm.eclipse.debug.ui.views;

import java.util.HashSet;
import java.util.Set;

import org.coreasm.eclipse.debug.core.model.ASMWatchpoint;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Wrapper class for ASM Updates. Extends ASMUpdateViewElement for easier integration into the ASM Update View
 * @author Michael Stegmaier
 *
 */
public class ASMUpdate extends ASMUpdateViewElement {
	private final Update update;
	private final boolean consistent;
	private boolean onBreakpoint;
	
	private ASMUpdate(Update update, boolean consistent, ControlAPI capi) {
		super(update, capi);
		
		this.consistent = consistent;
		this.update = update;

		for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug")) {
			try {
				if (!breakpoint.isEnabled())
					continue;
				if (getSourceName().equals(breakpoint.getMarker().getResource().getName())) {
					if (breakpoint instanceof ILineBreakpoint && ((ILineBreakpoint)breakpoint).getLineNumber() == getLineNumber()) {
						onBreakpoint = true;
						break;
					}
					else if (breakpoint instanceof ASMWatchpoint && (((ASMWatchpoint)breakpoint).isAccess() && !((ASMWatchpoint)breakpoint).getFuctionName().equals(getLocation().name) && toString().contains(((ASMWatchpoint)breakpoint).getFuctionName() + "(")
																	 || ((ASMWatchpoint)breakpoint).isModification() && ((ASMWatchpoint)breakpoint).getFuctionName().equals(getLocation().name))) {
						onBreakpoint = true;
						break;
					}
				}
			} catch (CoreException e) {
			}
		}
	}
	
	/**
	 * Wraps a given set of the class Update into a set of the class ASMUpdate.
	 * @param updates the set of updates to be wrapped
	 * @param consistent whether the set of updates is consistent
	 * @param capi the ControlAPI
	 * @return the wrapped update set
	 */
	public static Set<ASMUpdate> wrapUpdateSet(Set<Update> updates, boolean consistent, ControlAPI capi) {
		if (updates == null)
			return null;
		HashSet<ASMUpdate> asmUpdateSet = new HashSet<ASMUpdate>();
		for (Update update : updates)
			asmUpdateSet.add(new ASMUpdate(update, consistent, capi));
		return asmUpdateSet;
	}
	
	public static Set<ASMUpdate> wrapUpdateSet(ControlAPI capi) {
		return wrapUpdateSet(capi.getUpdateSet(0), true, capi);
	}
	
	public static Set<Update> unwrap(Set<ASMUpdate> asmUpdateSet) {
		HashSet<Update> updates = new HashSet<Update>();
		for (ASMUpdate asmUpdate : asmUpdateSet)
			updates.add(asmUpdate.update);
		return updates;
	}
	
	public Update getUpdate() {
		return update;
	}
	
	/**
	 * Returns the location assigned to this update.
	 * @return the location assigned to this update
	 */
	public Location getLocation() {
		return update.loc;
	}
	
	/**
	 * Returns the agents assigned to this update.
	 * @return the agents assigned to this update
	 */
	public Set<Element> getAgents() {
		return update.agents;
	}
	
	/**
	 * Returns whether this update is affected by a breakpoint
	 * @return the location assigned to this update
	 */
	public boolean isOnBreakpoint() {
		return onBreakpoint;
	}
	
	@Override
	public boolean isError() {
		return !consistent;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((update == null) ? 0 : update.hashCode());
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
		ASMUpdate other = (ASMUpdate) obj;
		if (update == null) {
			if (other.update != null)
				return false;
		} else if (!update.equals(other.update))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (!consistent)
			return "Inconsistent Update: " + update.toString();
		return update.toString();
	}
}
