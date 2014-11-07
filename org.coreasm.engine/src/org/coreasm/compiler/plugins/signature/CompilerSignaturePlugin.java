package org.coreasm.compiler.plugins.signature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.classlibrary.ClassInclude;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.mainprogram.statemachine.EngineTransition;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.signature.FunctionDomainFunctionElement;
import org.coreasm.engine.plugins.signature.FunctionRangeFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeBPlugin;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerSignaturePlugin implements CompilerPlugin, CompilerCodeBPlugin,
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
	
    private enum SignatureEntryType {UNIVERSE, ENUM, DERIVED, FUNCTION};
    
    private class IncludeEntry{
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
		
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
		
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.listplugin.include.ListElement", "plugins.ListPlugin.ListElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.setplugin.include.SetElement", "plugins.SetPlugin.SetElement");
			
			
			try{
				result.add(new MainFileEntry(
						classLibrary.includeClass(ClassInclude.PLUGIN_BASE + "signatureplugin\\include\\EnumerationBackgroundElement.java",
								this), EntryType.INCLUDEONLY,""));
				result.add(new MainFileEntry(
						classLibrary.includeClass(ClassInclude.PLUGIN_BASE + "signatureplugin\\include\\EnumerationElement.java",
								this), EntryType.INCLUDEONLY,""));
				result.add(new MainFileEntry(
						classLibrary.includeClass(ClassInclude.PLUGIN_BASE + "signatureplugin\\include\\FunctionDomainFunctionElement.java",
								this), EntryType.FUNCTION,"domain"));
				result.add(new MainFileEntry(
						classLibrary.includeClass(ClassInclude.PLUGIN_BASE + "signatureplugin\\include\\FunctionRangeFunctionElement.java",
								this), EntryType.FUNCTION,"range"));
				
				
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
		}
		else{
			try {
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/signature/EnumerationBackgroundElement.java", this), EntryType.INCLUDEONLY, ""));
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
	public void bCode(ASTNode n)
			throws CompilerException {
		if(n.getGrammarClass().equals("Declaration")){
			if(n.getGrammarRule().equals("Signature")){
				//get the actual root node of the declaration
				ASTNode root = n.getAbstractChildNodes().get(0);
				if(root.getGrammarRule().equals("UniverseDefinition")){
					parseUniverse(root);
					return;
				}
				else if(root.getGrammarRule().equals("EnumerationDefinition")){
					parseEnum(root);
					return;
				}
				else if(root.getGrammarRule().equals("FunctionSignature")){
					parseFunction(root);
					return;
				}
				else if(root.getGrammarRule().equals("DerivedFunctionDeclaration")){
					parseDerivedFunction(root);
					return;
				}
			}
		}
		

		throw new CompilerException("unhandled code type: (SignaturePlugin, bCode, " + n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	private void parseDerivedFunction(ASTNode root) throws CompilerException {
		ASTNode signature = root.getAbstractChildNodes().get(0);
		CodeFragment body = CoreASMCompiler.getEngine().compile(root.getAbstractChildNodes().get(1), CodeType.R);
		
		String name = signature.getAbstractChildNodes().get(0).getToken();
		String[] params = new String[signature.getAbstractChildNodes().size() - 1];
		for(int i = 1; i < signature.getAbstractChildNodes().size(); i++){
			params[i - 1] = signature.getAbstractChildNodes().get(i).getToken();
		}
		
		entries.put(name, new IncludeEntry(SignatureEntryType.DERIVED, new DerivedFunctionEntry(name, params, body)));
		//derived.put(name, new DerivedFunctionEntry(name, params, body));
	}

	private void parseFunction(ASTNode root) throws CompilerException {
		//first node is either the function id or the function class
		String name = null;
		String fclass = null;
		int pos = 0;
		List<ASTNode> children = root.getAbstractChildNodes();
		if(children.get(0).getGrammarRule().equals("ID")){
			name = children.get(0).getToken();
			pos = 1;
		}
		else{
			fclass = children.get(0).getToken();
			String tmp = fclass.substring(0, 1);
			fclass = tmp.toUpperCase() + fclass.substring(1);
			name = children.get(1).getToken();
			pos = 2;
		}
		
		
		//next node holds the lefthand side
		List<String> domain = new ArrayList<String>();
		if(children.get(pos).getGrammarRule().equals("UniverseTuple")){
			for(ASTNode n : children.get(pos).getAbstractChildNodes()){
				domain.add(n.getToken());
			}
			pos++;
		}
		//righthand side
		String range = children.get(pos).getToken();
		CodeFragment init = null;
		if(children.size() > pos + 1){
			init = CoreASMCompiler.getEngine().compile(children.get(pos + 1), CodeType.R);
		}
		
		//add the function element
		entries.put(name, new IncludeEntry(SignatureEntryType.FUNCTION, new FunctionEntry(name, fclass, domain, range, init)));
		//functions.put(name, new FunctionEntry(name, fclass, domain, range, init));
	}

	private void parseEnum(ASTNode root) {
		String name = root.getAbstractChildNodes().get(0).getToken();
		String[] elements = new String[root.getAbstractChildNodes().size() - 1];
		for(int i = 1; i < root.getAbstractChildNodes().size(); i++){
			elements[i - 1] = root.getAbstractChildNodes().get(i).getToken();
		}

		entries.put(name, new IncludeEntry(SignatureEntryType.ENUM, new EnumBackgroundEntry(name, elements)));
		//enums.put(name, new EnumBackgroundEntry(name, elements));
	}

	private void parseUniverse(ASTNode root) {
		String name = root.getAbstractChildNodes().get(0).getToken();
		String[] elements = new String[root.getAbstractChildNodes().size() - 1];
		for(int i = 1; i < root.getAbstractChildNodes().size(); i++){
			elements[i - 1] = root.getAbstractChildNodes().get(i).getToken();
		}
		
		entries.put(name, new IncludeEntry(SignatureEntryType.UNIVERSE, new UniverseEntry(name, elements)));
		//universes.put(name, new UniverseEntry(name, elements));
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
    	String mode = CoreASMCompiler.getEngine().getOptions().properties.get("Signature.TypeChecking");
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
}
