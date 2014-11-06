package org.coreasm.compiler.plugins.io.include;

import java.util.ArrayList;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;


public class IOPlugin {
	public static final String OUTPUT_FUNC_NAME = "output";
	public static final String INPUT_FUNC_NAME = "input";
	public static final Location OUTPUT_FUNC_LOC = new Location(OUTPUT_FUNC_NAME, new ArrayList<Element>());
	public static final Location INPUT_FUNC_LOC = new Location(INPUT_FUNC_NAME, new ArrayList<Element>());
	public static final String PRINT_ACTION = "printAction";
}
