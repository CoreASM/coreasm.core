package org.coreasm.engine.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.coreasm.compiler.CompilerOptions;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.Engine;
import org.coreasm.engine.EngineProperties;
import org.coreasm.util.Tools;

public class CompilerDriver {
	public static TestReport runSpecification(File testFile){
		//extract parameters and expected results from the testcase
		List<String> requiredOutputList = TestAllCasm.getFilteredOutput(testFile, "@require");
		List<String> refusedOutputList = TestAllCasm.getFilteredOutput(testFile, "@refuse");
		int minSteps = TestAllCasm.getParameter(testFile, "minsteps");
		if (minSteps <= 0)
			minSteps = 1;
		int maxSteps = TestAllCasm.getParameter(testFile, "maxsteps");
		if (maxSteps < minSteps)
			maxSteps = minSteps;
		
		//create a CoreASM engine
		CoreASMEngine engine = (Engine) CoreASMEngineFactory.createEngine();
		engine.setClassLoader(CoreASMEngineFactory.class.getClassLoader());
		String pluginFolders = Tools.getRootFolder(Engine.class)+"/plugins";
		if (System.getProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY) != null)
			pluginFolders += EngineProperties.PLUGIN_FOLDERS_DELIM
					+ System.getProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY);
		engine.setProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY, pluginFolders);
		engine.initialize();
		engine.waitWhileBusy();
		//Create compiler options, set the maximum step count and activate necessary output
		CompilerOptions options = new CompilerOptions();
		System.out.println(Tools.getRootFolder(Engine.class)+"/../engine-1.6.5-SNAPSHOT.jar");
		options.enginePath = new File(Tools.getRootFolder(Engine.class)+"/../engine-1.6.5-SNAPSHOT.jar");
		options.outputFile = new File("compiledTest.jar");
		options.removeExistingFiles = true;
		options.SpecificationName = testFile;
		options.terminateOnStepCount = maxSteps + 1;
		//Create a compiler using the CoreASM engine
		CoreASMCompiler compiler = new CoreASMCompiler(options, engine);
		try{
			compiler.compile();
		}
		catch(Exception e){
			return new TestReport(testFile, "Compilation failed: " + e.getMessage(), -1, false);
		}
		
		//file should now be compiled. Launch it as a separate process; requires a java executable on the PATH
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec("java -jar compiledTest.jar");
			
		} catch (IOException e) {
			return new TestReport(testFile, "Running failed: " + e.getMessage(), -1, false);
		}
		
		StreamGobbler in = new StreamGobbler(proc.getInputStream());
		StreamGobbler err = new StreamGobbler(proc.getErrorStream());
		Thread g1 = new Thread(in);
		Thread g2 = new Thread(err);
		g1.start();
		g2.start();
		try{
			proc.waitFor();
		}
		catch(Exception e){
			return new TestReport(testFile, "Waiting for process failed: " + e.getMessage(), -1, false);
		}
		in.stopThread();
		err.stopThread();
		
		
		//check for errors
		if (!err.output.toString().equals("")) {
			String failMessage = "An error occurred in " + testFile.getName() + ":" + err.output.toString();
			return new TestReport(testFile, failMessage, -1, false);
		}
		
		//loop through output lines
		String out = in.output.toString();		
		for(String l : requiredOutputList){
			if(!out.contains(l)){
				String failMessage = "missing required output for test file: " + testFile.getName()
						+ "\nmissing output:\n"
						+ l
						+ "\nactual output:\n" + out;
				return new TestReport(testFile, failMessage, -1 - 1, false);
			}
		}
		for(String l : refusedOutputList){
			if(out.contains(l)){
				String failMessage = "refused output found in test file: " + testFile.getName()
						+ "\nrefused output:\n"
						+ l
						+ "\nactual output:\n" + out;
				return new TestReport(testFile, failMessage, -1, false);
			}
		}
		
		return new TestReport(testFile, "Success", -1, true);
	}
}

class StreamGobbler implements Runnable{
	public StringBuilder output;
	//public List<String> lines;
	private InputStream stream;
	private boolean quit;
	
	public StreamGobbler(InputStream in){
		//lines = new ArrayList<String>();
		stream = in;
		quit = false;
		output = new StringBuilder();
	}
	
	public void run(){
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			
			while((line = br.readLine()) != null){
				if(output.length() == 0)
					output.append(line);
				else
					output.append("\n").append(line);
				//lines.add(line);
				if(quit) break;
			}
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void stopThread(){
		this.quit = true;
	}
}