package org.coreasm.eclipse.debug.ui.views;

import java.util.HashSet;
import java.util.Set;

import org.coreasm.eclipse.debug.core.model.ASMWatchpoint;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
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
	private Update update;
	private boolean onBreakpoint;

	private ASMUpdate(Update update) {
		super(EngineDebugger.getRunningInstance().getUpdateContext(update));
		
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
					else if (breakpoint instanceof ASMWatchpoint && (((ASMWatchpoint)breakpoint).isAccess() && !((ASMWatchpoint)breakpoint).getFuctionName().equals(getLocation().name) && getText().contains(((ASMWatchpoint)breakpoint).getFuctionName() + "(")
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
	 * @return the wrapped update set
	 */
	public static Set<ASMUpdate> wrapUpdateSet(Set<Update> updates) {
		HashSet<ASMUpdate> asmUpdateSet = new HashSet<ASMUpdate>();
		if (EngineDebugger.getRunningInstance() != null) {
			for (Update update : updates)
				asmUpdateSet.add(new ASMUpdate(update));
		}
		return asmUpdateSet;
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
	public String toString() {
//		String string = super.toString() + " (";
//		for (Element agent : update.agents) {
//			if (!string.endsWith("("))
//				string += ", ";
//			string += agent.denotation();
//		}
//		return string + ")";
		return update.toString();
	}
}
