package org.coreasm.compiler;

import java.io.File;

/**
 * Parses command line flags into compiler options.
 * Currently checks, if an option i equals one of the known hard coded
 * options and (if applicable) consumes more parameters.
 * Supports the following parameters:
 * <table>
 * 	<thead>
 * 	</thead>
 * 	<tbody>
 * 		<tr><td>-o file</td><td>Sets the output file to file</td></tr>
 * 		<tr><td>-t dir</td><td>Sets the temporary compilation directory to dir</td></tr>
 * 		<tr><td>-i file</td><td>Sets the specification to file</td></tr>
 * 		<tr><td>-terminateOnError [true/false]</td><td>Sets terminateOnError to the given value</td></tr>
 * 		<tr><td>-terminateOnFailedUpdate [true/false]</td><td>Sets terminateOnFailedUpdate to the given value</td></tr>
 * 		<tr><td>-terminateOnEmptyUpdate [true/false]</td><td>Sets terminateOnEmptyUpdate to the given value</td></tr>
 * 		<tr><td>-terminateOnSameUpdate [true/false]</td><td>Sets terminateOnSameUpdate to the given value</td></tr>
 * 		<tr><td>-terminateOnUndefAgent [true/false]</td><td>Sets terminateOnUndefAgent to the given value</td></tr>
 * 		<tr><td>-terminateOnStepCount int</td><td>Sets terminateOnStepCount to the given int</td></tr>
 * 		<tr><td>-logUpdatesAfterStep [true/false]</td><td>Sets logUpdatesAfterstep to the given value</td></tr>
 * 		<tr><td>-logEndOfStep [true/false]</td><td>Sets logEndOfStep to the given value</td></tr>
 * 		<tr><td>-logAgentSetAfterStep [true/false]</td><td>Sets logAgentSetAfterStep to the given value</td></tr>
 * 		<tr><td>-logStateTransition [true/false]</td><td>Sets logStateTransition to the given value</td></tr>
 * 		<tr><td>-removeExistingFiles [true/false]</td><td>Instructs the compiler to remove existing files in the temporary directory</td></tr>
 * 		<tr><td>-keepFiles [true/false]</td><td>Instructs the compiler to keep temporary files</td></tr>
 * 		<tr><td>-noCompile [true/false]</td><td>Instructs the compiler not to use the java compiler</td></tr>
 *	</tbody>
 * </table>
 * All other parameters will produce a CommandLineException.
 * The Parser will return a {@link CompilerOptions} object holding the specified options.
 * @see CompilerOptions for more information
 * @author Markus Brenner
 *
 */
public class CommandLineParser {
	/**
	 * Parses the given command line into a CompilerOptions object
	 * @param args The command line provided to the program
	 * @return A CompilerOptions Object holding the specified options
	 * @throws CommandLineException If the command line was incorrect
	 */
	public static CompilerOptions parseCommandLine(String[] args) throws CommandLineException{
		CompilerOptions options = new CompilerOptions();
		
		try{
			for(int i = 0; i < args.length; i++){
				if(args[i].equals("-o")){
						options.outputFile = new File(args[i + 1]);
						i++;
				}
				else if(args[i].equals("-t")){
					options.tempDirectory = new File(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-i")){
					if(options.SpecificationName == null){
						options.SpecificationName = new File(args[i + 1]);
						i++;
					}
					else{
						throw new CommandLineException("Cannot specify multiple input files");
					}
				}
				else if(args[i].equals("-terminateOnError")){
					options.terminateOnError = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-terminateOnFailedUpdate")){
					options.terminateOnFailedUpdate = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-terminateOnEmptyUpdate")){
					options.terminateOnEmptyUpdate = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-terminateOnSameUpdate")){
					options.terminateOnSameUpdate = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-terminateOnUndefAgent")){
					options.terminateOnUndefAgent = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-noCompile")){
					options.noCompile = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-terminateOnStepCount")){
					options.terminateOnStepCount = Integer.parseInt(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-logUpdatesAfterStep")){
					options.logUpdatesAfterStep = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-logStateAfterStep")){
					options.logStateAfterStep = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-logEndOfStep")){
					options.logEndOfStep = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-logAgentSetAfterStep")){
					options.logAgentSetAfterStep = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-logStateTransition")){
					options.logStateTransition = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-removeExistingFiles")){
					options.removeExistingFiles = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-keepFiles")){
					options.keepTempFiles = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
				else if(args[i].equals("-enginePath")){
					options.enginePath = args[i + 1];
					i++;
				}
				else if(args[i].equals("-runtimeDirectory")){
					options.runtimeDirectory = new File(args[i + 1]);
					i++;
				}
				else{
					throw new CommandLineException("Unknown command line option: " + args[i]);
				}
			}
		}
		catch(IndexOutOfBoundsException e){
			throw new CommandLineException("wrongly formatted input");
		}
		catch(NumberFormatException e){
			throw new CommandLineException("wrongly formatted input");
		}
		
		if(options.SpecificationName == null) throw new CommandLineException("No specification provided");
		
		
		return options;
	}
}
