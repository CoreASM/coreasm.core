package org.coreasm.compiler.plugins.number;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.classlibrary.ConstantFunctionLibraryEntry;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.preprocessor.InheritRule;
import org.coreasm.compiler.preprocessor.SynthesizeRule;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugins.number.NumberPlugin;
import org.coreasm.engine.plugins.number.NumberValueTransformer;

public class CompilerNumberPlugin implements
	CompilerOperatorPlugin, CompilerVocabularyExtender, CompilerCodeRPlugin, 
	CompilerFunctionPlugin, CompilerPreprocessorPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerNumberPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

		@Override
		public List<MainFileEntry> loadClasses(ClassLibrary classLibrary) throws CompilerException {
			List<MainFileEntry> result = new ArrayList<MainFileEntry>();
			ClassLibrary library = CoreASMCompiler.getEngine().getClassLibrary();

			String jarpath = CoreASMCompiler.getEngine().getOptions().enginePath;
			
			try{
				library.addPackageReplacement("org.coreasm.engine.plugins.number.NumberElement", "plugins.NumberPlugin.NumberElement");
				
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberUtil.java", this), EntryType.INCLUDEONLY, ""));
				
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberBackgroundElement.java", this), EntryType.BACKGROUND,
						"NUMBER"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberRangeBackgroundElement.java",
								this), EntryType.BACKGROUND,
						"NUMBER_RANGE"));
		
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberElement.java",
								this), EntryType.INCLUDEONLY, ""));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberRangeElement.java",
								this), EntryType.INCLUDEONLY, ""));
		
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberEvenFunction.java",//"org/coreasm/compiler/plugins/number/include/NumberEvenFunction.java",
								this), EntryType.FUNCTION,
						"isEvenNumber"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberIntegerFunction.java",
								this), EntryType.FUNCTION,
						"isIntegerNumber"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberNaturalFunction.java",
								this), EntryType.FUNCTION,
						"isNaturalNumber"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberNegativeFunction.java",
								this), EntryType.FUNCTION,
						"isNegativeValue"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberOddFunction.java",
								this), EntryType.FUNCTION,
						"isOddNumber"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberPositiveFunction.java",
								this), EntryType.FUNCTION,
						"isPositiveValue"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberRealFunction.java",
								this), EntryType.FUNCTION,
						"isRealNumber"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/ToNumberFunctionElement.java",
								this), EntryType.FUNCTION,
						"toNumber"));
				
				LibraryEntry le = new ConstantFunctionLibraryEntry("infinity", "plugins.NumberPlugin", "plugins.NumberPlugin.NumberElement.POSITIVE_INFINITY");
				library.addEntry(le);
				
				result.add(new MainFileEntry(
						le, EntryType.FUNCTION, "infinity"));
						
						//library.includeClass(jarpath, "org/coreasm/engine/plugins/number/NumberInfinityFunction.java",
						//		this), EntryType.FUNCTION, "infinity"));
				result.add(new MainFileEntry(
						library.includeClass(jarpath, "org/coreasm/engine/plugins/number/SizeFunctionElement.java",
								this), EntryType.FUNCTION, "size"));
			}
			catch(EntryAlreadyExistsException e){
				throw new CompilerException(e);
			} catch (IncludeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		public CodeFragment rCode(ASTNode n)
				throws CompilerException {
			if (n.getGrammarRule().equals("NUMBER")) {
				if (n.getGrammarClass().equals("Expression")) {
					CodeFragment result = new CodeFragment("");
					result.appendLine("evalStack.push(plugins.NumberPlugin.NumberElement.getInstance("
							+ Double.parseDouble(n.getToken()) + "));\n");
					return result;
				}
			} else if (n.getGrammarRule().equals("SizeOfEnumTerm")) {
				if (n.getGrammarClass().equals("Expression")) {
					CodeFragment result = new CodeFragment("");
					CodeFragment en = CoreASMCompiler.getEngine().compile(
							n.getAbstractChildNodes().get(0), CodeType.R);
					result.appendFragment(en);
					result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,list)=new java.util.ArrayList<CompilerRuntime.Element>();\n");
					result.appendLine("@list@.add((CompilerRuntime.Element)evalStack.pop());\n");
					result.appendLine("evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(new CompilerRuntime.Location(\"size\", @list@)));\n");
					return result;
				}
			} else if (n.getGrammarRule().equals("NumberRangeTerm")) {
				if (n.getGrammarClass().equals("Expression")) {
					CodeFragment start = CoreASMCompiler.getEngine().compile(
							n.getAbstractChildNodes().get(0), CodeType.R);
					CodeFragment end = CoreASMCompiler.getEngine().compile(
							n.getAbstractChildNodes().get(1), CodeType.R);
					CodeFragment step = null;
					if (n.getAbstractChildNodes().size() == 3) {
						step = CoreASMCompiler.getEngine().compile(
								n.getAbstractChildNodes().get(2), CodeType.R);
					}

					CodeFragment result = new CodeFragment("");

					result.appendFragment(start);
					result.appendFragment(end);
					if (step != null) {
						result.appendFragment(step);
						result.appendLine("@decl(double,step)=(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue());\n");
					}
					result.appendLine("@decl(double,end)=(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue());\n");
					result.appendLine("@decl(double,start)=(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue());\n");
					if (step != null) {
						result.appendLine("evalStack.push(new plugins.NumberPlugin.NumberRangeElement(@start@,@end@,@step@));\n");
					} else {
						result.appendLine("evalStack.push(new plugins.NumberPlugin.NumberRangeElement(@start@,@end@));\n");
					}
					return result;
				}
			}

			throw new CompilerException(
					"unhandled code type: (NumberPlugin, rCode, "
							+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
		}

		@Override
		public String compileBinaryOperator(String token)
				throws CompilerException {
			String result = "";

			if (token.equals("+")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(((plugins.NumberPlugin.NumberElement)@lhs@).getValue() + ((plugins.NumberPlugin.NumberElement)@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("-")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(((plugins.NumberPlugin.NumberElement)@lhs@).getValue() - ((plugins.NumberPlugin.NumberElement)@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("*")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(((plugins.NumberPlugin.NumberElement)@lhs@).getValue() * ((plugins.NumberPlugin.NumberElement)@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("/")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(((plugins.NumberPlugin.NumberElement)@lhs@).getValue() / ((plugins.NumberPlugin.NumberElement)@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("div")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "@decl(double,nmbr1)=((plugins.NumberPlugin.NumberElement)@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((plugins.NumberPlugin.NumberElement)@rhs@).getValue();\n"
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance((@nmbr1@-(@nmbr1@%@nmbr2@))/@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals("%")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(((plugins.NumberPlugin.NumberElement)@lhs@).getValue() % ((plugins.NumberPlugin.NumberElement)@rhs@).getValue()));\n";
				result = result + "}\n";
			} else if (token.equals("^")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "@decl(double,nmbr1)=((plugins.NumberPlugin.NumberElement)@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((plugins.NumberPlugin.NumberElement)@rhs@).getValue();\n"
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(Math.pow(@nmbr1@,@nmbr2@)));\n";
				result = result + "}\n";
			} else if (token.equals(">")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "@decl(double,nmbr1)=((plugins.NumberPlugin.NumberElement)@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((plugins.NumberPlugin.NumberElement)@rhs@).getValue();\n"
						+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@nmbr1@>@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals(">=")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "@decl(double,nmbr1)=((plugins.NumberPlugin.NumberElement)@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((plugins.NumberPlugin.NumberElement)@rhs@).getValue();\n"
						+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@nmbr1@>=@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals("<")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "@decl(double,nmbr1)=((plugins.NumberPlugin.NumberElement)@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((plugins.NumberPlugin.NumberElement)@rhs@).getValue();\n"
						+ "evalStack.push(CompilerRuntime.BooleanElement.valueOf(@nmbr1@<@nmbr2@));\n";
				result = result + "}\n";
			} else if (token.equals("<=")) {
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement) && (@rhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "@decl(double,nmbr1)=((plugins.NumberPlugin.NumberElement)@lhs@).getValue();\n"
						+ "@decl(double,nmbr2)=((plugins.NumberPlugin.NumberElement)@rhs@).getValue();\n"
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
				result = "if((@lhs@ instanceof plugins.NumberPlugin.NumberElement)){\n";
				result = result
						+ "evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(0 - ((plugins.NumberPlugin.NumberElement)@lhs@).getValue());\n";
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
			List<ASTNode> children = n.getAbstractChildNodes();
			String fname = children.get(0).getToken();
			if (fname.equals("isEvenNumber")) {
				CodeFragment result = new CodeFragment("");
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment param = CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R);
				result.appendFragment(param);
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue() % 2 == 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("infinity")) {
				if (children.size() != 1)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				return new CodeFragment(
						"evalStack.push(plugins.NumberPlugin.NumberElement.POSITIVE_INFINITY);\n");
			} else if (fname.equals("isIntegerNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(((plugins.NumberPlugin.NumberElement)evalStack.pop()).isInteger()));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isNaturalNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(((plugins.NumberPlugin.NumberElement)evalStack.pop()).isNatural()));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isNegativeValue")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue() < 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isOddNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue() % 2 != 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isPositiveValue")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(((plugins.NumberPlugin.NumberElement)evalStack.pop()).getValue() >= 0));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("isRealNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("@decl(plugins.NumberPlugin.NumberElement,tmp)=(plugins.NumberPlugin.NumberElement) evalStack.pop();\n");
				result.appendLine("evalStack.push(CompilerRuntime.BooleanElement.valueOf(!Double.isInfinite(@tmp@.getValue()) && ! Double.isNaN(@tmp@.getValue())));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;
			} else if (fname.equals("size")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("@decl(CompilerRuntime.Enumerable,tmp) = (CompilerRuntime.Enumerable) evalStack.pop();\n");
				result.appendLine("if(@tmp@.size() == Long.MAX_VALUE){\n");
				result.appendLine("evalStack.push(plugins.NumberPlugin.NumberElement.POSITIVE_INFINITY);\n");
				result.appendLine("}\n");
				result.appendLine("else{\n");
				result.appendLine("evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(@tmp@.size()));\n");
				result.appendLine("}\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
				result.appendLine("}\n");

				return result;

			} else if (fname.equals("toNumber")) {
				if (children.size() != 2)
					throw new CompilerException(
							"wrong number of arguments for function " + fname);

				CodeFragment result = new CodeFragment("");
				result.appendFragment(CoreASMCompiler.getEngine().compile(children.get(1),
						CodeType.R));
				result.appendLine("try{\n");
				result.appendLine("evalStack.push(plugins.NumberPlugin.NumberElement.getInstance(Double.parseDouble(evalStack.pop().toString())));\n");
				result.appendLine("}catch(@decl(ClassCastException, cce)){\n");
				result.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF);\n");
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
}
