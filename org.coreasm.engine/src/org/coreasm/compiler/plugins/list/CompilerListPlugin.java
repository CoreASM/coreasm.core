package org.coreasm.compiler.plugins.list;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.plugins.list.code.rcode.ListCompHandler;
import org.coreasm.compiler.plugins.list.code.rcode.ListTermHandler;
import org.coreasm.compiler.plugins.list.code.ucode.ShiftRuleHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.list.ConsFunctionElement;
import org.coreasm.engine.plugins.list.DropFunctionElement;
import org.coreasm.engine.plugins.list.FlattenListFunctionElement;
import org.coreasm.engine.plugins.list.HeadLastFunctionElement;
import org.coreasm.engine.plugins.list.IndexesFunctionElement;
import org.coreasm.engine.plugins.list.ListBackgroundElement;
import org.coreasm.engine.plugins.list.NthFunctionElement;
import org.coreasm.engine.plugins.list.ReplicateFunctionElement;
import org.coreasm.engine.plugins.list.ReverseFunctionElement;
import org.coreasm.engine.plugins.list.SetNthFunctionElement;
import org.coreasm.engine.plugins.list.TailFunctionElement;
import org.coreasm.engine.plugins.list.TakeFunctionElement;
import org.coreasm.engine.plugins.list.ToListFunctionElement;
import org.coreasm.engine.plugins.list.ZipFunctionElement;
import org.coreasm.engine.plugins.list.ZipWithFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerListPlugin extends CompilerCodePlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerOperatorPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerListPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	
	@Override
	public String getName() {
		return "ListPlugin";
	}

	@Override
	public List<String> unaryOperations() {
		List<String> result = new ArrayList<String>();
		
		return result;
	}

	@Override
	public List<String> binaryOperations() {
		List<String> result = new ArrayList<String>();

		result.add("+");
		
		return result;
	}

	@Override
	public String compileBinaryOperator(String token)
			throws CompilerException {
		
		String result = "";
		
		if(token.equals("+")){
			result += "if((@lhs@ instanceof plugins.ListPlugin.ListElement) && (@rhs@ instanceof plugins.ListPlugin.ListElement)){\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>(((plugins.ListPlugin.ListElement)@lhs@).values());\n";
			result += "@list@.addAll(((plugins.ListPlugin.ListElement)@rhs@).values());\n";
			result += "evalStack.push(new plugins.ListPlugin.ListElement(@list@));\n";
			result += "}\n";
		}
		else throw new CompilerException("unkown operator: ListPlugin, " + token);
		
		result = result + " else ";
		
		return result;
	}

	@Override
	public String compileUnaryOperator(String token)
			throws CompilerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException {
		
		String enginePathStr = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePathStr == null){
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.AbstractListElement", "plugins.CollectionPlugin.AbstractListElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.ModifiableIndexedCollection", "plugins.CollectionPlugin.ModifiableIndexedCollection");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.numberplugin.include.NumberElement", "plugins.NumberPlugin.NumberElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.numberplugin.include.NumberBackgroundElement", "plugins.NumberPlugin.NumberBackgroundElement");
			
			try{
				File listpluginFolder = new File("src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include".replace("\\", File.separator));

				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ConsFunctionElement.java"),
						this), EntryType.FUNCTION, "cons"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "DropFunctionElement.java"),
						this), EntryType.FUNCTION, "drop"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "FlattenListFunctionElement.java"),
						this), EntryType.FUNCTION, "flattenList"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "HeadFunctionElement.java"),
						this), EntryType.FUNCTION, "head"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "IndexesFunctionElement.java"),
						this), EntryType.FUNCTION, "indices"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "LastFunctionElement.java"),
						this), EntryType.FUNCTION, "last"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ListBackgroundElement.java"),
						this), EntryType.BACKGROUND, "LIST"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ListElement.java"),
						this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ListFunctionElement.java"),
						this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "NthFunctionElement.java"),
						this), EntryType.FUNCTION, "nth"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ReplicateFunctionElement.java"),
						this), EntryType.FUNCTION, "replicate"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ReverseFunctionElement.java"),
						this), EntryType.FUNCTION, "reverse"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "SetNthFunctionElement.java"),
						this), EntryType.FUNCTION, "setnth"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "TailFunctionElement.java"),
						this), EntryType.FUNCTION, "tail"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "TakeFunctionElement.java"),
						this), EntryType.FUNCTION, "take"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ToListFunctionElement.java"),
						this), EntryType.FUNCTION, "toList"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ZipFunctionElement.java"),
						this), EntryType.FUNCTION, "zip"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(listpluginFolder, "ZipWithFunctionElement.java"),
						this), EntryType.FUNCTION, "zipwith"));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
		}
		else{
			try {
				File enginePath = new File(enginePathStr);

				//replacements for packages
				/*classLibrary.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractListElement", "plugins.CollectionPlugin.AbstractListElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.include.NumberElement", "plugins.NumberPlugin.NumberElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.NumberBackgroundElement", "plugins.NumberPlugin.NumberBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableIndexedCollection", "plugins.CollectionPlugin.ModifiableIndexedCollection");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListFunctionElement", "plugins.ListPlugin.ListFunctionElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListBackgroundElement", "plugins.ListPlugin.ListBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListElement", "plugins.ListPlugin.ListElement");*/
				
				//package replacements for classes accessible from other plugins
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListFunctionElement", "plugins.ListPlugin.ListFunctionElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListBackgroundElement", "plugins.ListPlugin.ListBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListElement", "plugins.ListPlugin.ListElement");
				
				
				//elements provided by the plugins include
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/list/include/LastFunctionElement.java", this), EntryType.FUNCTION_CAPI, HeadLastFunctionElement.LAST_FUNC_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/list/include/ListElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/list/include/HeadFunctionElement.java", this), EntryType.FUNCTION_CAPI, HeadLastFunctionElement.HEAD_FUNC_NAME));
				//elements taken from the coreasm files
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ListBackgroundElement.java", this), EntryType.BACKGROUND, ListBackgroundElement.LIST_BACKGROUND_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ListFunctionElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ConsFunctionElement.java", this), EntryType.FUNCTION, ConsFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/DropFunctionElement.java", this), EntryType.FUNCTION_CAPI, DropFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/FlattenListFunctionElement.java", this), EntryType.FUNCTION, FlattenListFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/IndexesFunctionElement.java", this), EntryType.FUNCTION_CAPI, IndexesFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/NthFunctionElement.java", this), EntryType.FUNCTION, NthFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ReplicateFunctionElement.java", this), EntryType.FUNCTION_CAPI, ReplicateFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ReverseFunctionElement.java", this), EntryType.FUNCTION_CAPI, ReverseFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/SetNthFunctionElement.java", this), EntryType.FUNCTION_CAPI, SetNthFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/TailFunctionElement.java", this), EntryType.FUNCTION_CAPI, TailFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/TakeFunctionElement.java", this), EntryType.FUNCTION_CAPI, TakeFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ToListFunctionElement.java", this), EntryType.FUNCTION, ToListFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ZipFunctionElement.java", this), EntryType.FUNCTION_CAPI, ZipFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/list/ZipWithFunctionElement.java", this), EntryType.FUNCTION_CAPI, ZipWithFunctionElement.NAME));
			} catch (IncludeException e) {
				throw new CompilerException(e);
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		
		return result;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new ShiftRuleHandler(), CodeType.U, "Rule", "ShiftRule", null);
		register(new ListTermHandler(), CodeType.R, "Expression", "ListTerm", null);
		register(new ListCompHandler(), CodeType.R, "Expression", "ListComprehension", null);
	}
}
