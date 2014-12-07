package org.coreasm.compiler.plugins.set;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.plugins.set.code.rcode.ComprehensionHandler;
import org.coreasm.compiler.plugins.set.code.rcode.EnumerateHandler;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.set.SetBackgroundElement;
import org.coreasm.engine.plugins.set.SetCardinalityFunctionElement;
import org.coreasm.engine.plugins.set.ToSetFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerSetPlugin extends CompilerCodePlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerOperatorPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerSetPlugin(Plugin parent){
		this.interpreterPlugin = parent;
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
		
		result = "if((@lhs@ instanceof plugins.CollectionPlugin.AbstractSetElement) && (@rhs@ instanceof plugins.CollectionPlugin.AbstractSetElement)){\n";
		result = result
				+ "@decl(plugins.CollectionPlugin.AbstractSetElement,set1)=(plugins.CollectionPlugin.AbstractSetElement)@lhs@;\n"
				+ "@decl(plugins.CollectionPlugin.AbstractSetElement,set2)=(plugins.CollectionPlugin.AbstractSetElement)@rhs@;\n";
		
		if(token.equals("union")){
			result += "@decl(java.util.List<CompilerRuntime.Element>,result)=new java.util.ArrayList<CompilerRuntime.Element>();\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el1)=new java.util.ArrayList<CompilerRuntime.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el2)=new java.util.ArrayList<CompilerRuntime.Element>(@set2@.enumerate());\n";
			result += "for(@decl(CompilerRuntime.Element,e) : @el1@){\n";
			result += "@result@.add(@e@);\n";
			result += "}\n";
			result += "for(@decl(CompilerRuntime.Element,e2) : @el2@){\n";
			result += "@result@.add(@e2@);\n";
			result += "}\n";
			result += "evalStack.push(new plugins.SetPlugin.SetElement(@result@));\n";
		}
		else if(token.equals("intersect")){
			result += "@decl(java.util.List<CompilerRuntime.Element>,result)=new java.util.ArrayList<CompilerRuntime.Element>();\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el1)=new java.util.ArrayList<CompilerRuntime.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el2)=new java.util.ArrayList<CompilerRuntime.Element>(@set2@.enumerate());\n";
			result += "for(@decl(CompilerRuntime.Element,e) : @el1@){\n";
			result += "if(@el2@.contains(@e@)){\n";
			result += "@result@.add(@e@);\n";
			result += "}\n";
			result += "}\n";
			result += "evalStack.push(new plugins.SetPlugin.SetElement(@result@));\n";
		}
		else if(token.equals("diff")){
			result += "@decl(java.util.List<CompilerRuntime.Element>,result)=new java.util.ArrayList<CompilerRuntime.Element>();\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el1)=new java.util.ArrayList<CompilerRuntime.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el2)=new java.util.ArrayList<CompilerRuntime.Element>(@set2@.enumerate());\n";
			result += "for(@decl(CompilerRuntime.Element,e) : @el1@){\n";
			result += "if(!@el2@.contains(@e@)){\n";
			result += "@result@.add(@e@);\n";
			result += "}\n";
			result += "}\n";
			result += "evalStack.push(new plugins.SetPlugin.SetElement(@result@));\n";
		}
		else if(token.equals("subset")){
			result += "@decl(java.util.List<CompilerRuntime.Element>,result)=new java.util.ArrayList<CompilerRuntime.Element>();\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el1)=new java.util.ArrayList<CompilerRuntime.Element>(@set1@.enumerate());\n";
			result += "@decl(java.util.List<CompilerRuntime.Element>,el2)=new java.util.ArrayList<CompilerRuntime.Element>(@set2@.enumerate());\n";
			result += "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@el2@.containsAll(@el1@)));\n";
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
		String enginePathStr = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePathStr == null){
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.numberplugin.include.NumberElement", "plugins.NumberPlugin.NumberElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.AbstractSetElement", "plugins.CollectionPlugin.AbstractSetElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
			
			try{
				File setpluginFolder = new File("src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\setplugin\\include".replace("\\", File.separator));

				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(setpluginFolder, "SetBackgroundElement.java"),
						this), EntryType.BACKGROUND, "SET"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(setpluginFolder, "SetCardinalityFunctionElement.java"),
						this), EntryType.FUNCTION, "setCardinality"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(setpluginFolder, "SetElement.java"),
						this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(setpluginFolder, "ToSetFunctionElement.java"),
						this), EntryType.FUNCTION, "toSet"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						new File(setpluginFolder, "SetAggregator.java"),
						this), EntryType.AGGREGATOR, ""));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
		}
		else{
			try {
				File enginePath = new File(enginePathStr);

				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractSetElement", "plugins.CollectionPlugin.AbstractSetElement");
				//classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
				
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetBackgroundElement", "plugins.SetPlugin.SetBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetElement", "plugins.SetPlugin.SetElement");
				
				
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/set/SetBackgroundElement.java", this), EntryType.BACKGROUND, SetBackgroundElement.SET_BACKGROUND_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/set/include/SetCardinalityFunctionElement.java", this), EntryType.FUNCTION, SetCardinalityFunctionElement.SET_CARINALITY_FUNCTION_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/set/include/SetElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/set/ToSetFunctionElement.java", this), EntryType.FUNCTION, ToSetFunctionElement.NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/set/include/SetAggregator.java", this), EntryType.AGGREGATOR, ""));
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
		register(new EnumerateHandler(), CodeType.R, "Expression", "SetEnumerate", null);
		register(new ComprehensionHandler(), CodeType.R, "Expression", "SetComprehension", null);
	}
}
