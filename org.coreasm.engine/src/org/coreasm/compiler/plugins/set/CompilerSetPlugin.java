package org.coreasm.compiler.plugins.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.set.SetBackgroundElement;
import org.coreasm.engine.plugins.set.SetCardinalityFunctionElement;
import org.coreasm.engine.plugins.set.SetCompNode;
import org.coreasm.engine.plugins.set.SetElement;
import org.coreasm.engine.plugins.set.ToSetFunctionElement;
import org.coreasm.engine.plugins.set.TrueGuardNode;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerSetPlugin implements CompilerPlugin, CompilerVocabularyExtender, CompilerCodeRPlugin, CompilerOperatorPlugin{

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
	public CodeFragment rCode(ASTNode n)
			throws CompilerException {		
		if(n.getGrammarClass().equals("Expression")){
			if(n.getGrammarRule().equals("SetEnumerate")){
				CodeFragment result = new CodeFragment("");
				for(ASTNode c : n.getAbstractChildNodes()){
					result.appendFragment(CoreASMCompiler.getEngine().compile(c, CodeType.R));
				}
				result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,slist)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
				for(int i = 0; i < n.getAbstractChildNodes().size(); i++){
					result.appendLine("@slist@.add((CompilerRuntime.Element)evalStack.pop());\n");
				}
				result.appendLine("evalStack.push(new plugins.SetPlugin.SetElement(@slist@));\n");
				return result;
			}
			else if(n.getGrammarRule().equals("SetComprehension")){
				//set comprehension was changed in a newer update of CoreASM.
				//there are no longer two different set comprehension node types
				

				//evaluates a set comprehension of the form
				//{id | id in value with guard}
				//{id is exp | id1 in value1, ... idn in value n with guard}
				//where the guard is optional.
				//in the first case, the exp is simply id
				
				SetCompNode cnode = (SetCompNode) n;
				
				//get the exp
				CodeFragment expr = CoreASMCompiler.getEngine().compile(cnode.getSetFunction(), CodeType.R);
				//guard might be non existent, so initialize it
				CodeFragment guard = null;
				//optimization: the true guard is always true anyway, so if it is existent, leave it out
				if(!(cnode.getGuard() instanceof TrueGuardNode)) guard = CoreASMCompiler.getEngine().compile(cnode.getGuard(), CodeType.R);

				CodeFragment result = new CodeFragment("");
				List<String> constrnames = new ArrayList<String>();
				
				result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
				try{
					//evaluate all constrainer domains and collect the list of variable names
					int counter = 0;
					for(Entry<String, ASTNode> e: cnode.getVarBindings().entrySet()){
						constrnames.add(e.getKey());
						result.appendFragment(CoreASMCompiler.getEngine().compile(e.getValue(), CodeType.R));
						result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,domain" + counter + ")=new java.util.ArrayList<CompilerRuntime.Element>(((CompilerRuntime.Enumerable)evalStack.pop()).enumerate());\n");
						counter++;
					}	
					//iterate
					
					//open for loops
					for(int i = 0; i < constrnames.size(); i++){
						String var = "@domain" + i + "@";
						String cvar = "@c" + i + "@";
						result.appendLine("for(@decl(int,c" + i + ")=0; " + cvar + " < " + var + ".size(); " + cvar + "++){\n");
					}
					
					result.appendLine("localStack.pushLayer();\n");
					
					for(int i = 0; i < constrnames.size(); i++){
						result.appendLine("localStack.put(\"" + constrnames.get(i) + "\", @domain" + i + "@.get(@c" + i + "@));\n");
					}
					
					if(guard == null){
						result.appendFragment(expr);
						result.appendLine("@list@.add((CompilerRuntime.Element)evalStack.pop());\n");
					}
					else{
						result.appendFragment(guard);
						result.appendLine("if(evalStack.pop().equals(CompilerRuntime.BooleanElement.TRUE)){\n");
						result.appendFragment(expr);
						result.appendLine("@list@.add((CompilerRuntime.Element)evalStack.pop());\n");
						result.appendLine("}\n");
					}
					
					result.appendLine("localStack.popLayer();\n");
					
					//close for loops
					for(int i = 0; i < constrnames.size(); i++){
						result.appendLine("}\n");
					}
					result.appendLine("evalStack.push(new plugins.SetPlugin.SetElement(@list@));\n");
					
					return result;
				}
				catch(EngineException exc){
					throw new CompilerException(exc);
				}
			}
		}
		
		throw new CompilerException(
				"unhandled code type: (SetPlugin, rCode, "
						+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	
	
	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException {
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		
		if(enginePath == null){
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.numberplugin.include.NumberElement", "plugins.NumberPlugin.NumberElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.AbstractSetElement", "plugins.CollectionPlugin.AbstractSetElement");
			classLibrary.addPackageReplacement("org.coreasm.compiler.dummy.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
			
			try{
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\setplugin\\include\\SetBackgroundElement.java", 
						this), EntryType.BACKGROUND, "SET"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\setplugin\\include\\SetCardinalityFunctionElement.java", 
						this), EntryType.FUNCTION, "setCardinality"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\setplugin\\include\\SetElement.java", 
						this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\setplugin\\include\\ToSetFunctionElement.java", 
						this), EntryType.FUNCTION, "toSet"));
				result.add(new MainFileEntry(classLibrary.includeClass(
						"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\setplugin\\include\\SetAggregator.java", 
						this), EntryType.AGGREGATOR, ""));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
		}
		else{
			try {
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetElement", "plugins.SetPlugin.SetElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractSetElement", "plugins.CollectionPlugin.AbstractSetElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.set.SetBackgroundElement", "plugins.SetPlugin.SetBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
				
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
}
