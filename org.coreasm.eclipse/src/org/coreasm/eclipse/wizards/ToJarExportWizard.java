package org.coreasm.eclipse.wizards;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
		System.out.println("spec: " + page.getSpec());
		System.out.println("jar: " + page.getJar());
		System.out.println("tmp: " + page.keepTmp());
		
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
		co.keepTempFiles = page.keepTmp();
		//TODO: set these to correct values later on
		co.removeExistingFiles = true;
		co.terminateOnError = false;		
		co.terminateOnFailedUpdate = false;		
		co.terminateOnEmptyUpdate = false;
		co.terminateOnSameUpdate = false;
		co.terminateOnUndefAgent = false;		
		co.terminateOnStepCount = -1;				
		co.logUpdatesAfterStep = false;			
		co.logStateAfterStep = false;		
		co.logEndOfStep = false;
		co.logAgentSetAfterStep = false;
		co.logStateTransition = false;
		co.noCompile = false;
		
		
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
