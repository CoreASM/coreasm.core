package org.coreasm.compiler.interfaces;

import java.util.List;

import org.coreasm.compiler.components.mainprogram.statemachine.EngineTransition;

/**
 * Interface for extension point plugins.
 * An extension point plugins hooks additional code into the state machine
 * transitions of the main class.
 * @author Markus Brenner
 *
 */
public interface CompilerExtensionPointPlugin extends CompilerPlugin {
	/**
	 * Returns the list of transitions this class provides for the state machine.
	 * @return A list of EngineTransitions
	 */
	public List<EngineTransition> getTransitions();
}
