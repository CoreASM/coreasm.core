package org.coreasm.compiler;
import org.coreasm.compiler.exception.CompilerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the CoreASMCompiler CoreASMC.
 * Parses the command line and runs a CompilerEngine translating
 * the provided specification
 * @author Markus Brenner
 *
 */
public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Main program entry point
	 * @param args The command line parameters. 
	 * @see CommandLineParser
	 */
	public static void main(String[] args){			
		CompilerOptions co = null;
		try {
			co = CommandLineParser.parseCommandLine(args);
		} catch (CommandLineException e) {
			logger.error("malformed command line: " + e.getMessage());
			System.exit(0);
		}
		CoreASMCompiler compiler = new CoreASMCompiler(co);

		try{
			compiler.compile();
		}
		catch(CompilerException ce){
			System.out.println("Build has failed");
			//logger.error("Build has failed");
			//ce.printStackTrace();
		}
	}
}
