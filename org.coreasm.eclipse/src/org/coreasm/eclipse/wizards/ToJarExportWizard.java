package org.coreasm.eclipse.wizards;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.coreasm.compiler.CompilerOptions;


public class ToJarExportWizard extends Wizard implements IExportWizard {
	private ToJarExportWizardPage page;
	private IFile selected;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if(selection.getFirstElement() instanceof IFile){
			selected = (IFile) selection.getFirstElement();
		}
	}
	
	@Override
	public String getWindowTitle(){
		return "CoreASM To Jar Export";
	}
	
	@Override
	public void addPages(){
		page = new ToJarExportWizardPage(selected);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		System.out.println("requested to compile specification");
		
		//build a compiler options object and fill in information
		CompilerOptions co = new CompilerOptions();

		co.SpecificationName = page.getSpec();
		co.tempDirectory = "tmp";
		
		//create temporary directory
		Path tmppath = null;
		try {
			tmppath = Files.createTempDirectory("coreasmc");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		co.tempDirectory = tmppath.toAbsolutePath().toString();		
		co.outputFile = page.getJar();
		
		Map<String, Boolean> settings = page.getSettings();
		
		
		co.keepTempFiles = settings.get("keepTempFiles");
		//TODO: set these to correct values later on		
			
		co.removeExistingFiles = settings.get("removeExistingFiles");
		co.terminateOnError = settings.get("terminateOnError");		
		co.terminateOnFailedUpdate = settings.get("terminateOnFailedUpdate");		
		co.terminateOnEmptyUpdate = settings.get("terminateOnEmptyUpdate");
		co.terminateOnSameUpdate = settings.get("terminateOnSameUpdate");
		co.terminateOnUndefAgent = settings.get("terminateOnUndefAgent");		
		co.terminateOnStepCount = page.getStepCount();				
		co.logUpdatesAfterStep = settings.get("logUpdatesAfterStep");	
		co.logStateAfterStep = settings.get("logStateAfterStep");		
		co.logEndOfStep = settings.get("logEndOfStep");
		co.logAgentSetAfterStep = settings.get("logAgentSetAfterStep");
		co.logStateTransition = settings.get("logStateTransition");
		co.noCompile = settings.get("noCompile");
		
		
		CompileJob cj = new CompileJob("Compiling CoreASM specification", co);
		cj.setPriority(Job.BUILD);
		cj.schedule();
		return true;
		
		/*CoreASMCompiler casmc = new CoreASMCompiler(co, CoreASMEngineFactory.createCoreASMEngine());
		try{
			casmc.compile();
		}
		catch(CompilerException ce){
			ce.printStackTrace();
			return false;
		}
		return true;*/
	}

	@Override
	public boolean isHelpAvailable(){
		return false;
	}
	
	@Override
	public boolean needsPreviousAndNextButtons(){
		return false;
	}
}
