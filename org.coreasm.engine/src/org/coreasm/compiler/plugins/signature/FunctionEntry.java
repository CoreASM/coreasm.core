package org.coreasm.compiler.plugins.signature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CompilerOptions;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Represents a general function entry with a type, name and possible starting values
 * @author Markus Brenner
 *
 */
public class FunctionEntry implements LibraryEntry {
	private String name;
	private String fclass;
	private List<String> domain;
	private String range;
	private CodeFragment init;
	private CompilerEngine engine;

	/**
	 * Builds a new function entry
	 * @param name The name of the final function
	 * @param fclass The function class
	 * @param domain The domain of the function type
	 * @param range The range of the function type
	 * @param init A code fragment generating the initial values for the function
	 */
	public FunctionEntry(String name, String fclass, List<String> domain, String range, CodeFragment init, CompilerEngine engine){
		this.name = name;
		this.fclass = fclass;
		this.domain = domain;
		this.range = range;
		this.init = init;
		this.engine = engine;
	}
	
	@Override
	public void writeFile() throws LibraryEntryException {
		CompilerOptions options = engine.getOptions();
		File directory = new File(options.tempDirectory + File.separator + "plugins" + File.separator + "SignaturePlugin");
		File file = new File(directory, "Func_" + name + ".java");
		
		if(file.exists()) throw new LibraryEntryException(new Exception("file already exists"));
		
		BufferedWriter bw = null;
		
		directory.mkdirs();
		try {
			file.createNewFile();
		
			bw = new BufferedWriter(new FileWriter(file));
		
			bw.write(this.generate());
		} 
		catch (IOException e){
			throw new LibraryEntryException(e);
		} 
		catch(CodeFragmentException e) {
			throw new LibraryEntryException(e);
		}
		finally{
			try{
				bw.close();
			}
			catch(IOException e){
			}
		}
	}

	private String generate() throws CodeFragmentException {
		String result = "";
		
		result += "package plugins.SignaturePlugin;\n";
		result += "public class Func_" + name + " extends CompilerRuntime.MapFunction{\n";
		
		result += "public Func_" + name + "() throws Exception{\n";
			if(this.fclass != null) result += "this.setFClass(FunctionClass.fc" + fclass + ");\n";
			result += "CompilerRuntime.Signature sig = new CompilerRuntime.Signature();\n";
			result += "java.util.List<String> list = new java.util.ArrayList<String>();\n";
			for(int i = 0; i < domain.size(); i++){
				result += "list.add(\"" + domain.get(i) + "\");\n";
			}
			result += "sig.setDomain(list);\n";
			result += "sig.setRange(\"" + range + "\");\n";
			result += "this.setSignature(sig);\n";

			if(init != null){
				result += "CompilerRuntime.EvalStack evalStack = new CompilerRuntime.EvalStack();\n";
				result += "CompilerRuntime.LocalStack localStack = new CompilerRuntime.LocalStack();\n";
				result += "java.util.Map<String, CompilerRuntime.RuleParam> ruleparams = new java.util.HashMap<String, CompilerRuntime.RuleParam>();\n";
				result += init.generateCode(engine);
				result += "CompilerRuntime.Element initValue = (CompilerRuntime.Element) evalStack.pop();\n";
				if(this.domain.size() == 0){
					result += "try {\n";
					result += "    setValue(CompilerRuntime.ElementList.NO_ARGUMENT, initValue);\n";
					result += "} catch (CompilerRuntime.UnmodifiableFunctionException e) {}\n";
				}
				else{
					result += "if (initValue instanceof CompilerRuntime.FunctionElement) {\n";
					result += "    CompilerRuntime.FunctionElement map = (CompilerRuntime.FunctionElement) initValue;\n";
					result += "    int dSize = " + domain.size() + ";\n";
					result += "    for (CompilerRuntime.Location l: map.getLocations(\""+ name + "\")) {\n";
					result += "    	if (l.args.size() == dSize) {\n";
					result += "            try {\n";
					result += "                setValue(l.args, map.getValue(l.args));\n";
					result += "            } catch (CompilerRuntime.UnmodifiableFunctionException e) {}\n";
					result += "    	} else {\n";
					result += "    		if (l.args.size() == 1 \n";
					result += "    				&& l.args.get(0) instanceof CompilerRuntime.Enumerable \n";
					result += "    				&& ((CompilerRuntime.Enumerable)l.args.get(0)).enumerate().size() == dSize) \n";
					result += "    		{\n";
					result += "                try {\n";
					result += "                    setValue(CompilerRuntime.ElementList.create(((CompilerRuntime.Enumerable)l.args.get(0)).enumerate()), map.getValue(l.args));\n";
					result += "                } catch (CompilerRuntime.UnmodifiableFunctionException e) {}\n";
					result += "    		}\n";
					result += "    		else\n";
					result += "            	throw new CompilerRuntime.EngineError(\"Initial value of function " + name + " does not match the function signature.\");\n";
					result += "    	}\n";
					result += "            \n";
					result += "    }        \n";                          
					result += "}\n";
				}
			}
		result += "}\n";
		result += "}\n";
		
		return result;
	}

	@Override
	public String getFullName() {
		return "plugins.SignaturePlugin.Func_" + name;
	}

}
