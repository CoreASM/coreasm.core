package org.coreasm.eclipse.wizards.compiler;

import java.io.File;
import java.io.IOException;
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
		CompilerOptions co = page.getResult();
		
		//create temporary directory
		try{
			File temp = File.createTempFile("coreasmc", Long.toString(System.nanoTime()));
			temp.mkdir();
			
			co.tempDirectory = temp;	
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		CompileJob cj = new CompileJob("Compiling CoreASM specification", co, page.runJar());
		cj.setPriority(Job.BUILD);
		cj.schedule();
		return true;
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
