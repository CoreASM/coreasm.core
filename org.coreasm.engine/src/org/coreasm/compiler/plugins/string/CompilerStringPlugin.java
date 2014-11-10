package org.coreasm.compiler.plugins.string;

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
import org.coreasm.engine.plugins.string.StringLengthFunctionElement;
import org.coreasm.engine.plugins.string.StringMatchingFunction;
import org.coreasm.engine.plugins.string.StringPlugin;
import org.coreasm.engine.plugins.string.StringSubstringFunction;
import org.coreasm.engine.plugins.string.ToStringFunctionElement;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;

public class CompilerStringPlugin implements CompilerOperatorPlugin,
		CompilerVocabularyExtender, CompilerCodeRPlugin, CompilerFunctionPlugin {

	private Plugin interpreterPlugin;
	
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
		ClassLibrary library = CoreASMCompiler.getEngine().getClassLibrary();
	
		try {
			
			String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
			if(enginePath == null){
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\stringplugin\\include\\StringBackgroundElement.java",
								this), EntryType.BACKGROUND, "STRING"));
	
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\stringplugin\\include\\StringElement.java",
								this), EntryType.INCLUDEONLY, ""));
	
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\stringplugin\\include\\ToStringFunctionElement.java",
								this), EntryType.FUNCTION, "toString"));
	
				ClassInclude tmp = library
						.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\stringplugin\\include\\StringLengthFunctionElement.java",
								this);
				tmp.addImportReplacement(
						"org.coreasm.compiler.dummy.numberplugin.include.NumberElement",
						"plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(tmp, EntryType.FUNCTION, "strlen"));
	
				tmp = library
						.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\stringplugin\\include\\StringSubstringFunction.java",
								this);
				tmp.addImportReplacement(
						"org.coreasm.compiler.dummy.numberplugin.include.NumberElement",
						"plugins.NumberPlugin.NumberElement");
				result.add(new MainFileEntry(tmp, EntryType.FUNCTION,
						"stringSubstring"));
	
				result.add(new MainFileEntry(
						library.includeClass(
								"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\stringplugin\\include\\StringMatchingFunction.java",
								this), EntryType.FUNCTION, "matches"));
			}
			else{
				//load classes from jar archive
				//classLibrary.addPackageReplacement("org.coreasm.engine.plugins.number.NumberElement", "plugins.NumberPlugin.NumberElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.string.StringBackgroundElement", "plugins.StringPlugin.StringBackgroundElement");
				classLibrary.addPackageReplacement("org.coreasm.engine.plugins.string.StringElement", "plugins.StringPlugin.StringElement");
				
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/string/StringBackgroundElement.java", this),EntryType.BACKGROUND, "STRING"));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/string/StringElement.java", this),EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/string/ToStringFunctionElement.java", this),EntryType.FUNCTION, ToStringFunctionElement.TOSTRING_FUNC_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/string/StringLengthFunctionElement.java", this),EntryType.FUNCTION, StringLengthFunctionElement.STRLENGTH_FUNC_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/string/StringSubstringFunction.java", this),EntryType.FUNCTION, StringSubstringFunction.STRING_SUBSTRING_FUNCTION_NAME));
				result.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/engine/plugins/string/StringMatchingFunction.java", this),EntryType.FUNCTION_CAPI, StringMatchingFunction.STRING_MATCHES_FUNCTION_NAME));
				
			
			}
		} catch (EntryAlreadyExistsException e) {
			throw new CompilerException(e);
		} catch (IncludeException e){
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

		if (token.equals("+")) {
			result += "if((@lhs@ instanceof plugins.StringPlugin.StringElement) || (@rhs@ instanceof plugins.StringPlugin.StringElement)){\n";
			result += "evalStack.push(new plugins.StringPlugin.StringElement(@lhs@.toString() + @rhs@.toString()));\n";
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
	public CodeFragment rCode(ASTNode n) throws CompilerException {
		if (n.getGrammarClass().equals("Expression")
				&& n.getGrammarRule().equals("StringTerm")) {
			return new CodeFragment(
					"evalStack.push(new plugins.StringPlugin.StringElement(\""
							+ n.getToken().replaceAll("\n", "\\n") + "\"));\n");
		}

		throw new CompilerException(
				"unhandled code type: (StringPlugin, rCode, "
						+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
	}

	@Override
	public List<String> getCompileFunctionNames() {
		List<String> result = new ArrayList<String>();
		result.add("strlen");
		result.add("matches");
		result.add("stringSubstring");
		result.add("toString");
		return result;
	}

	@Override
	public CodeFragment compileFunctionCall(ASTNode n) throws CompilerException {
		List<ASTNode> children = n.getAbstractChildNodes();
		String fname = children.get(0).getToken();
		if (fname.equals("strlen")) {
			if (children.size() != 2)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);

			CodeFragment result = new CodeFragment("");
			result.appendFragment(CoreASMCompiler.getEngine().compile(
					children.get(1), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(plugins.StringPlugin.StringElement, tmp1)=(plugins.StringPlugin.StringElement)evalStack.pop();\n");
			result.appendLine("evalStack.push(@tmp1@.toString().length());\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		} else if (fname.equals("matches")) {
			if (children.size() != 3)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);
			CodeFragment result = new CodeFragment("");
			result.appendFragment(CoreASMCompiler.getEngine().compile(
					children.get(1), CodeType.R));
			result.appendFragment(CoreASMCompiler.getEngine().compile(
					children.get(2), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(plugins.StringPlugin.StringElement, regex)=(plugins.StringPlugin.StringElement)evalStack.pop();\n");
			result.appendLine("@decl(plugins.StringPlugin.StringElement, value)=(plugins.StringPlugin.StringElement)evalStack.pop();\n");
			result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(@value@.matches(@regex@)));\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		} else if (fname.equals("stringSubstring")) {
			if (children.size() != 4)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);
			CodeFragment result = new CodeFragment("");
			result.appendFragment(CoreASMCompiler.getEngine().compile(
					children.get(1), CodeType.R));
			result.appendFragment(CoreASMCompiler.getEngine().compile(
					children.get(2), CodeType.R));
			result.appendFragment(CoreASMCompiler.getEngine().compile(
					children.get(3), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(plugins.NumberPlugin.NumberElement, value2)=(plugins.NumberPlugin.NumberElement)evalStack.pop();\n");
			result.appendLine("@decl(plugins.NumberPlugin.NumberElement, value1)=(plugins.NumberPlugin.NumberElement)evalStack.pop();\n");
			result.appendLine("@decl(plugins.StringPlugin.StringElement, str)=(plugins.StringPlugin.StringElement)evalStack.pop();\n");
			result.appendLine("if(!@value2@.isInteger() || !@value1@.isInteger()) throw new ClassCastException();\n");
			result.appendLine("evalStack.push(new plugins.StringPlugin.StringElement(@str@.toString().substring((int)@value1@.getValue(),(int)@value2@.getValue())));\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		} else if (fname.equals("toString")) {
			if (children.size() != 2)
				throw new CompilerException(
						"wrong number of arguments for function " + fname);
			CodeFragment result = new CodeFragment("");
			result.appendFragment(CoreASMCompiler.getEngine().compile(
					children.get(1), CodeType.R));
			result.appendLine("try{\n");
			result.appendLine("@decl(CompilerRuntime.Element, tmp1)=(CompilerRuntime.Element)evalStack.pop();\n");
			result.appendLine("evalStack.push(new plugins.StringPlugin.StringElement(@tmp1@.toString()));\n");
			result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
			result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
			result.appendLine("}\n");

			return result;
		}

		throw new CompilerException(
				"unknown function name for plugin NumberPlugin: " + fname);
	}
}
