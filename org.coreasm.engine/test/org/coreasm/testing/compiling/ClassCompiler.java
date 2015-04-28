package org.coreasm.testing.compiling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

public class ClassCompiler {
	private List<File> tasks;
	private List<String> errors;
	private List<String> warnings;
	
	public ClassCompiler(){
		tasks = new ArrayList<File>();
		errors = new ArrayList<String>();
		warnings = new ArrayList<String>();
	}
	
	public void addTask(File f){
		tasks.add(f);
	}
	
	public List<String> getErrors(){
		return Collections.unmodifiableList(errors);
	}
	
	public List<String> getWarnings(){
		return Collections.unmodifiableList(warnings);
	}
	
	public void compile() throws Exception{
		errors.clear();
		warnings.clear();
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		if(jc == null){
			errors.add("javac not found");
			throw new Exception("java compiler not found");
		}
		//create a diagnostics object to collect errors
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		//set up a file manager to provide java sources
		StandardJavaFileManager fileManager = jc.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(tasks);
		//set compiler options
		ArrayList<String> copt = new ArrayList<String>();
		
		//there was some kind of warning this option prevented from
		//displaying. not sure right now what it was and also its not appearing anymore.
		//leaving this line here for further reference
		//copt.add("-Xlint:unchecked");
		CompilationTask task = jc.getTask(null, fileManager, diagnostics, copt, null, units);

		task.call();		
		for(Diagnostic<?> error : diagnostics.getDiagnostics()){
			if(error.getKind() == Diagnostic.Kind.ERROR){
				errors.add(error.toString());
			}
			else if(error.getKind() == Diagnostic.Kind.WARNING){
				warnings.add(error.toString());
			}
		}	
		
		if(errors.size() > 0) throw new Exception("compilation failed");
		
		try {
			fileManager.close();
		} catch (IOException e) {
			throw new Exception("could not close file manager");
		}
	}
}
