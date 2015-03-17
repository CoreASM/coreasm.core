package org.coreasm.compiler.plugins.set;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.plugins.set.code.rcode.ComprehensionHandler;
import org.coreasm.compiler.plugins.set.code.rcode.EnumerateHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.set.SetBackgroundElement;
import org.coreasm.engine.plugins.set.SetCardinalityFunctionElement;
import org.coreasm.engine.plugins.set.ToSetFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

/**
 * Provides sets for the compiler
 * @author Spellmaker
 *
 */
public class CompilerSetPlugin extends CompilerCodePlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerOperatorPlugin{

	private Plugin interpreterPlugin;
	
	/**
	 * Constructs a new plugin
	 * @param parent The interpreter version
	 */
	public CompilerSetPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public String getName() {
		return "SetPlugin";
	}

	@Override
	public List<String> unaryOperations() {
		List<String> result = new ArrayList<String>();
		
		return result;
	}

	@Override
	public List<String> binaryOperations() {
		List<String> result = new ArrayList<String>();
		result.add("union");
		result.add("intersect");
		result.add("diff");
		result.add("subset");
		return result;
	}

	@Override
	public String compileBinaryOperator(String token)
			throws CompilerException {
		String result = "";
		String abstractsetelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "AbstractSetElement", "CollectionPlugin");
		String setelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "SetElement", "SetPlugin");
		
		result = "if((@lhs@ instanceof " + abstractsetelement + ") && (@rhs@ instanceof " + abstractsetelement + ")){\n";
		result = result
				+ "@decl(" + abstractsetelement + ",set1)=(" + abstractsetelement + ")@lhs@;\n"
				+ "@decl(" + abstractsetelement + ",set2)=(" + abstractsetelement + ")@rhs@;\n";
		
		if(token.equals("union")){
			result += "@decl(java.util.List<@RuntimePkg@.Element>,result)=new java.util.ArrayList<@RuntimePkg@.Element>();\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el1)=new java.util.ArrayList<@RuntimePkg@.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el2)=new java.util.ArrayList<@RuntimePkg@.Element>(@set2@.enumerate());\n";
			result += "for(@decl(@RuntimePkg@.Element,e) : @el1@){\n";
			result += "@result@.add(@e@);\n";
			result += "}\n";
			result += "for(@decl(@RuntimePkg@.Element,e2) : @el2@){\n";
			result += "@result@.add(@e2@);\n";
			result += "}\n";
			result += "evalStack.push(new " + setelement + "(@result@));\n";
		}
		else if(token.equals("intersect")){
			result += "@decl(java.util.List<@RuntimePkg@.Element>,result)=new java.util.ArrayList<@RuntimePkg@.Element>();\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el1)=new java.util.ArrayList<@RuntimePkg@.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el2)=new java.util.ArrayList<@RuntimePkg@.Element>(@set2@.enumerate());\n";
			result += "for(@decl(@RuntimePkg@.Element,e) : @el1@){\n";
			result += "if(@el2@.contains(@e@)){\n";
			result += "@result@.add(@e@);\n";
			result += "}\n";
			result += "}\n";
			result += "evalStack.push(new " + setelement + "(@result@));\n";
		}
		else if(token.equals("diff")){
			result += "@decl(java.util.List<@RuntimePkg@.Element>,result)=new java.util.ArrayList<@RuntimePkg@.Element>();\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el1)=new java.util.ArrayList<@RuntimePkg@.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el2)=new java.util.ArrayList<@RuntimePkg@.Element>(@set2@.enumerate());\n";
			result += "for(@decl(CompilerRuntime.Element,e) : @el1@){\n";
			result += "if(!@el2@.contains(@e@)){\n";
			result += "@result@.add(@e@);\n";
			result += "}\n";
			result += "}\n";
			result += "evalStack.push(new " + setelement + "(@result@));\n";
		}
		else if(token.equals("subset")){
			result += "@decl(java.util.List<@RuntimePkg@.Element>,result)=new java.util.ArrayList<@RuntimePkg@.Element>();\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el1)=new java.util.ArrayList<@RuntimePkg@.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<@RuntimePkg@.Element>,el2)=new java.util.ArrayList<@RuntimePkg@.Element>(@set2@.enumerate());\n";
			result += "evalStack.push(@RuntimePkg@.BooleanElement.valueOf(@el2@.containsAll(@el1@)));\n";
		}
		else{
			throw new CompilerException("unknown operator call: SetPlugin, "
					+ token);
		}

		result += "}\n";
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
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetBackgroundElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "SetBackgroundElement", "SetPlugin"));
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "SetElement", "SetPlugin"));
				
				result = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/engine/plugins/set/SetBackgroundElement.java", EntryType.BACKGROUND, SetBackgroundElement.SET_BACKGROUND_NAME).
						includeStatic("org/coreasm/compiler/plugins/set/include/SetCardinalityFunctionElement.java", EntryType.FUNCTION, SetCardinalityFunctionElement.SET_CARINALITY_FUNCTION_NAME).
						includeStatic("org/coreasm/compiler/plugins/set/include/SetElement.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/engine/plugins/set/ToSetFunctionElement.java", EntryType.FUNCTION, ToSetFunctionElement.NAME).
						includeStatic("org/coreasm/compiler/plugins/set/include/SetAggregator.java", EntryType.AGGREGATOR).
						build();
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		return result;
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new EnumerateHandler(), CodeType.R, "Expression", "SetEnumerate", null);
		register(new ComprehensionHandler(), CodeType.R, "Expression", "SetComprehension", null);
	}
}
