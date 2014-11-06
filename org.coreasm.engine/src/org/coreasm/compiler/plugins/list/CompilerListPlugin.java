package org.coreasm.compiler.plugins.list;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.engine.interpreter.ASTNode;
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
import org.coreasm.engine.plugins.list.ShiftRuleNode;
import org.coreasm.engine.plugins.list.TailFunctionElement;
import org.coreasm.engine.plugins.list.TakeFunctionElement;
import org.coreasm.engine.plugins.list.ToListFunctionElement;
import org.coreasm.engine.plugins.list.ZipFunctionElement;
import org.coreasm.engine.plugins.list.ZipWithFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerListPlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerCodeRPlugin, CompilerOperatorPlugin, CompilerCodeUPlugin{

	
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
			result += "if((@lhs@ instanceof plugins.ListPlugin.ListElement) || (@rhs@ instanceof plugins.ListPlugin.ListElement)){\n";
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
	public CodeFragment rCode(ASTNode n)
			throws CompilerException {		
		
		if(n.getGrammarClass().equals("Expression")){
			if(n.getGrammarRule().equals("ListTerm")){
				CodeFragment result = new CodeFragment("");
				
				result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
				for(ASTNode child : n.getAbstractChildNodes()){
					result.appendFragment(CoreASMCompiler.getEngine().compile(child, CodeType.R));
					result.appendLine("@list@.add((CompilerRuntime.Element)evalStack.pop());\n");
				}
				
				result.appendLine("evalStack.push(new plugins.ListPlugin.ListElement(@list@));\n");
				
				return result;
			}
			else if(n.getGrammarRule().equals("ShiftRule")){
				
			}
		}
		
		
		throw new CompilerException(
				"unhandled code type: (ListPlugin, rCode, "
						+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException {
		
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath==null){
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.AbstractListElement", "plugins.CollectionPlugin.AbstractListElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.ModifiableIndexedCollection", "plugins.CollectionPlugin.ModifiableIndexedCollection");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.numberplugin.include.NumberElement", "plugins.NumberPlugin.NumberElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.numberplugin.include.NumberBackgroundElement", "plugins.NumberPlugin.NumberBackgroundElement");
			
			
			try{
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ConsFunctionElement.java", 
						this), EntryType.FUNCTION, "cons"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\DropFunctionElement.java", 
						this), EntryType.FUNCTION, "drop"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\FlattenListFunctionElement.java", 
						this), EntryType.FUNCTION, "flattenList"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\HeadFunctionElement.java", 
						this), EntryType.FUNCTION, "head"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\IndexesFunctionElement.java", 
						this), EntryType.FUNCTION, "indices"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\LastFunctionElement.java", 
						this), EntryType.FUNCTION, "last"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ListBackgroundElement.java", 
						this), EntryType.BACKGROUND, "LIST"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ListElement.java", 
						this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ListFunctionElement.java", 
						this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\NthFunctionElement.java", 
						this), EntryType.FUNCTION, "nth"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ReplicateFunctionElement.java", 
						this), EntryType.FUNCTION, "replicate"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ReverseFunctionElement.java", 
						this), EntryType.FUNCTION, "reverse"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\SetNthFunctionElement.java", 
						this), EntryType.FUNCTION, "setnth"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\TailFunctionElement.java", 
						this), EntryType.FUNCTION, "tail"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\TakeFunctionElement.java", 
						this), EntryType.FUNCTION, "take"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ToListFunctionElement.java", 
						this), EntryType.FUNCTION, "toList"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ZipFunctionElement.java", 
						this), EntryType.FUNCTION, "zip"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\listplugin\\include\\ZipWithFunctionElement.java", 
						this), EntryType.FUNCTION, "zipwith"));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
		}
		else{
			try {
				//replacements for packages
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractListElement", "plugins.CollectionPlugin.AbstractListElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.include.NumberElement", "plugins.NumberPlugin.NumberElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.NumberBackgroundElement", "plugins.NumberPlugin.NumberBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableIndexedCollection", "plugins.CollectionPlugin.ModifiableIndexedCollection");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListFunctionElement", "plugins.ListPlugin.ListFunctionElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.list.ListBackgroundElement", "plugins.ListPlugin.ListBackgroundElement");
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
	public CodeFragment uCode(ASTNode n)
			throws CompilerException {

		if(n.getGrammarClass().equals("Rule")){
			if(n.getGrammarRule().equals("ShiftRule")){
				ShiftRuleNode srn = (ShiftRuleNode) n;
				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(srn.getLocationNode(), CodeType.L));
				result.appendFragment(CoreASMCompiler.getEngine().compile(srn.getListNode(), CodeType.LR));
				
				result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>(((plugins.ListPlugin.ListElement)evalStack.pop()).values());\n");
				result.appendLine("@decl(CompilerRuntime.Location,listloc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.Location,loc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList,ulist)=new CompilerRuntime.UpdateList();\n");
				
				if(srn.isLeft){
					result.appendLine("@ulist@.add(new CompilerRuntime.Update(@loc@,@list@.get(0),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible()));\n");
					result.appendLine("@list@.remove(0);\n");
					result.appendLine("@ulist@.add(new CompilerRuntime.Update(@listloc@,new plugins.ListPlugin.ListElement(@list@),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible()));\n");
				}
				else{
					result.appendLine("@ulist@.add(new CompilerRuntime.Update(@loc@,@list@.get(@list@.size() - 1),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible()));\n");
					result.appendLine("@list@.remove(@list@.size() - 1);\n");
					result.appendLine("@ulist@.add(new CompilerRuntime.Update(@listloc@,new plugins.ListPlugin.ListElement(@list@),CompilerRuntime.Update.UPDATE_ACTION,this.getUpdateResponsible()));\n");
				}
				
				result.appendLine("evalStack.push(@ulist@);\n");
				
				return result;
			}
		}
		
		
		throw new CompilerException(
				"unhandled code type: (ListPlugin, uCode, "
						+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
}
