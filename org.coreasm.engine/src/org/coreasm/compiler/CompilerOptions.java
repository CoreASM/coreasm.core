package org.coreasm.compiler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates all compiler options
 * @author Markus Brenner
 *
 */
public class CompilerOptions {
	/**
	 * The name of the provided specification
	 */
	public File SpecificationName = null;
	/**
	 * The target directory for the temporary java classes
	 */
	public File tempDirectory = new File("tmp");
	/**
	 * Path to the engine jar
	 */
	public File enginePath = null;
	// loading compiler runtime from a directory is currently no longer supported,
	// as the runtime is built at runtime from the source files.
	// The runtime must be provided in an engine .jar, which can be built using maven
	// /**
	//  * Path to the runtime files
	//  */
	//public File runtimeDirectory = new File("CompilerRuntime");//"C:\\Users\\Spellmaker\\git\\pmcoreasm\\org.coreasm.engine\\src\\CompilerRuntime\\";
	/**
	 * The final output jar archive
	 */	
	public File outputFile = new File("Main.jar");
	/**
	 * Signals, if the temporary classes should be deleted after the compilation process
	 */
	public boolean keepTempFiles = true;
	/**
	 * Signals, if already existing files will be purged before new files are written
	 */
	public boolean removeExistingFiles = true;
	
	//options taken from the CoreASM Settings panel
	//termination options
	/**
	 * If set to true, the compiled application will terminate on errors
	 * Note: This is currently ignored and is the default behavior
	 */
	public boolean terminateOnError = false;		
	/**
	 * If set to true, the compiled application will terminate
	 * after an update has failed. Also currently default behavior
	 */
	public boolean terminateOnFailedUpdate = false;		
	/**
	 * If set to true, the compiled application after a step
	 * produced an empty update set
	 */
	public boolean terminateOnEmptyUpdate = false;
	/**
	 * If set to true, the compiled application will terminate after
	 * two steps produced the same update
	 */
	public boolean terminateOnSameUpdate = false;
	/**
	 * If set to true, the compiled application will terminate
	 * if no agent has a program other than undef
	 */
	public boolean terminateOnUndefAgent = false;		
	/**
	 * If set to a value other than -1, the compiled application will
	 * terminate after terminateOnStepCount steps
	 */
	public int terminateOnStepCount = -1;				
	//logging / verbosity
	/**
	 * If set to true, the compiled application will log the updates
	 * of each step
	 */
	public boolean logUpdatesAfterStep = false;			
	/**
	 * If set to true, the compiled application will log the state of
	 * the abstract storage after each step
	 */
	public boolean logStateAfterStep = false;		
	/**
	 * If set to true, the compiled application will log
	 * the end of each step
	 */
	public boolean logEndOfStep = false;
	/**
	 * If set to true, the compiled application will log
	 * the last selected agent set after each step
	 */
	public boolean logAgentSetAfterStep = false;
	/**
	 * If set to true, the compiled application will log all
	 * state transitions
	 */
	public boolean logStateTransition = false;		
	/**
	 * If set to true, the compiler will not run the java compiler
	 */
	public boolean noCompile = false;
	/**
	 * If set to true, the compiler will print the amount of time
	 * spend in different compilation stages
	 */
	public boolean logTimings = false;
	/**
	 * Amount of time the preprocessor should run before it is terminated
	 */
	public int preprocessorRuns = 10;
	/**
	 * If set to true the compiler will hide the output from the coreasm engine
	 */
	public boolean hideCoreASMOutput = true;
	/**
	 * General properties provided by plugins
	 * Any Plugin can store options for other plugins here
	 */
	public Map<String, String> properties = new HashMap<String, String>();
}
