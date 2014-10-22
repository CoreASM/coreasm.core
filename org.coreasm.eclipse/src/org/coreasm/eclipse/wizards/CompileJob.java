package org.coreasm.eclipse.wizards;

import java.io.IOException;
import java.util.List;

import org.coreasm.compiler.CompilerOptions;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.LoggingHelper.Level;
import org.coreasm.eclipse.engine.CoreASMEngineFactory;
import org.coreasm.util.Tools;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class CompileJob extends Job {
	private CompilerOptions options;
	private MessageConsole console;

	public CompileJob(String name, CompilerOptions options) {
		super(name);
		this.options = options;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		String name = "CoreASMC";
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				console = (MessageConsole) existing[i];
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		console = myConsole;
		
		options.enginePath = System.getProperty(Tools.COREASM_ENGINE_LIB_PATH);
		
		CoreASMCompiler comp = new CoreASMCompiler(options, CoreASMEngineFactory.createCoreASMEngine());
		CompilerLogger logger = new CompilerLogger(console);
		
		comp.getLogger().addListener(Level.DEBUG, logger);
		comp.getLogger().addListener(Level.ERROR, logger);
		comp.getLogger().addListener(Level.WARN, logger);
				
		Exception err = null;
		try{
			comp.compile();
		}
		catch(Exception exc){
			err = exc;
		}
		
		comp.getLogger().removeListener(Level.DEBUG, logger);
		comp.getLogger().removeListener(Level.ERROR, logger);
		comp.getLogger().removeListener(Level.WARN, logger);

		try{
			logger.destroy();
		}
		catch(IOException e){
			//mask exception
		}
		List<String> errors = comp.getErrors();
		List<String> warnings = comp.getWarnings();
		
		IStatus result = null; 
		if(err == null){
			
			MultiStatus r = new MultiStatus("CoreASM", IStatus.OK, "Compilation successfull", null);
			for(String s : warnings){
				r.add(new Status(IStatus.WARNING, "CoreASM", s));
			}
			result = r;
			
			if(options.keepTempFiles){
				MessageConsoleStream out = console.newMessageStream();
				out.print("Created source files preserved in temp directory" + System.getProperty("line.separator"));
				out.print(options.tempDirectory + System.getProperty("line.separator"));
				try{
					out.close();
				}
				catch(IOException e){
					//mask exception
				}
			}
		}
		else{	
			MultiStatus r = new MultiStatus("CoreASM", IStatus.ERROR, "Compilation failed", null);
			for(String s : errors){
				r.add(new Status(IStatus.ERROR, "CoreASM", s));
			}
			for(String s : warnings){
				r.add(new Status(IStatus.WARNING, "CoreASM", s));
			}
			result = r;
		}
		
		return result;
	}

}
