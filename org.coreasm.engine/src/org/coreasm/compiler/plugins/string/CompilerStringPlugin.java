package org.coreasm.compiler.plugins.string;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.ClassLibrary;
import org.coreasm.compiler.components.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.mainprogram.EntryType;
import org.coreasm.compiler.components.mainprogram.MainFileEntry;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.plugins.string.code.rcode.StringTermHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.string.StringLengthFunctionElement;
import org.coreasm.engine.plugins.string.StringMatchingFunction;
import org.coreasm.engine.plugins.string.StringPlugin;
import org.coreasm.engine.plugins.string.StringSubstringFunction;
import org.coreasm.engine.plugins.string.ToStringFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerMakroProvider;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

/**
 * Provides strings to the compiler
 * @author Spellmaker
 *
 */
public class CompilerStringPlugin extends CompilerCodePlugin implements CompilerOperatorPlugin,
		CompilerVocabularyExtender, CompilerFunctionPlugin, CompilerMakroProvider {

	private Plugin interpreterPlugin;
	
	/**
	 * Constructs the plugin
	 * @param parent The interpreter version
	 */
	public CompilerStringPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	@Override
	public String getName() {
		return StringPlugin.PLUGIN_NAME;
	}

	@Override
	public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
			throws CompilerException {
		List<MainFileEntry> result = new ArrayList<MainFileEntry>();
		//ClassLibrary library = engine.getClassLibrary();
	
		try {
			File enginePath = engine.getOptions().enginePath;
			if(enginePath == null){
				engine.getLogger().error(getClass(), "loading classes from a directory is currently not supported");
				throw new CompilerException("could not load classes");
			}
			else{
				//load classes from jar archive
				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.NumberElement", "plugins.NumberPlugin.NumberElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.string.StringBackgroundElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringBackgroundElement", "StringPlugin"));
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.string.StringElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringElement", "StringPlugin"));
				
				result = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/engine/plugins/string/StringBackgroundElement.java", EntryType.BACKGROUND, "STRING").
						includeStatic("org/coreasm/engine/plugins/string/StringElement.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/engine/plugins/string/ToStringFunctionElement.java", EntryType.FUNCTION, ToStringFunctionElement.TOSTRING_FUNC_NAME).
						includeStatic("org/coreasm/engine/plugins/string/StringLengthFunctionElement.java", EntryType.FUNCTION, StringLengthFunctionElement.STRLENGTH_FUNC_NAME).
						includeStatic("org/coreasm/engine/plugins/string/StringSubstringFunction.java", EntryType.FUNCTION, StringSubstringFunction.STRING_SUBSTRING_FUNCTION_NAME).
						includeStatic("org/coreasm/engine/plugins/string/StringMatchingFunction.java", EntryType.FUNCTION_CAPI, StringMatchingFunction.STRING_MATCHES_FUNCTION_NAME).
						build();
			}
		} catch (EntryAlreadyExistsException e) {
			throw new CompilerException(e);
		}

		return result;
	}

	@Override
	public List<String> unaryOperations() {
		return new ArrayList<String>();
	}

	@Override
	public List<String> binaryOperations() {
		List<String> result = new ArrayList<String>();

		result.add("+");

		return result;
	}

	@Override
	public String compileBinaryOperator(String token) throws CompilerException {
		String result = "";
		String stringelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringElement", "StringPlugin");
		if (token.equals("+")) {
			result += "if((@lhs@ instanceof " + stringelement + ") || (@rhs@ instanceof " + stringelement + ")){\n";
			result += "evalStack.push(new " + stringelement + "(@lhs@.toString() + @rhs@.toString()));\n";
			result += "}\n";
		} else
			throw new CompilerException("unkown operator: StringPlugin, "
					+ token);

		result = result + " else ";

		return result;
	}

	@Override
	public String compileUnaryOperator(String token) throws CompilerException {
		throw new CompilerException("unkown operator: StringPlugin, " + token);
	}

	@Override
	public List<String> getCompileFunctionNames() {
		List<String> result = new ArrayList<String>();
		//result.add("strlen");
		//result.add("matches");
		//result.add("stringSubstring");
		//result.add("toString");
		return result;
	}

	@Override
	public CodeFragment compileFunctionCall(ASTNode n) throws CompilerException {
		String stringelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringElement", "StringPlugin");
		String numberelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberElement", "NumberPlugin");
		List<ASTNode> children = n.getAbstractChildNodes();
		String fname = children.get(0).getToken();
		if (fname.equals("strlen")) {
			if (children.size() != 2)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);

			CodeFragment result = new CodeFragment("");
			result.appendFragment(engine.compile(
					children.get(1), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(" + stringelement + ", tmp1)=(" + stringelement + ")evalStack.pop();\n");
			result.appendLine("evalStack.push(@tmp1@.toString().length());\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		} else if (fname.equals("matches")) {
			if (children.size() != 3)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);
			CodeFragment result = new CodeFragment("");
			result.appendFragment(engine.compile(
					children.get(1), CodeType.R));
			result.appendFragment(engine.compile(
					children.get(2), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(" + stringelement + ", regex)=(" + stringelement + ")evalStack.pop();\n");
			result.appendLine("@decl(" + stringelement + ", value)=(" + stringelement + ")evalStack.pop();\n");
			result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(@value@.matches(@regex@)));\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		}
		else if (StringSubstringFunction.STRING_SUBSTRING_FUNCTION_NAME.equals(fname)) {
			if (children.size() != 4)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);
			CodeFragment result = new CodeFragment("");
			result.appendFragment(engine.compile(
					children.get(1), CodeType.R));
			result.appendFragment(engine.compile(
					children.get(2), CodeType.R));
			result.appendFragment(engine.compile(
					children.get(3), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(" + numberelement + ", value2)=(" + numberelement + ")evalStack.pop();\n");
			result.appendLine("@decl(" + numberelement + ", value1)=(" + numberelement + ")evalStack.pop();\n");
			result.appendLine("@decl" + stringelement + ", str)=(" + stringelement + ")evalStack.pop();\n");
			result.appendLine("if(!@value2@.isInteger() || !@value1@.isInteger()) throw new ClassCastException();\n");
			result.appendLine("evalStack.push(new " + stringelement + "(@str@.toString().substring((int)@value1@.getValue(),(int)@value2@.getValue())));\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		} else if (fname.equals("toString")) {
			if (children.size() != 2)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);
			CodeFragment result = new CodeFragment("");
			result.appendFragment(engine.compile(
					children.get(1), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(@RuntimePkg@.Element, tmp1)=(@RuntimePkg@.Element)evalStack.pop();\n");
			result.appendLine("evalStack.push(new " + stringelement + "(@tmp1@.toString()));\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		}

		throw new CompilerException(
				"unknown function name for plugin NumberPlugin: " + fname);
	}

	@Override
	public void registerCodeHandlers() throws CompilerException {
		register(new StringTermHandler(), CodeType.R, "Expression", "StringTerm", null);
	}

	@Override
	public void init(CompilerEngine engine) {
		this.engine = engine;
	}

	@Override
	public Map<String, String> getMakros() {
		Map<String, String> makros = new HashMap<String, String>();
		makros.put("StringElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "StringElement", this.getName()));
		return makros;
	}
}
