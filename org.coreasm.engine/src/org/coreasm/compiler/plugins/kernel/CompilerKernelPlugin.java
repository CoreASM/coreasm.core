package org.coreasm.compiler.plugins.kernel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.classlibrary.ClassInclude;
import org.coreasm.compiler.classlibrary.ClassLibrary;
import org.coreasm.compiler.classlibrary.RuleClassFile;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.IncludeException;
import org.coreasm.compiler.interfaces.CompilerCodeBPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeLPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeLRPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeRPlugin;
import org.coreasm.compiler.interfaces.CompilerCodeUPlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
import org.coreasm.compiler.mainprogram.EntryType;
import org.coreasm.compiler.mainprogram.MainFileEntry;
import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.InheritRule;
import org.coreasm.compiler.preprocessor.Preprocessor;
import org.coreasm.compiler.preprocessor.SynthesizeRule;
import org.coreasm.engine.absstorage.BooleanBackgroundElement;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.FunctionBackgroundElement;
import org.coreasm.engine.absstorage.RuleBackgroundElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.coreasm.engine.kernel.UpdateRuleNode;
import org.coreasm.engine.plugin.Plugin;

public class CompilerKernelPlugin implements 
	CompilerPlugin, CompilerCodeLPlugin, CompilerCodeRPlugin, CompilerCodeUPlugin, CompilerCodeBPlugin,
	CompilerVocabularyExtender, CompilerOperatorPlugin, CompilerCodeLRPlugin, CompilerPreprocessorPlugin{

	private Plugin interpreterPlugin;
	
	public CompilerKernelPlugin(Plugin parent){
		this.interpreterPlugin = parent;
	}
	
	@Override
	public Plugin getInterpreterPlugin(){
		return interpreterPlugin;
	}

	private void addAbsReplacement(String name){
		CoreASMCompiler.getEngine().getClassLibrary().addPackageReplacement("org.coreasm.engine.absstorage." + name, "CompilerRuntime." + name);
	}
	
	
	//------------------------------------------------------------
		//---------------- Start of Compiler code --------------------
		//------------------------------------------------------------
		@Override
		public CodeFragment lCode(ASTNode n) throws CompilerException {
			if (n.getGrammarRule().equals("ID")) {
				CodeFragment result = new CodeFragment("");
				result.appendLine("evalStack.push(new CompilerRuntime.Location(\""
						+ n.getToken()
						+ "\", new java.util.ArrayList<CompilerRuntime.Element>()));\n");
				return result;
			} else if (n.getGrammarRule().equals("FunctionRuleTerm")) {
				if (n.getGrammarClass().equals("FunctionRule")) {
					FunctionRuleTermNode frtn = (FunctionRuleTermNode) n;

					String name = frtn.getName();
					if (frtn.hasArguments()) {
						// if the function is not a constant, the arguments
						// need to be evaluated first
						List<ASTNode> args = frtn.getArguments();
						CodeFragment[] argcode = new CodeFragment[args.size()];

						for (int i = 0; i < args.size(); i++) {
							argcode[i] = CoreASMCompiler.getEngine().compile(args.get(i),
									CodeType.R);
						}
						// each code will leave its value on the eval stack
						CodeFragment code = new CodeFragment("");
						for (int i = args.size() - 1; i >= 0; i--) {
							code.appendFragment(argcode[i]);
						}

						code.appendLine("\n"
								+ "@decl(java.util.ArrayList<CompilerRuntime.Element>,arglist);\n@arglist@ = new java.util.ArrayList<>();\n");
						code.appendLine("for(@decl(int,__i)=0;@__i@<"
								+ args.size()
								+ ";@__i@++)\n@arglist@.add((CompilerRuntime.Element)evalStack.pop());\n");
						code.appendLine("evalStack.push(new CompilerRuntime.Location(\""
								+ name + "\", @arglist@));");
						return code;
					} else {
						String code = "evalStack.push(new CompilerRuntime.Location(\""
								+ name
								+ "\", new java.util.ArrayList<CompilerRuntime.Element>()));";
						return new CodeFragment(code);
					}
				}
			}
			throw new CompilerException("unhandled code type: (Kernel, lCode, "
					+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
		}

		@Override
		public CodeFragment rCode(ASTNode n) throws CompilerException {
			if (n.getGrammarRule().equals("KernelTerms")) {
				if (n.getToken().equals("undef")) {
					return new CodeFragment(
							"evalStack.push(CompilerRuntime.Element.UNDEF);");
				} else if (n.getToken().equals("self")) {
					return new CodeFragment("if(CompilerRuntime.RuntimeProvider.getRuntime().getSelf(Thread.currentThread()) == null)\n"
							+ "evalStack.push(CompilerRuntime.Element.UNDEF);\n"
							+ "else\n" + "evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getSelf(Thread.currentThread()));\n");
				}
			}
			if (n.getGrammarRule().equals("BooleanTerm")) {
				return new CodeFragment(
						"evalStack.push("
								+ ((n.getToken().equals("true")) ? "CompilerRuntime.BooleanElement.TRUE"
										: "CompilerRuntime.BooleanElement.FALSE")
								+ ");");
			} else if (n.getGrammarRule().equals("FunctionRuleTerm")) {
				FunctionRuleTermNode frtn = (FunctionRuleTermNode) n;
				String name = frtn.getName();

				if (frtn.getArguments().size() > 0) {
					List<ASTNode> args = frtn.getArguments();
					CodeFragment[] argcode = new CodeFragment[args.size()];
					for (int i = 0; i < args.size(); i++) {
						argcode[i] = CoreASMCompiler.getEngine().compile(args.get(i),
								CodeType.R);
					}

					CodeFragment code = new CodeFragment("");
					for (int i = args.size() - 1; i >= 0; i--) {
						code.appendFragment(argcode[i]);
					}
					code.appendLine("\n"
							+ "@decl(java.util.ArrayList<CompilerRuntime.Element>,arglist);\n@arglist@ = new java.util.ArrayList<>();\n");
					code.appendLine("for(@decl(int,__i)=0;@__i@<"
							+ args.size()
							+ ";@__i@++)\n@arglist@.add((CompilerRuntime.Element)evalStack.pop());\n");
					code.appendLine("evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(new CompilerRuntime.Location(\""
							+ name + "\", @arglist@)));");

					return code;
				} else {
					// look in all different locations
					// TODO: integrate undef location handlers
					CodeFragment code = new CodeFragment("");
					code.appendLine("@decl(Object, res) = ruleparams.get(\"" + name + "\");\n");
					code.appendLine("if(@res@ != null){\n");
					code.appendLine("evalStack.push(((CompilerRuntime.RuleParam) @res@).evaluate(localStack));\n");
					code.appendLine("}\n");
					code.appendLine("else{\n");
					code.appendLine("@res@ = localStack.get(\"" + name + "\");\n");
					code.appendLine("if(@res@ == null){\n");
					code.appendLine("@decl(CompilerRuntime.Location,loc) = new CompilerRuntime.Location(\"" + name + "\", new java.util.ArrayList<CompilerRuntime.Element>());\n");
					code.appendLine("@res@ = CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(@loc@);\n");
					code.appendLine("}\n");
					code.appendLine("if(@res@.equals(CompilerRuntime.Element.UNDEF)){\n");
					code.appendLine("evalStack.push(CompilerRuntime.Element.UNDEF); //this should actually be the undef handler\n");
					code.appendLine("}\n");
					code.appendLine("else{\n");
					code.appendLine("evalStack.push(@res@);\n");
					code.appendLine("}\n");
					code.appendLine("}\n");
					return code;
				}
			/*} 
			 does this ever occur? an id should never be translated in r context, there is always a function rule term before it
			else if (n.getGrammarRule().equals("ID")) {
				String code = "if(localStack.get(\"" + n.getToken() + "\") != null) evalStack.push(localStack.get(\"" + n.getToken() + "\"));\n";
				code += "else if(CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(new CompilerRuntime.Location(\""
						+ n.getToken() + "\")) != null) evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(new CompilerRuntime.Location(\""
						+ n.getToken() + "\")));\n";
				code += "else evalStack.push(CompilerRuntime.Element.undef);\n"; // this
																					// line
																					// in
																					// particular
																					// should
																					// be
																					// the
																					// undef
																					// handler

				return new CodeFragment(code);*/
			} else if (n.getGrammarRule().equals("")) {
				if (n.getGrammarClass().equals("Expression")) {
					return CoreASMCompiler.getEngine().compile(n.getFirst(), CodeType.R);
				}
			} else if (n.getGrammarRule().equals("RuleOrFunctionElementTerm")) {
				if (n.getGrammarClass().equals("Expression")) {
					// @operator
					// the @operator returns either a rule element or a function
					// element
					try {
						String name = n.getAbstractChildNodes().get(0).getToken();
											
						//get rule names
						Preprocessor prep = CoreASMCompiler.getEngine().getPreprocessor();
						Information inf = prep.getGeneralInfo().get("RuleDeclaration");
						
						if (inf.getChildren().contains(name)) {
							// if it is a rule, return the rule element
							CodeFragment tmp = new CodeFragment(
									"@decl(CompilerRuntime.Rule, tmprule)=new Rules."
											+ name + "();\n");
							tmp.appendLine("@tmprule@.initRule(new java.util.ArrayList<CompilerRuntime.RuleParam>(), null);\n");
							tmp.appendLine("evalStack.push(@tmprule@);\n");
							return tmp;
						} else {
							// otherwise get the function element from the abstract
							// storage
							CodeFragment tmp = new CodeFragment(
									"evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getFunction(\""
											+ name + "\"));\n");
							return tmp;
						}

					} catch (Exception e) {
						throw new CompilerException(e);
					}
				}
			}
			throw new CompilerException("unhandled code type: (Kernel, rCode, "
					+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
		}

		@Override
		public CodeFragment uCode(ASTNode n) throws CompilerException {
			if (n.getGrammarClass().equals("Rule")) {
				if (n.getGrammarRule().equals("SkipRule")) {
					return new CodeFragment(
							"evalStack.push(new CompilerRuntime.UpdateList());");
				} else if (n.getGrammarRule().equals("UpdateRule")) {
					// rule of the form identifier := value
					if (!(n instanceof UpdateRuleNode))
						throw new CompilerException(
								"Illegal Node found - expected UpdateRuleNode");

					ASTNode location = n.getFirst();
					ASTNode expression = n.getFirst().getNext();

					CodeFragment lhs = CoreASMCompiler.getEngine().compile(location,
							CodeType.L);
					CodeFragment rhs = CoreASMCompiler.getEngine().compile(expression,
							CodeType.R);

					// generates an update
					CodeFragment result = new CodeFragment("\n");
					result.appendFragment(rhs);
					result.appendFragment(lhs);
					result.appendLine("\n@decl(CompilerRuntime.Location,tmplocation)=(CompilerRuntime.Location)evalStack.pop();\n");
					result.appendLine("@decl(CompilerRuntime.Element,tmpvalue)=(CompilerRuntime.Element)evalStack.pop();\n");
					result.appendLine("\n@decl(CompilerRuntime.Update,tmpupdate)=new CompilerRuntime.Update(@tmplocation@, @tmpvalue@, \"updateAction\", this.getUpdateResponsible(), null);\n");
					result.appendLine("@decl(CompilerRuntime.UpdateList,tmplist)=new CompilerRuntime.UpdateList();\n");
					result.appendLine("@tmplist@.add(@tmpupdate@);\n");
					result.appendLine("evalStack.push(@tmplist@);\n");

					return result;
				} else if (n.getGrammarRule().equals("ImportRule")) {
					// import x do {}
					// get the identifier
					CodeFragment result = new CodeFragment("");
					CodeFragment name = CoreASMCompiler.getEngine().compile(
							n.getAbstractChildNodes().get(0), CodeType.L);
					result.appendFragment(name);
					result.appendLine("@decl(CompilerRuntime.Location,nameloc)=(CompilerRuntime.Location)evalStack.pop();\n");
					result.appendLine("if(@nameloc@.args.size() != 0) throw new Exception();\n");

					result.appendLine("localStack.pushLayer();\n");
					result.appendLine("localStack.put(@nameloc@.name, new CompilerRuntime.Element());\n");
					result.appendFragment(CoreASMCompiler.getEngine().compile(
							n.getAbstractChildNodes().get(1), CodeType.U));
					result.appendLine("localStack.popLayer();\n");

					return result;
				} else if (n.getGrammarRule().equals("MacroCallRule")) {
					// TODO parameter might also be a rule call - how to fix this?
					MacroCallRuleNode mcrn = (MacroCallRuleNode) n;
					String name = mcrn.getFunctionRuleElement().getFirst()
							.getToken();

					CodeFragment cf = new CodeFragment(
							"@decl(java.util.ArrayList<CompilerRuntime.RuleParam>,arglist)=new java.util.ArrayList<>();");

					FunctionRuleTermNode params = (FunctionRuleTermNode) mcrn
							.getFunctionRuleElement();

					if (params.hasArguments()) {
						CodeFragment[] paramCode = new CodeFragment[params
								.getArguments().size()];

						for (int i = 0; i < paramCode.length; i++) {
							// create the code for the parameter
							CodeFragment tmp = CoreASMCompiler.getEngine().compile(
									params.getArguments().get(i), CodeType.R);
							// create the param object and push it onto the stack
							cf.appendLine("\n@arglist@.add(new CompilerRuntime.RuleParam(){\n");
							cf.appendLine("public CompilerRuntime.Rule getUpdateResponsible(){\nreturn null;\n}\n");
							cf.appendLine("java.util.Map<String, CompilerRuntime.RuleParam> ruleparams;\n");
							cf.appendLine("public void setParams(java.util.Map<String, CompilerRuntime.RuleParam> params){\n");
							cf.appendLine("this.ruleparams = params;\n");
							cf.appendLine("}\n");
							cf.appendLine("public CompilerRuntime.Element evaluate(CompilerRuntime.LocalStack localStack) throws Exception{\n");
							cf.appendFragment(tmp);
							cf.appendLine("\nreturn (CompilerRuntime.Element)evalStack.pop();\n}\n});\n");
							cf.appendLine("@arglist@.get(@arglist@.size() - 1).setParams(ruleparams);\n");
						}
					}
					// cf.appendLine("\n@decl(CompilerRuntime.Rule,macrorule)=new Rules."
					// + name + "(@arglist, localStack);");
					
					Preprocessor prep = CoreASMCompiler.getEngine().getPreprocessor();
					Information inf = prep.getGeneralInfo().get("RuleDeclaration");
					
					if (inf.getChildren().contains(name)) {
						// check parameter count
						if (inf.getInformation(name).getChildren().size() != params
								.getArguments().size()) {
							CoreASMCompiler.getEngine().addError("wrong number of parameters in rulecall to rule " + name);
							throw new CompilerException(
									"wrong number of parameters for Rulecall to Rule "
											+ name);
						}

						// if name is a valid rulename, so call it by creating a new
						// rule instance
						cf.appendLine("@decl(CompilerRuntime.Rule, callruletmp)=new Rules."
								+ name + "();\n");
						cf.appendLine("@callruletmp@.initRule(@arglist@, localStack);\n");
						cf.appendLine("@decl(CompilerRuntime.UpdateList, ulist)=new CompilerRuntime.UpdateList();\n");
						//cf.appendLine("@callruletmp@.setAgent(this.getAgent());\n");
						cf.appendLine("@ulist@.addAll(@callruletmp@.call().updates);\n");
						cf.appendLine("evalStack.push(@ulist@);\n");
					} else {
						throw new UnsupportedOperationException("currently not supported");
						/*// otherwise, name is a parameter on the local stack
						cf.appendLine("@decl(CompilerRuntime.Rule,callruletmp);\n");
						cf.appendLine("@callruletmp@ = ((CompilerRuntime.Rule)((CompilerRuntime.RuleParam) localStack.get(\""
								+ name + "\")).evaluate(localStack)).getCopy();\n");
						cf.appendLine("@callruletmp@.initRule(@arglist@, localStack);\n");
						cf.appendLine("@decl(CompilerRuntime.UpdateList, ulist)=new CompilerRuntime.UpdateList();\n");
						cf.appendLine("@ulist@.addAll(@callruletmp@.call().updates);\n");
						cf.appendLine("evalStack.push(@ulist@);\n");*/
					}

					return cf;
				}
			}

			throw new CompilerException("unhandled code type: (Kernel, uCode, "
					+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
		}

		@Override
		public void bCode(ASTNode n) throws CompilerException {
			CompilerEngine ce = CoreASMCompiler.getEngine();

			try {
				// logger.debug("Kernel bCode");
				if (n.getGrammarRule().equals("CoreASM")) {
					// TODO: move this to different parts
					CoreASMCompiler.getEngine().getLogger().debug(CompilerKernelPlugin.class, "copying CoreASM Runtime classes");
					// end of file adding

					List<ASTNode> children = new ArrayList<ASTNode>();
					children.addAll(n.getAbstractChildNodes());

					// Find the id node and drop all use nodes
					ASTNode id = null;
					for (int i = 0; i < children.size(); i++) {
						ASTNode node = children.get(i);
						if (node.getGrammarRule().equals("ID")) {
							if (id != null)
								throw new CompilerException(
										"only one id node allowed");
							id = node;
							children.remove(i);
							i--;
						}
						if (node.getGrammarRule().equals("UseClauses")) {
							children.remove(i);
							i--;
						}
					}
					if (id == null)
						throw new CompilerException(
								"Couldn't find id node for init rule");

					// request basic code for all other nodes
					for (Iterator<ASTNode> it = children.iterator(); it.hasNext();) {
						ce.compile((ASTNode) it.next(), CodeType.BASIC);
					}
				} else if (n.getGrammarRule().equals("Initialization")) {
					CoreASMCompiler.getEngine().getLogger().debug(CompilerKernelPlugin.class, "extracting initialization rule name");

					// should have exactly one child node which is an id
					String name = n.getAbstractChildNodes().get(0).getToken();

					name = "Rules." + name;

					CoreASMCompiler.getEngine().getMainFile().setInitRule(name);
				} else if (n.getGrammarRule().equals("RuleDeclaration")) {
					CoreASMCompiler.getEngine().getLogger().debug(CompilerKernelPlugin.class, "creating a rule for node");

					// first, find the signature
					ASTNode signature = n.getAbstractChildNodes().get(0);
					ASTNode body = n.getAbstractChildNodes().get(1);

					String ruleName = signature.getFirst().getToken();
					List<String> ruleParameters = new ArrayList<String>();
					for (int i = 1; i < signature.getAbstractChildNodes().size(); i++) {
						ruleParameters.add(signature.getAbstractChildNodes().get(i)
								.getToken());
					}

					// compile the body
					CodeFragment cbody = CoreASMCompiler.getEngine().compile(body, CodeType.U);
					RuleClassFile r = new RuleClassFile(ruleName, ruleParameters,
							cbody);

					// r.initRule(ruleName, "Rules", ruleParameters);
					// r.setBody(cbody);
					CoreASMCompiler.getEngine().getClassLibrary().addEntry(r);

					CoreASMCompiler.getEngine().getLogger().debug(CompilerKernelPlugin.class, "end rule creation");
				}
			} catch (Exception e) {
				throw new CompilerException(e);
			}
		}

		/*@Override
		public String getName() {
			return "Kernel";

			/*
			 * if(this instanceof ICoreASMPlugin){ return
			 * ((ICoreASMPlugin)this).getName(); } throw new
			 * UnsupportedOperationException("Unknown Plugin name");
			 * /
		}*/

		@Override
		public List<MainFileEntry> loadClasses(ClassLibrary classLibrary)
				throws CompilerException {
			// load runtime classes		
			ArrayList<MainFileEntry> loadedClasses = new ArrayList<MainFileEntry>();


			String enginePath = CoreASMCompiler.getEngine().getOptions().enginePath;
			// if no jar archive is set for the engine, simply copy files from the runtime directory
			if(enginePath == null){
				File runtimedir = new File(CoreASMCompiler.getEngine().getOptions().runtimeDirectory);//"src\\CompilerRuntime\\");
				if(!runtimedir.exists()){
					runtimedir = new File("CompilerRuntime\\");
					if(!runtimedir.exists()){
						CoreASMCompiler.getEngine().addError("could not find runtime files");
						throw new CompilerException("could not find runtime files");
					}
				}
				for (String s : runtimedir.list()) {
					if (s != "." && s != "..")
						try {
							classLibrary.addEntry(new ClassInclude(
									CoreASMCompiler.getEngine().getOptions().runtimeDirectory + s, "CompilerRuntime"));
						} catch (EntryAlreadyExistsException e) {
							CoreASMCompiler.getEngine().getLogger().error(CompilerKernelPlugin.class, "kernel should not have collisions with itself");
							e.printStackTrace();
						}
				}
				String policypath = CoreASMCompiler.getEngine().getOptions().runtimeDirectory + "..\\org\\coreasm\\engine\\kernel\\include\\DefaultSchedulingPolicy.java";//"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\kernel\\include\\DefaultSchedulingPolicy.java";
				String aggregatorpath = CoreASMCompiler.getEngine().getOptions().runtimeDirectory + "..\\org\\coreasm\\engine\\kernel\\include\\KernelAggregator.java";//"src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\kernel\\include\\KernelAggregator.java";
			
				try{
					loadedClasses.add(new MainFileEntry(classLibrary.includeClass(
							aggregatorpath, this), EntryType.AGGREGATOR,
							"kernelaggregator"));
					loadedClasses.add(new MainFileEntry(classLibrary.includeClass(
							policypath, this), EntryType.SCHEDULER, "scheduler"));
				}
				catch(EntryAlreadyExistsException e){
					e.printStackTrace();
				}
			}
			else{
				//otherwise the runtime is contained in the jar archive
				JarFile jar = null;
				try {
					jar = new JarFile(enginePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				Enumeration<JarEntry> entries = jar.entries();
				
				//list of entries not to include from the runtime directory
					
				classLibrary.addPackageReplacement("org.coreasm.engine.ControlAPI", "CompilerRuntime.ControlAPI");
				classLibrary.addPackageReplacement("org.coreasm.engine.interpreter.Node", "CompilerRuntime.Node");
				classLibrary.addPackageReplacement("org.coreasm.engine.interpreter.ScannerInfo", "CompilerRuntime.ScannerInfo");
				classLibrary.addPackageReplacement("org.coreasm.engine.scheduler.SchedulingPolicy", "CompilerRuntime.SchedulingPolicy");
				classLibrary.addPackageReplacement("org.coreasm.engine.EngineError", "CompilerRuntime.EngineError");
				classLibrary.addPackageReplacement("org.coreasm.engine.EngineException", "CompilerRuntime.EngineException");
				
				addAbsReplacement("AbstractUniverse");
				addAbsReplacement("BackgroundElement");
				addAbsReplacement("BooleanBackgroundElement");
				addAbsReplacement("BooleanElement");
				addAbsReplacement("Element");
				addAbsReplacement("ElementBackgroundElement");
				addAbsReplacement("Enumerable");
				addAbsReplacement("FunctionBackgroundElement");
				addAbsReplacement("FunctionElement");
				addAbsReplacement("Location");
				addAbsReplacement("MapFunction");
				addAbsReplacement("NameElement");
				addAbsReplacement("Signature");
				addAbsReplacement("UniverseElement");
				addAbsReplacement("Update");
				addAbsReplacement("ElementFormatException");
				addAbsReplacement("ElementList");
				addAbsReplacement("IdentifierNotFoundException");
				addAbsReplacement("InvalidLocationException");
				addAbsReplacement("NameConflictException");
				addAbsReplacement("UnmodifiableFunctionException");
				
				
				try{
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/AbstractUniverse.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/BackgroundElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/BooleanBackgroundElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/BooleanElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/Element.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/ElementBackgroundElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/Enumerable.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/FunctionBackgroundElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/FunctionElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/Location.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/MapFunction.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/NameElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/Signature.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/UniverseElement.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/Update.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/ElementFormatException.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/EngineError.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/EngineException.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/IdentifierNotFoundException.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/InvalidLocationException.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/NameConflictException.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/UnmodifiableFunctionException.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/absstorage/ElementList.java", "CompilerRuntime"));
					classLibrary.addEntry(new ClassInclude(enginePath, "org/coreasm/engine/scheduler/SchedulingPolicy.java", "CompilerRuntime"));
				}
				catch(IOException e){
					throw new CompilerException("could not load classes " + e.getMessage());
				} catch (EntryAlreadyExistsException e) {
					throw new CompilerException("could not load classes " + e.getMessage());
				}
				
				
				
				for(JarEntry jarEntry = entries.nextElement(); entries.hasMoreElements(); jarEntry = entries.nextElement()){
					String name = jarEntry.getName();
					if(name.startsWith("CompilerRuntime/") && name.endsWith(".java")){
						try {
							classLibrary.addEntry(new ClassInclude(enginePath, jarEntry.getName(), "CompilerRuntime"));
						} catch (EntryAlreadyExistsException e) {
							CoreASMCompiler.getEngine().getLogger().error(CompilerKernelPlugin.class, "kernel should not have collisions with itself");
							e.printStackTrace();
						} catch (IOException e) {
							CoreASMCompiler.getEngine().getLogger().error(CompilerKernelPlugin.class, "kernel could not access engine jar");
							e.printStackTrace();
						}
					}
				}
				
				//TODO: fix these includes
				try{
					loadedClasses.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/kernel/include/KernelAggregator.java", this), EntryType.AGGREGATOR, "kernelaggregator"));
					loadedClasses.add(new MainFileEntry(classLibrary.includeClass(enginePath, "org/coreasm/compiler/plugins/kernel/include/DefaultSchedulingPolicy.java", this), EntryType.SCHEDULER, "scheduler"));
				}
				catch(EntryAlreadyExistsException e){
					e.printStackTrace();
				} catch (IncludeException e) {
					e.printStackTrace();
				}
				
				
				try {
					jar.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// universes
			// the agents universe is loaded by default
			// loadedClasses.add(new
			// MainFileEntry(classLibrary.findEntry("CompilerRuntime.UniverseElement"),
			// EntryType.UNIVERSE, AbstractStorage.AGENTS_UNIVERSE_NAME));

			// backgrounds
			loadedClasses.add(new MainFileEntry(classLibrary
					.findEntry("CompilerRuntime.BooleanBackgroundElement"),
					EntryType.BACKGROUND,
					BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME));
			loadedClasses.add(new MainFileEntry(classLibrary
					.findEntry("CompilerRuntime.FunctionBackgroundElement"),
					EntryType.BACKGROUND,
					FunctionBackgroundElement.FUNCTION_BACKGROUND_NAME));
			loadedClasses.add(new MainFileEntry(classLibrary
					.findEntry("CompilerRuntime.ElementBackgroundElement"),
					EntryType.BACKGROUND,
					ElementBackgroundElement.ELEMENT_BACKGROUND_NAME));
			loadedClasses.add(new MainFileEntry(classLibrary
					.findEntry("CompilerRuntime.RuleBackgroundElement"),
					EntryType.BACKGROUND,
					RuleBackgroundElement.RULE_BACKGROUND_NAME));

			// functions
			// loadedClasses.add(new
			// MainFileEntry(classLibrary.findEntry("CompilerRuntime.ProgramFunction"),
			// EntryType.FUNCTION, AbstractStorage.PROGRAM_FUNCTION_NAME));
			/*try {
			} catch (EntryAlreadyExistsException e) {
				throw new CompilerException(e);
			}*/
			return loadedClasses;
		}


		@Override
		public List<String> unaryOperations() {
			return new ArrayList<String>();
		}

		@Override
		public List<String> binaryOperations() {
			List<String> result = new ArrayList<String>();
			result.add("=");
			return result;
		}

		@Override
		public String compileBinaryOperator(String token) {
			String result = "if(true){\nevalStack.push(CompilerRuntime.BooleanElement.valueOf(@lhs@.equals(@rhs@)));\n}\n";
			result = result + "else ";
			return result;
		}

		@Override
		public String compileUnaryOperator(String token) {
			return null;
		}

		@Override
		public CodeFragment lrCode(ASTNode n) throws CompilerException {
			if (n.getGrammarRule().equals("FunctionRuleTerm")) {
				if (n.getGrammarClass().equals("FunctionRule")) {
					// evaluate the right side of an expression
					// and push the location of the expression and the value to the
					// stack

					CodeFragment result = new CodeFragment("");
					FunctionRuleTermNode frtn = (FunctionRuleTermNode) n;

					result.appendLine("@decl(java.util.List<CompilerRuntime.Element>,args)=new java.util.ArrayList<CompilerRuntime.Element>();\n");

					if (frtn.hasArguments()) {
						for (ASTNode child : frtn.getArguments()) {
							result.appendFragment(CoreASMCompiler.getEngine().compile(child,
									CodeType.R));
							result.appendLine("@args@.add((CompilerRuntime.Element)evalStack.pop());\n");
						}
					}

					result.appendLine("@decl(CompilerRuntime.Location,loc)=new CompilerRuntime.Location(\""
							+ frtn.getName() + "\", @args@);\n");
					result.appendLine("evalStack.push(@loc@);\n");
					result.appendLine("evalStack.push(CompilerRuntime.RuntimeProvider.getRuntime().getStorage().getValue(@loc@));\n");

					return result;
				}
			}

			throw new CompilerException("unhandled code type: (Kernel, lrCode, "
					+ n.getGrammarClass() + ", " + n.getGrammarRule() + ")");
		}

		@Override
		public List<SynthesizeRule> getSynthesizeRules() {
			List<SynthesizeRule> result = new ArrayList<SynthesizeRule>();
			
			result.add(new SignatureTransformer());
			result.add(new IDSpawner());
			
			return result;
		}

		@Override
		public Map<String, SynthesizeRule> getSynthDefaultBehaviours() {
			Map<String, SynthesizeRule> result = new HashMap<String, SynthesizeRule>();
			
			result.put("ID", new IDSpawner());
			
			return result;
		}

		@Override
		public List<InheritRule> getInheritRules() {
			List<InheritRule> result = new ArrayList<InheritRule>();
			result.add(new RuleParamInheritRule());
			return result;
		}

		@Override
		public Map<String, InheritRule> getInheritDefaultBehaviours() {
			Map<String, InheritRule> result = new HashMap<String, InheritRule>();
			result.put("RuleParameter", new RuleParamInheritRule());
			return result;
		}

		@Override
		public String getName() {
			return Kernel.PLUGIN_NAME;
		}
}
