package org.coreasm.compiler.plugins.io.include;

import java.util.ArrayList;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;

/**
 * Stores IO constants
 * @author Spellmaker
 *
 */
public class IOPlugin {
	/**
	 * The name of the output function
	 */
	public static final String OUTPUT_FUNC_NAME = "output";
	/**
	 * The name of the input function
	 */
	public static final String INPUT_FUNC_NAME = "input";
	/**
	 * The location for the output function
	 */
	public static final Location OUTPUT_FUNC_LOC = new Location(OUTPUT_FUNC_NAME, new ArrayList<Element>());
	/**
	 * The location for the input function
	 */
	public static final Location INPUT_FUNC_LOC = new Location(INPUT_FUNC_NAME, new ArrayList<Element>());
	/**
	 * The update action for the print updates
	 */
	public static final String PRINT_ACTION = "printAction";
}
