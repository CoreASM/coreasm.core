package org.coreasm.compiler.plugins.collection;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.classlibrary.ClassInclude;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.collection.FilterFunctionElement;
import org.coreasm.engine.plugins.collection.MapFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerCollectionPlugin implements CompilerCodeUPlugin,
		CompilerFunctionPlugin, CompilerVocabularyExtender, CompilerPlugin {

	private Plugin interpreterPlugin;
	
	public CompilerCollectionPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	private final String FilterName = "filter";
	private final String FoldName = "fold";
	private final String FoldLName = "foldl";
	private final String FoldRName = "foldr";
	private final String MapName = "map";

	@Override
	public String getName() {
		return "CollectionPlugin";
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		ClassLibrary library = CoreASMCompiler.getEngine().getClassLibrary();
		
		
		String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
		if(enginePath == null){
			try {
				// these classes need a replacement import
				ClassInclude include = library
						.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\AbstractBagElement.java",
								this);
				include.addImportReplacement(
						"org.coreasm.compiler.dummy.numberplugin.include.NumberElement",
						"plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(include, EntryType.INCLUDEONLY, ""));
	
				include = library
						.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\AbstractListElement.java",
								this);
				include.addImportReplacement(
						"org.coreasm.compiler.dummy.numberplugin.include.NumberElement",
						"plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(include, EntryType.INCLUDEONLY, ""));
	
				include = library
						.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\ModifiableIndexedCollection.java",
								this);
				include.addImportReplacement(
						"org.coreasm.compiler.dummy.numberplugin.include.NumberElement",
						"plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(include, EntryType.INCLUDEONLY, ""));
				// add the other classes
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\AbstractMapElement.java",
								this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\AbstractSetElement.java",
								this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\ModifiableCollection.java",
								this), EntryType.INCLUDEONLY, ""));
	
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\CollectionFunctionElement.java",
								this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\FilterFunctionElement.java",
								this), EntryType.FUNCTION, FilterName));
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\FoldFunctionElement.java",
								this), EntryType.FUNCTION, FoldName));
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\FoldFunctionElement.java",
								this), EntryType.FUNCTION, FoldLName));
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\FoldrFunctionElement.java",
								this), EntryType.FUNCTION, FoldRName));
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\collection\\include\\MapFunctionElement.java",
								this), EntryType.FUNCTION, MapName));
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		else{
			
			try {
				//add package replacements for imported classes which can be used by other plugins
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractBagElement", "plugins.CollectionPlugin.AbstractBagElement");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractListElement", "plugins.CollectionPlugin.AbstractListElement");
				library.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableIndexedCollection", "plugins.CollectionPlugin.ModifiableIndexedCollection");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractMapElement", "plugins.CollectionPlugin.AbstractMapElement");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.AbstractSetElement", "plugins.CollectionPlugin.AbstractSetElement");
				library.addPackageReplacement("org.coreasm.compiler.plugins.collection.include.ModifiableCollection", "plugins.CollectionPlugin.ModifiableCollection");
				library.addPackageReplacement("org.coreasm.engine.plugins.collection.CollectionFunctionElement", "plugins.CollectionPlugin.CollectionFunctionElement");
				
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractBagElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractListElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/ModifiableIndexedCollection.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractMapElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/AbstractSetElement.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/ModifiableCollection.java", this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/CollectionFunctionElement.java", this), EntryType.INCLUDEONLY, ""));
				
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/FilterFunctionElement.java", this), EntryType.FUNCTION_CAPI, FilterFunctionElement.NAME));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/FoldFunctionElement.java", this), EntryType.FUNCTION_CAPI, FoldLName));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/FoldFunctionElement.java", this), EntryType.FUNCTION_CAPI, FoldName));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/compiler/plugins/collection/include/FoldrFunctionElement.java", this), EntryType.FUNCTION_CAPI, FoldRName));
				result.add(new MainFileEntry(library.includeClass(enginePath, "org/coreasm/engine/plugins/collection/MapFunctionElement.java", this), EntryType.FUNCTION_CAPI, MapFunctionElement.NAME));
			} catch (IncludeException e) {
				throw new CompilerException(e);
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}
		}
		return result;
	}

	@Override
	public List<String> getCompileFunctionNames() {
		List<String> result = new ArrayList<String>();
		// result.add("map");
		// result.add("filter");
		// result.add("fold");
		// result.add("foldr");
		// result.add("foldl");
		return result;
	}

	@Override
	public CodeFragment compileFunctionCall(ASTNode n) throws CompilerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CodeFragment uCode(ASTNode n) throws CompilerException {

		List<ASTNode> children = n.getAbstractChildNodes();

		if (n.getGrammarClass().equals("Rule")) {
			if (n.getGrammarRule().equals("AddToCollectionRule")) {
				CodeFragment lhs = CoreASMCompiler.getEngine().compile(
						children.get(0), CodeType.R);
				CodeFragment rhs = CoreASMCompiler.getEngine().compile(
						children.get(1), CodeType.L);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(lhs);
				result.appendFragment(rhs);
				result.appendLine("@decl(CompilerRuntime.Location, loc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.Element, el) = (CompilerRuntime.Element) evalStack.pop();\n");
				result.appendLine("@decl(plugins.CollectionPlugin.ModifiableCollection, coll) = (plugins.CollectionPlugin.ModifiableCollection)CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(@loc@);\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList, ul) = new CompilerRuntime.UpdateList();\n");
				result.appendLine("@ul@.addAll(@coll@.computeAddUpdate(@loc@, @el@, this));\n");
				result.appendLine("evalStack.push(@ul@);\n");

				return result;
			} else if (n.getGrammarRule().equals("RemoveFromCollectionRule")) {
				CodeFragment lhs = CoreASMCompiler.getEngine().compile(
						children.get(0), CodeType.R);
				CodeFragment rhs = CoreASMCompiler.getEngine().compile(
						children.get(1), CodeType.L);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(lhs);
				result.appendFragment(rhs);
				result.appendLine("@decl(CompilerRuntime.Location, loc)=(CompilerRuntime.Location)evalStack.pop();\n");
				result.appendLine("@decl(CompilerRuntime.Element, el) = (CompilerRuntime.Element) evalStack.pop();\n");
				result.appendLine("@decl(plugins.CollectionPlugin.ModifiableCollection, coll) = (plugins.CollectionPlugin.ModifiableCollection)CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(@loc@);\n");
				result.appendLine("@decl(CompilerRuntime.UpdateList, ul) = new CompilerRuntime.UpdateList();\n");
				result.appendLine("@ul@.addAll(@coll@.computeRemoveUpdate(@loc@, @el@, this));\n");
				result.appendLine("evalStack.push(@ul@);\n");

				return result;
			}
		}

		throw new CompilerException(
				"unhandled code type: (CollectionPlugin, uCode, "
						+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}
}
