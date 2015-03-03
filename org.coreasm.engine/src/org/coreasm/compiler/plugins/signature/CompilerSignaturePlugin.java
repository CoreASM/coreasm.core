package org.coreasm.compiler.plugins.signature;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.mainprogram.statemachine.EngineTransition;
import org.coreasm.compiler.plugins.signature.code.bcode.SignatureHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.signature.FunctionDomainFunctionElement;
import org.coreasm.engine.plugins.signature.FunctionRangeFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerSignaturePlugin extends CompilerCodePlugin implements CompilerPlugin,
		CompilerVocabularyExtender, CompilerExtensionPointPlugin {

	private Plugin interpreterPlugin;
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

    private static enum CheckMode {cmOff, cmWarn, cmStrict};
    private CheckMode typeCheckingMode;    
    
    //TODO: implement undefined identifier handler so that this entry
    //is put to use
    //private CheckMode idCheckingMode;    
	
    public enum SignatureEntryType {UNIVERSE, ENUM, DERIVED, FUNCTION};
    
    public class IncludeEntry{
    	SignatureEntryType type;
    	LibraryEntry entry;
    	
    	public IncludeEntry(SignatureEntryType t, LibraryEntry e){
    		type = t;
    		entry = e;
    	}
    }
    
    private Map<String, IncludeEntry> entries;
	/**
	 * Creates a new signature plugin
	 */
	public CompilerSignaturePlugin(Plugin p){
		entries = new HashMap<String, IncludeEntry>();
		this.interpreterPlugin = p;
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException{
		
		File enginePath = engine.getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
			engine.getLogger().error(getClass(), "loading classes from a directory is currently not supported");
			throw new CompilerException("could not load classes");
		}
		else{
			try {
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.signature.EnumerationBackgroundElement", "plugins.SignaturePlugin.EnumerationBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.signature.EnumerationElement", "plugins.SignaturePlugin.EnumerationElement");
				
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/signature/include/EnumerationBackgroundElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/signature/EnumerationElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/signature/FunctionDomainFunctionElement.java", this), EntryType.FUNCTION, FunctionDomainFunctionElement.FUNCTION_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/signature/FunctionRangeFunctionElement.java", this), EntryType.FUNCTION, FunctionRangeFunctionElement.FUNCTION_NAME));
				
			} catch (IncludeException e) {
				throw new CompilerException(e);
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
			
		}
		
		//problem is here: we actually need to include universes and enums before functions, as they could be used
		//in function declarations

		try {
			for(Entry<String, IncludeEntry> e : entries.entrySet()){
				if(e.getValue().type == SignatureEntryType.ENUM || e.getValue().type == SignatureEntryType.UNIVERSE){
						classLibrary.addEntry(e.getValue().entry);
					result.add(new MainFileEntry(e.getValue().entry, (e.getValue().type == SignatureEntryType.ENUM) ? EntryType.BACKGROUND : EntryType.UNIVERSE, e.getKey()));
				}
			}
			
			for(Entry<String, IncludeEntry> e : entries.entrySet()){
				try{
					switch(e.getValue().type){
					case DERIVED:
						classLibrary.addEntry(e.getValue().entry);
						result.add(new MainFileEntry(e.getValue().entry, EntryType.FUNCTION, e.getKey()));
						break;
					case ENUM:
					case UNIVERSE:
						continue;
					case FUNCTION:
						classLibrary.addEntry(e.getValue().entry);
						result.add(new MainFileEntry(e.getValue().entry, EntryType.FUNCTION, e.getKey()));
						break;		
					}
				}
				catch(EntryAlreadyExistsException e1){
					throw new CompilerException(e1);
				}
			}
		} catch (EntryAlreadyExistsException e1) {
			throw new CompilerException(e1);
		}
		return result;
	}

	@Override
	public String getName() {
		return "SignaturePlugin";
	}

	@Override
	public List<EngineTransition> getTransitions() {		
		List<EngineTransition> result = new ArrayList<EngineTransition>();
		
		if(getTypeCheckMode() != CheckMode.cmOff){
			CodeFragment succ = new CodeFragment("");
			succ.appendLine(buildTransition(true, typeCheckingMode));
			CodeFragment fail = new CodeFragment("");
			fail.appendLine(buildTransition(false, typeCheckingMode));
			//the success and fail code only differ in the action taken.
			//therefore, build the code separately and then attach it to the codefragment afterwards
			
			
			result.add(new EngineTransition(succ, "emAggregation", "emStepSucceeded"));
			result.add(new EngineTransition(fail, "emAggregation", "emUpdateFailed"));	
		}
		
		return result;
	}
	
	private String buildTransition(boolean succ, CheckMode mode){
		//if succ is false, only the error message is emitted without
		//doing anything else
		String code = "";
		code += "@decl(CompilerRuntime.AbstractStorage,storage)=CompilerRuntime.RuntimeProvider.getRuntime().getStorage();\n";
		code += "@decl(CompilerRuntime.UpdateList,ulist)=CompilerRuntime.RuntimeProvider.getRuntime().getScheduler().getUpdateSet();\n";
		code += "for(@decl(CompilerRuntime.Update,u) : @ulist@){\n";
			code += "@decl(String,fname)=@u@.loc.name;";
			//check function
			code += "@decl(CompilerRuntime.FunctionElement,func)=@storage@.getFunction(@fname@);\n";
			code += "if(@func@ == null){\n";
				code += "System.out.println(\"Function \" + @fname@ +\" does not exist but there is an update for it\");\n";
				if(succ){
					code += "System.exit(0);\n";
				}
				else{
					code += "continue;\n";
				}
			code += "}\n";
			code += "@decl(CompilerRuntime.Signature,sig)=@func@.getSignature();\n";
			code += "if(@sig@ == null) continue;\n";
			code += "if(@u@.loc.args.size() != @sig@.getDomain().size()){\n";
				code += "System.out.println(\"The arity of function in update \" + @u@.toString() +\" does not match the signature \" + @sig@ + \" for function \" + @fname@);\n";
				if(succ){
					code += "System.exit(0);\n";
				}
				else{
					code += "continue;\n";
				}
			code += "}\n";
			code += "else{\n";
			//check parameters
				code += "for(@decl(int,i)=0; @i@ < @sig@.getDomain().size(); @i@++){\n";
					code += "@decl(CompilerRuntime.Element,arg)=@u@.loc.args.get(@i@);\n";
					code += "@decl(String,domName)=@sig@.getDomain().get(@i@);\n";
					code += "if(@arg@.equals(CompilerRuntime.Element.UNDEF)) continue;\n";
					code += "@decl(CompilerRuntime.AbstractUniverse,domain)=@storage@.getUniverse(@domName@);\n";
					code += "if(@domain@ == null){\n";
						code += "System.out.println(\"Could not find universe \" + @domName@);\n";
						code += "System.exit(0);\n";
					code += "}\n";
					code += "if(!@domain@.member(@arg@)){\n";
						code += "System.out.println(\"Parameter \" + @i@ + \" in update \" + @u@.toString() + \" is not a member of \" + @domName@ +\" and does not match the signature \" + @sig@.toString());\n";
						if(succ){
							code += "System.exit(0);\n";
						}
						else{
							code += "continue;\n";
						}
					code += "}\n";
				code += "}\n";
			code += "}\n";
			//check value
			code += "if(@u@.value.equals(CompilerRuntime.Element.UNDEF)) continue;\n";
			code += "@decl(String,rangeName)=@sig@.getRange();\n";
			code += "@decl(CompilerRuntime.AbstractUniverse,range)=@storage@.getUniverse(@rangeName@);\n";
			code += "if(@range@ == null){\n";
				code += "System.out.println(\"Could not find universe \" + @rangeName@);\n";
				code += "System.exit(0);\n";
			code += "}\n";
			code += "if(!@range@.member(@u@.value)){\n";
				code += "System.out.println(\"The value \" + @u@.value.toString() + \" is not a member of \" + @rangeName@ + \" and does not match signature \" + @rangeName@);\n";
				if(succ){
					code += "System.exit(0);\n";
				}
				else{
					code += "continue;\n";
				}
			code += "}\n";
		
		code += "}\n";
		
		return code;
	}

	private CheckMode getTypeCheckMode() {
    	String mode = engine.getOptions().properties.get("Signature.TypeChecking");
    	typeCheckingMode = CheckMode.cmOff;
    	if (mode != null) {
    		if (mode.equals("warning"))
    			typeCheckingMode = CheckMode.cmWarn;
    		else
    			if (mode.equals("on") || mode.equals("strict"))
    				typeCheckingMode = CheckMode.cmStrict;
    			else
    				if (!mode.equals("off")){
    					System.out.println("warning: Type Checking property is not set to a valid value");
    				}
    	}
    	return typeCheckingMode;
    }	
	
	public void addEntry(String name, IncludeEntry entry){
		entries.put(name, entry);
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new SignatureHandler(this), CodeType.BASIC, "Declaration", "Signature", null);
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
}
