package org.coreasm.compiler.plugins.number;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.classlibrary.ClassLibrary;
import org.coreasm.compiler.components.classlibrary.ConstantFunctionLibraryEntry;
import org.coreasm.compiler.components.classlibrary.JarIncludeHelper;
import org.coreasm.compiler.components.classlibrary.LibraryEntry;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.mainprogram.EntryType;
import org.coreasm.compiler.components.mainprogram.MainFileEntry;
import org.coreasm.compiler.components.preprocessor.InheritRule;
import org.coreasm.compiler.components.preprocessor.SynthesizeRule;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerMakroProvider;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.plugins.number.code.rcode.NumberHandler;
import org.coreasm.compiler.plugins.number.code.rcode.NumberRangeHandler;
import org.coreasm.compiler.plugins.number.code.rcode.SizeOfHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.number.NumberPlugin;
import org.coreasm.engine.plugins.number.NumberValueTransformer;

/**
 * Provides numbers and functions on numbers.
 * @author Spellmaker
 *
 */
public class CompilerNumberPlugin extends CompilerCodePlugin implements
	CompilerOperatorPlugin, CompilerVocabularyExtender, 
	CompilerFunctionPlugin, CompilerPreprocessorPlugin, CompilerMakroProvider{

	private Plugin interpreterPlugin;
	
	/**
	 * Constructs a new plugin
	 * @param parent The interpreter version
	 */
	public CompilerNumberPlugin(Plugin parent){
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
		public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException {
			List<MainFileEntry> result = new ArrayList<MainFileEntry>();
			ClassLibrary library = engine.getClassLibrary();
			
			try{
				library.addPackageReplacement("org.coreasm.engine.plugins.number.NumberElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberElement", "NumberPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.number.NumberUtil", engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberUtil", "NumberPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.number.NumberBackgroundElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberBackgroundElement", "NumberPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.number.NumberRangeBackgroundElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberRangeBackgroundElement", "NumberPlugin"));
				library.addPackageReplacement("org.coreasm.engine.plugins.number.NumberRangeElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberRangeElement", "NumberPlugin"));
				
				
				result = (new JarIncludeHelper(engine, this)).
						includeStatic("org/coreasm/engine/plugins/number/NumberUtil.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/engine/plugins/number/NumberBackgroundElement.java", EntryType.BACKGROUND, "NUMBER").
						includeStatic("org/coreasm/engine/plugins/number/NumberRangeBackgroundElement.java", EntryType.BACKGROUND, "NUMBER_RANGE").
						includeStatic("org/coreasm/engine/plugins/number/NumberElement.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/engine/plugins/number/NumberRangeElement.java", EntryType.INCLUDEONLY).
						includeStatic("org/coreasm/engine/plugins/number/NumberEvenFunction.java", EntryType.FUNCTION, "isEvenNumber").
						includeStatic("org/coreasm/engine/plugins/number/NumberIntegerFunction.java", EntryType.FUNCTION, "isIntegerNumber").
						includeStatic("org/coreasm/engine/plugins/number/NumberNaturalFunction.java", EntryType.FUNCTION, "isNaturalNumber").
						includeStatic("org/coreasm/engine/plugins/number/NumberNegativeFunction.java", EntryType.FUNCTION, "isNegativeValue").
						includeStatic("org/coreasm/engine/plugins/number/NumberOddFunction.java", EntryType.FUNCTION, "isOddNumber").
						includeStatic("org/coreasm/engine/plugins/number/NumberPositiveFunction.java", EntryType.FUNCTION, "isPositiveValue").
						includeStatic("org/coreasm/engine/plugins/number/NumberRealFunction.java", EntryType.FUNCTION, "isRealNumber").
						includeStatic("org/coreasm/engine/plugins/number/ToNumberFunctionElement.java", EntryType.FUNCTION, "toNumber").
						includeStatic("org/coreasm/engine/plugins/number/SizeFunctionElement.java", EntryType.FUNCTION, "size").
						build();
				
				LibraryEntry le = new ConstantFunctionLibraryEntry("infinity", engine.getPath().pluginStaticPkg() + ".NumberPlugin.NumberElement.POSITIVE_INFINITY", getName(), LibraryEntryType.STATIC, engine);
				library.addEntry(le);
				result.add(new MainFileEntry(le, EntryType.FUNCTION, "infinity"));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			}
			return result;
		}

		@Override
		public List<String> unaryOperations() {
			List<String> ops = new ArrayList<String>();

			ops.add("-");

			return ops;
		}

		@Override
		public List<String> binaryOperations() {
			List<String> ops = new ArrayList<String>();

			ops.add("+");
			ops.add("-");
			ops.add("*");
			ops.add("/");
			ops.add("div");
			ops.add("%");
			ops.add("^");
			ops.add(">");
			ops.add(">=");
			ops.add("<");
			ops.add("<=");

			return ops;
		}

		@Override
		public String compileBinaryOperator(String token)
				throws CompilerException {
			String result = "";

			String numberelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberElement", "NumberPlugin");
			
			if (token.equals("+")) {
				result = "if((@lhs@ instanceof @NumberElement@) && (@rhs@ instanceof @NumberElement@)){\n";
				result = result
						+ "evalStack.push(@NumberElement@.getInstance(((@NumberElement@)@lhs@).getValue() + ((@NumberElement@)@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("-")) {
				result = "if((@lhs@ instanceof @NumberElement@) && (@rhs@ instanceof @NumberElement@)){\n";
				result = result
						+ "evalStack.push(@NumberElement@.getInstance(((@NumberElement@)@lhs@).getValue() - ((@NumberElement@)@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("*")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "evalStack.push(" + numberelement + ".getInstance(((" + numberelement + ")@lhs@).getValue() * ((" + numberelement + ")@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("/")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "evalStack.push(" + numberelement + ".getInstance(((" + numberelement + ")@lhs@).getValue() / ((" + numberelement + ")@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("div")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "@decl(double,nmbr1)=((" + numberelement + ")@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((" + numberelement + ")@rhs@).getValue();\n"
						+ "evalStack.push(" + numberelement + ".getInstance((@nmbr1@-(@nmbr1@%@nmbr2@))/@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals("%")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "evalStack.push(" + numberelement + ".getInstance(((" + numberelement + ")@lhs@).getValue() % ((" + numberelement + ")@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("^")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "@decl(double,nmbr1)=((" + numberelement + ")@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((" + numberelement + ")@rhs@).getValue();\n"
						+ "evalStack.push(" + numberelement + ".getInstance(Math.pow(@nmbr1@,@nmbr2@)));\n";
				result = result + "}\n";
			} else if (token.equals(">")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "@decl(double,nmbr1)=((" + numberelement + ")@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((" + numberelement + ")@rhs@).getValue();\n"
						+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@nmbr1@>@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals(">=")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "@decl(double,nmbr1)=((" + numberelement + ")@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((" + numberelement + ")@rhs@).getValue();\n"
						+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@nmbr1@>=@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals("<")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "@decl(double,nmbr1)=((" + numberelement + ")@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((" + numberelement + ")@rhs@).getValue();\n"
						+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@nmbr1@<@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals("<=")) {
				result = "if((@lhs@ instanceof " + numberelement + ") && (@rhs@ instanceof " + numberelement + ")){\n";
				result = result
						+ "@decl(double,nmbr1)=((" + numberelement + ")@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((" + numberelement + ")@rhs@).getValue();\n"
						+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@nmbr1@<=@nmbr2@));\n";
				result = result + "}\n";
			} else {
				throw new CompilerException("unknown operator call: NumberPlugin, "
						+ token);
			}

			result = result + " else ";

			return result;
		}

		@Override
		public String compileUnaryOperator(String token)
				throws CompilerException {
			String result = "";
			if (token.equals("-")) {
				result = "if((@lhs@ instanceof @NumberElement@)){\n";
				result = result
						+ "evalStack.push(@NumberElement@.getInstance(0 - ((@NumberElement@)@lhs@).getValue()));\n";
				result = result + "}\n";
			} else
				throw new CompilerException("unknown operator call: NumberPlugin, "
						+ token);

			result = result + " else ";

			return result;
		}

		@Override
		public List<String> getCompileFunctionNames() {
			List<String> result = new ArrayList<String>();
			result.add("isEvenNumber");
			result.add("infinity");
			result.add("isIntegerNumber");
			result.add("isNaturalNumber");
			result.add("isNegativeValue");
			result.add("isOddNumber");
			result.add("isPositiveValue");
			result.add("isRealNumber");
			result.add("size");
			result.add("toNumber");
			return result;
		}

		@Override
		public CodeFragment compileFunctionCall(ASTNode n)
				throws CompilerException {
			
			String numberelement = engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberElement", "NumberPlugin");
			List<ASTNode> children = n.getAbstractChildNodes();
			String fname = children.get(0).getToken();
			if (fname.equals("isEvenNumber")) {
				CodeFragment result = new CodeFragment("");
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment param = engine.compile(children.get(1),
						CodeType.R);
				result.appendFragment(param);
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(((" + numberelement + ")evalStack.pop()).getValue() % 2 == 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("infinity")) {
				if (children.size() != 1)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				return new CodeFragment(
						"evalStack.push(" + numberelement + ".POSITIVE_INFINITY);\n");
			} else if (fname.equals("isIntegerNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(((" + numberelement + ")evalStack.pop()).isInteger()));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isNaturalNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(((" + numberelement + ")evalStack.pop()).isNatural()));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isNegativeValue")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(((" + numberelement + ")evalStack.pop()).getValue() < 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isOddNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(((" + numberelement + ")evalStack.pop()).getValue() % 2 != 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isPositiveValue")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(((" + numberelement + ")evalStack.pop()).getValue() >= 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isRealNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("@decl(" + numberelement + ",tmp)=(" + numberelement + ") evalStack.pop();\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.valueOf(!Double.isInfinite(@tmp@.getValue()) && ! Double.isNaN(@tmp@.getValue())));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.BooleanElement.FALSE);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("size")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("@decl(@RuntimePkg@.Enumerable,tmp) = (@RuntimePkg@.Enumerable) evalStack.pop();\n");
				result.appendLine("if(@tmp@.size() == Long.MAX_VALUE){\n");
				result.appendLine("evalStack.push(" + numberelement + ".POSITIVE_INFINITY);\n");
				result.appendLine("}\n");
				result.appendLine("else{\n");
				result.appendLine("evalStack.push(" + numberelement + ".getInstance(@tmp@.size()));\n");
				result.appendLine("}\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;

			} else if (fname.equals("toNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(engine.compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(" + numberelement + ".getInstance(Double.parseDouble(evalStack.pop().toString())));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
				result.appendLine("}\n");
				result.appendLine("catch(@decl(NumberFormatException, nfe)){\n");
				result.appendLine("evalStack.push(@RuntimePkg@.Element.UNDEF);\n");
				result.appendLine("}\n");
				return result;
			}

			throw new CompilerException(
					"unknown function name for plugin NumberPlugin: " + fname);
		}

		@Override
		public List<SynthesizeRule> getSynthesizeRules() {
			List<SynthesizeRule> result = new ArrayList<SynthesizeRule>();
			result.add(new NumberValueTransformer());
			result.add(new NumberValueSpawner());
			return result;
		}



		@Override
		public List<InheritRule> getInheritRules() {
			// TODO Auto-generated method stub
			return null;
		}



		@Override
		public Map<String, SynthesizeRule> getSynthDefaultBehaviours() {
			// TODO Auto-generated method stub
			return null;
		}



		@Override
		public Map<String, InheritRule> getInheritDefaultBehaviours() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return NumberPlugin.PLUGIN_NAME;
		}

		@Override
		public void registerCodeHandlers() throws CompilerException {
			register(new NumberHandler(), CodeType.R, "Expression", "NUMBER", null);
			register(new NumberRangeHandler(), CodeType.R, "Expression", "NumberRangeTerm", null);
			register(new SizeOfHandler(), CodeType.R, "Expression", "SizeOfEnumTerm", null);
		}

		@Override
		public Map<String, String> getMakros() {
			Map<String, String> makros = new HashMap<String, String>();
			makros.put("NumberElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberElement", this.getName()));
			makros.put("NumberRangeElement", engine.getPath().getEntryName(LibraryEntryType.STATIC, "NumberRangeElement", this.getName()));
			return makros;
		}
}
