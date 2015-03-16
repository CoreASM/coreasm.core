package org.coreasm.compiler.plugins.list;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
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
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerMakroProvider;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerListPlugin extends CompilerCodePlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerOperatorPlugin, CompilerMakroProvider{

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
		String listelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "ListElement", "ListPlugin");
		if(token.equals("+")){
			result += "if((@lhs@ instanceof " + listelement + ") && (@rhs@ instanceof " + listelement + ")){\n";
			result +="System.out.println(\"executing concat\");\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,list)=new java.util.ArrayList<@RuntimePkg@.Element>(((" + listelement + ")@lhs@).values());\n";
			result += "@list@.addAll(((" + listelement + ")@rhs@).values());\n";
			result += "evalStack.push(new " + listelement + "(@list@));\n";
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
		
		File enginePath = engine.getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
			engine.getLogger().error(getClass(), "loading classes from a directory is currently not supported");
			throw new CompilerException("could not load classes");
		}
		else{
			try {
				//replacements for packages
				/*classLibrary.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractListElement", "plugins.CollectionPlugin.AbstractListElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.include.NumberElement", "plugins.NumberPlugin.NumberElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.NumberBackgroundElement", "plugins.NumberPlugin.NumberBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableIndexedCollection", "plugins.CollectionPlugin.ModifiableIndexedCollection");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListFunctionElement", "plugins.ListPlugin.ListFunctionElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListBackgroundElement", "plugins.ListPlugin.ListBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListElement", "plugins.ListPlugin.ListElement");*/
				
				//package replacements for classes accessible from other plugins
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListFunctionElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "ListFunctionElement", "ListPlugin"));
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListBackgroundElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "ListBackgroundElement", "ListPlugin"));
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "ListElement", "ListPlugin"));
				
				
				//elements provided by the plugins include
				result = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/compiler/plugins/list/include/LastFunctionElement.java", EntryType.FUNCTION_CAPI, HeadLastFunctionElement.LAST_FUNC_NAME).
						includeStatic("org/coreasm/compiler/plugins/list/include/ListElement.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/compiler/plugins/list/include/HeadFunctionElement.java", EntryType.FUNCTION_CAPI, HeadLastFunctionElement.HEAD_FUNC_NAME).
						includeStatic("org/coreasm/engine/plugins/list/ListBackgroundElement.java", EntryType.BACKGROUND, ListBackgroundElement.LIST_BACKGROUND_NAME).
						includeStatic("org/coreasm/engine/plugins/list/ListFunctionElement.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/engine/plugins/list/ConsFunctionElement.java", EntryType.FUNCTION, ConsFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/DropFunctionElement.java", EntryType.FUNCTION_CAPI, DropFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/FlattenListFunctionElement.java", EntryType.FUNCTION, FlattenListFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/IndexesFunctionElement.java", EntryType.FUNCTION_CAPI, IndexesFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/NthFunctionElement.java", EntryType.FUNCTION, NthFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/ReplicateFunctionElement.java", EntryType.FUNCTION_CAPI, ReplicateFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/ReverseFunctionElement.java", EntryType.FUNCTION_CAPI, ReverseFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/SetNthFunctionElement.java", EntryType.FUNCTION_CAPI, SetNthFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/TailFunctionElement.java", EntryType.FUNCTION_CAPI, TailFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/TakeFunctionElement.java", EntryType.FUNCTION_CAPI, TakeFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/ToListFunctionElement.java", EntryType.FUNCTION, ToListFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/ZipFunctionElement.java", EntryType.FUNCTION_CAPI, ZipFunctionElement.NAME).
						includeStatic("org/coreasm/engine/plugins/list/ZipWithFunctionElement.java", EntryType.FUNCTION_CAPI, ZipWithFunctionElement.NAME).
						build();
				LibraryEntry indexes = classLibrary.findEntry("IndexesFunctionElement", "ListPlugin", LibraryEntryType.STATIC);
				result.add(new MainFileEntry(indexes, EntryType.FUNCTION_CAPI, IndexesFunctionElement.NAME_ALTERNATIVE));
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

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}

	@Override
	public Map<String, String> getMakros() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("ListElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "ListElement", getName()));
		return result;
	}
}
