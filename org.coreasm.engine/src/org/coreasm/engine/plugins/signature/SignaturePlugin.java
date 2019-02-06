/*	
 * SignaturePlugin.java 	1.0 	$Revision: 101 $
 * 
 * Copyright (c) 2006-2007 George Ma
 * Copyright (c) 2006-2009 Roozbeh Farahbod
 *
 * Last Modified on $Date: 2009-08-12 14:45:42 +0200 (Mi, 12 Aug 2009) $ by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.signature.CompilerSignaturePlugin;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.CoreASMIssue;
import org.coreasm.engine.CoreASMWarning;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.EngineTools;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.FunctionElement.FunctionClass;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.MapFunction;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParseMap;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.UndefinedIdentifierHandler;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *	Plugin for function signatures and user defined universes.
 *   
 *  @author  George Ma, Roozbeh Farahbod
 */
public class SignaturePlugin extends Plugin 
		implements ParserPlugin, VocabularyExtender, ExtensionPointPlugin, UndefinedIdentifierHandler {
  
	protected static final Logger logger = LoggerFactory.getLogger(SignaturePlugin.class);

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 3, 1, "beta");
	
	public static final String PLUGIN_NAME = SignaturePlugin.class.getSimpleName();
	
	/** 
	 * The name of the Signature.NoUndefinedId property. If the engine has any value for this 
	 * property ths signature plug-in would not allow use of udefined identifiers.
	 */
	public static final String NO_UNDEFINED_ID_PROPERTY = "NoUndefinedId";
	
	/**
	 * The name of the Signature.TypeChecking property. This property can
	 * have any of the "off", "warning", "strict", and "on" values. 
	 * "on" and "strict" are equivalent. 
	 */
	public static final String TYPE_CHECKING_PROPERTY = "TypeChecking";
	
    private HashMap<String,FunctionElement> functions;
    private HashMap<String,UniverseElement> universes;
    private HashMap<String,BackgroundElement> backgrounds;
    private HashMap<String,RuleElement> rules;
    
    private IdentityHashMap<FunctionNode, FunctionElement> functionsWithInit;
    
    private Set<String>	dependencyNames;
    private Map<String, GrammarRule> parsers = null;
    private Map<EngineMode, Integer> sourceModes = null;
    
    public static enum CheckMode {OFF, WARN, STRICT};
    private CheckMode typeCheckingMode;    
    private CheckMode idCheckingMode;    
    //private HashMap<String,FunctionClass> functionClass;
    //private boolean hasInit = false;
    private boolean processingSignatures = false;


	private final String[] keywords = {"enum", "universe", "controlled", "monitored", "static", "function", "initially", "initialized", "by", "derived"};
	private final String[] operators = {"=", "{", "}", ",", ":", "->"};
	private static final Set<String> options = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] { NO_UNDEFINED_ID_PROPERTY, TYPE_CHECKING_PROPERTY })));
	
	private final CompilerPlugin compilerPlugin = new CompilerSignaturePlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
    /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.Plugin#initialize()
     */
    public void initialize() {                
        typeCheckingMode = CheckMode.OFF;
        idCheckingMode = CheckMode.OFF;
    }
    
    @Override
    public void terminate() {
    	super.terminate();
    	functions = null;
    	universes = null;
    	backgrounds = null;
    	rules = null;
    }

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

    /*
     * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
     */
    public Parser<Node> getParser(String nonterminal) {
    	return null;
    }
    
	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}
	
	@Override
	public Set<String> getOptions() {
		return options;
	}
	
	@Override
	public void checkOptionValue(String option, String value) throws CoreASMIssue {
		if (TYPE_CHECKING_PROPERTY.equals(option) || NO_UNDEFINED_ID_PROPERTY.equals(option)) {
			try {
				CheckMode.valueOf(value.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new CoreASMWarning(getName(), "'" + value + "' is not valid and will be treated as 'off'");
			}
		}
	}

	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			Kernel kernelPlugin = (Kernel)capi.getPlugin("Kernel");
			KernelServices kernel = (KernelServices)kernelPlugin.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> ruleSignatureParser = kernel.getRuleSignatureParser(); 
			Parser<Node> ruleParser = kernel.getRuleParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();			
			
			ParserPlugin numberPlugin = (ParserPlugin)capi.getPlugin("NumberPlugin");
			if (numberPlugin == null) 
				throw new EngineError("SignaturePlugin: Cannot access Number plug-in.");
			Parser<Node> numberRangeTermParser = numberPlugin.getParser("NumberRangeTerm");
			if (numberRangeTermParser == null)
				throw new EngineError("SignaturePlugin: Cannot access NumberRangeTerm parser from Number plug-in.");
			
			// EnumerationDefinition : 'enum' ID '=' '{' ID (',' ID)* '}'
			Parser<Node> enumParser = Parsers.array(
					new Parser[] {
							pTools.getKeywParser("enum", PLUGIN_NAME),
							idParser,
							pTools.getOprParser("="),
							pTools.getOprParser("{"),
							pTools.csplus(idParser),
							pTools.getOprParser("}"),
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						@Override
						public Node apply(Object[] vals) {
							Node node = new EnumerationNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
						
					});
			parsers.put("EnumerationDefinition", new GrammarRule("EnumerationDefinition", 
					"'enum' ID '=' '{' ID (',' ID)* '}'", enumParser, PLUGIN_NAME));
					
			// UniverseDefinition : 'universe' ID ('=' '{' ID (',' ID)* '}')?
			Parser<Node> univParser = Parsers.array(
					new Parser[] {
							pTools.getKeywParser("universe", PLUGIN_NAME),
							idParser,
							pTools.seq(
								pTools.getOprParser("="),
								pTools.getOprParser("{"),
								pTools.csplus(idParser),
								pTools.getOprParser("}")).optional(null),
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node apply(Object[] vals) {
							Node node = new UniverseNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
						
					});
			parsers.put("UniverseDefinition", new GrammarRule("UniverseDefinition", 
					"'universe' ID ( '=' '{' ID (',' ID)* '}' )?", univParser, PLUGIN_NAME));

			// UniverseTerm : ID | NumberRangeTerm
			Parser<Node> univTermParser = Parsers.or(idParser,	numberRangeTermParser);
			parsers.put("UniverseTerm", new GrammarRule("UniverseTerm", 
					"ID | NumberRangeTerm", univTermParser, PLUGIN_NAME));
			
			// UniverseTuple : UniverseTerm ('*' UniverseTerm)*
			Parser<Node> univTupleParser = Parsers.array(
					new Parser[] { 
							pTools.csplus(
									pTools.getOprParser("*"),
									univTermParser) 
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node apply(Object[] vals) {
							Node node = new ASTNode(
				            		SignaturePlugin.PLUGIN_NAME,
				            		ASTNode.DECLARATION_CLASS,
				            		"UniverseTuple",
				            		null,
				            		null);
							addChildren(node, vals);
							node.setScannerInfo(node.getFirstCSTNode());
							return node;
						}
						
					});
			parsers.put("UniverseTuple", new GrammarRule("UniverseTuple", 
					"UniverseTerm ('*' UniverseTerm )*", univTupleParser, PLUGIN_NAME));

			// FunctionClass : 'controlled'|'static'|'monitored'
			Parser<Node> funcClassParser = Parsers.or(
					pTools.getKeywParser("controlled", PLUGIN_NAME),
					pTools.getKeywParser("static", PLUGIN_NAME),
					pTools.getKeywParser("monitored", PLUGIN_NAME)).map(new ParseMap<Node, Node>(PLUGIN_NAME) {

						@Override
						public Node apply(Node v) {
							return new ASTNode(
					        		SignaturePlugin.PLUGIN_NAME,
					        		ASTNode.DECLARATION_CLASS,
					        		"FunctionClass",
					        		v.getToken(),
					        		v.getScannerInfo(),
					        		Node.KEYWORD_NODE
					        		);
						}
						
					});
			parsers.put("FunctionClass", new GrammarRule("FunctionClass", 
					"'controlled'|'static'|'monitored'", funcClassParser, PLUGIN_NAME));
			
			// FunctionSignature : 'function' (FunctionClass)? ID ':' (UniverseTuple)? '->' UniverseTerm ('initially' Term | 'initialized by' Term)?
			Parser<Node> funcSigParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("function", PLUGIN_NAME),
						funcClassParser.optional(null),
						idParser,
						pTools.getOprParser(":"),
						univTupleParser.optional(null),
						pTools.getOprParser("->"),
						univTermParser,
						Parsers.or(
							pTools.seq(
								pTools.getKeywParser("initially", PLUGIN_NAME),
								termParser).atomic(),
							pTools.seq(
								pTools.getKeywParser("initialized", PLUGIN_NAME),
								pTools.getKeywParser("by", PLUGIN_NAME),
								termParser).atomic()
						).optional(null)
					}).map(new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node apply(Object[] vals) {
							Node node = new FunctionNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}});
			parsers.put("FunctionSignature", new GrammarRule("FunctionSignature", 
					"'function' (FunctionClass)? ID ':' (UniverseTuple)? '->' UniverseTerm (('initially' Term) | ('initialized by' Term))?", funcSigParser, PLUGIN_NAME));
			
			// DerivedFunctionDeclaration : 'function'? 'derived' RuleSignature '=' Term
			Parser<Node> derivedFuncParser = Parsers.array(
					new Parser[] {
						pTools.seq(
								pTools.getKeywParser("function", PLUGIN_NAME)).optional(null),
						pTools.getKeywParser("derived", PLUGIN_NAME),
						ruleSignatureParser,
						pTools.getOprParser("="),
						Parsers.or(termParser, ruleParser),
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node apply(Object[] vals) {
							ScannerInfo info = null;
							if (vals[0] != null)
								info = ((Node)((Object[])vals[0])[0]).getScannerInfo();
							else
								info = ((Node)vals[1]).getScannerInfo();
							Node node = new DerivedFunctionNode(info);
							addChildren(node, vals);
							return node;
						}});
			parsers.put("DerivedFunctionDeclaration", new GrammarRule("DerivedFunctionDeclaration", 
					"'function'? 'derived' RuleSignature '=' Term", derivedFuncParser, PLUGIN_NAME));
			
			
			// Signature : (EnumerationDefinition|FunctionSignature|UniverseDefinition)*
			Parser<Node> sigParser = Parsers.array(
					new Parser[] {
						Parsers.or(
								enumParser,
								univParser,
								funcSigParser,
								derivedFuncParser)
						}).map(
						new ParserTools.ArrayParseMap(PLUGIN_NAME) {

							@Override
							public Node apply(Object[] vals) {
								Node node = new ASTNode(
					        			SignaturePlugin.PLUGIN_NAME,
					        			ASTNode.DECLARATION_CLASS,
					        			"Signature",
					        			null,
					        			((Node)vals[0]).getScannerInfo());
								addChildren(node, vals);
								return node;
							}});
			parsers.put("Signature", new GrammarRule("Signature", 
					"(EnumerationDefinition|UniverseDefinition|FunctionSignature|DerivedFunctionDeclaration)*", sigParser, PLUGIN_NAME));
			
			parsers.put("Header", new GrammarRule("Header", "Signature", sigParser, PLUGIN_NAME));
			
		}
		return parsers;
	}
	
    /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.ExtensionPointPlugin#getTargetModes()
     */
    public Map<EngineMode, Integer> getTargetModes() {
    	return Collections.emptyMap();
    	/*
        HashSet<EngineMode> set = new HashSet<EngineMode>();
        set.add(EngineMode.emInitializingState);
        return set;
        */
    }

    /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.ExtensionPointPlugin#getSourceModes()
     */
    public Map<EngineMode, Integer> getSourceModes() {
    	if (sourceModes == null) {
    		sourceModes = new HashMap<EngineMode, Integer>();
        	//sourceModes.add(EngineMode.emParsingSpec);
        	sourceModes.put(EngineMode.emAggregation, ExtensionPointPlugin.DEFAULT_PRIORITY);
        	sourceModes.put(EngineMode.emInitializingState, ExtensionPointPlugin.DEFAULT_PRIORITY);
    	}
        return sourceModes;
    }

    /*
     * Returns the type checking mode policy based on the 
     * TYPE_CHECKING_PROPERTY of the engine.
     */
    private CheckMode getTypeCheckMode() {
    	String mode = getOptionValue(TYPE_CHECKING_PROPERTY);
    	typeCheckingMode = CheckMode.OFF;
    	if (mode != null) {
    		if (mode.equals("warning"))
    			typeCheckingMode = CheckMode.WARN;
    		else
    			if (mode.equals("on") || mode.equals("strict"))
    				typeCheckingMode = CheckMode.STRICT;
    			else
    				if (!mode.equals("off"))
    					capi.warning(PLUGIN_NAME, 
    							"The value of engine property '" + TYPE_CHECKING_PROPERTY + 
    							"' is ignored as it is neither 'off', 'warning', 'on', nor 'strict'.");
    	}
    	return typeCheckingMode;
    }
    
    /*
     * Returns the id checking mode policy based on the 
     * NO_UNDEFINED_ID_PROPERTY of the engine.
     */
    private CheckMode getIdCheckMode() {
    	String mode = getOptionValue(NO_UNDEFINED_ID_PROPERTY);
    	idCheckingMode = CheckMode.OFF;
    	if (mode != null) {
    		if (mode.equals("warning"))
    			idCheckingMode = CheckMode.WARN;
    		else
    			if (mode.equals("on") || mode.equals("strict"))
    				idCheckingMode = CheckMode.STRICT;
    			else
    				if (!mode.equals("off")) {
    					capi.warning(PLUGIN_NAME, 
    							"The value of engine property '" + NO_UNDEFINED_ID_PROPERTY + 
    							"' is ignored as it is neither 'off', 'warning', 'on', nor 'strict'.");
    				}
    	}
    	return idCheckingMode;
    }
    
    /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.ExtensionPointPlugin#fireOnModeTransition(org.coreasm.engine.CoreASMEngine.EngineMode, org.coreasm.engine.CoreASMEngine.EngineMode)
     */
    public void fireOnModeTransition(EngineMode source, EngineMode target) {
    	/* 
    	 * Michael Altenhofen (SAP) suggested that type checking should not 
    	 * be done if the updates are inconsistent. I (Roozbeh) disagree since
    	 * if there is a problem in type-checking, it is an error in the spec
    	 * which can actually be the cause of the inconsistent updates; so,
    	 * it better be reported regardless of the inconsistency of the updates.  
         * -- Jan 2009
         * 
         * if ((source == EngineMode.emAggregation && target == EngineMode.emStepSucceeded) && 
    	 */
        if ((source == EngineMode.emAggregation) && 
            (getTypeCheckMode() != CheckMode.OFF)) {
            checkUpdateSet(target == EngineMode.emStepSucceeded);
        }
        /*
        if (source == EngineMode.emStartingStep) {                
            if ((hasInit && capi.getStepCount() == 0) ||
                (!hasInit && capi.getStepCount() == 1))    {
                setStaticFunctions();
            }
        }
        
        if ((source == EngineMode.emInitializingState)) {
            if (hasInit) {
                capi.getScheduler().setStepCount(-1);
            }
        }
        */
        if (source == EngineMode.emInitializingState) {
        	Interpreter interpreter = capi.getInterpreter();
        	
        	for (Entry<FunctionNode, FunctionElement> entry : functionsWithInit.entrySet()) {
        		FunctionNode functionNode = entry.getKey();
        		FunctionElement function = entry.getValue();
	        	try {
	                interpreter.interpret(functionNode.getInitNode(), interpreter.getSelf());
	            } catch (InterpreterException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	            
	            Element initValue = functionNode.getInitNode().getValue();            
	            
	            if (functionNode.getDomain().size() == 0) {
	                try {
	                    function.setValue(ElementList.NO_ARGUMENT, initValue);
	                } catch (UnmodifiableFunctionException e) {
	                	throw new EngineError("Cannot set initial value for unmodifiable function " + functionNode.getName());
	                }
	            } else {
	                if (initValue instanceof FunctionElement) {
	                    FunctionElement map = (FunctionElement) initValue;
	                    int dSize = functionNode.getDomain().size();
	                    for (Location l: map.getLocations(functionNode.getName())) {
	                    	if (l.args.size() == dSize) {
		                        try {
		                            function.setValue(l.args, map.getValue(l.args));
		                        } catch (UnmodifiableFunctionException e) {
		                        	throw new EngineError("Cannot set initial value for unmodifiable function " + functionNode.getName());
		                        }
	                    	} else {
	                    		if (l.args.size() == 1 
	                    				&& l.args.get(0) instanceof Enumerable 
	                    				&& ((Enumerable)l.args.get(0)).enumerate().size() == dSize) 
	                    		{
	    	                        try {
	    	                            function.setValue(ElementList.create(((Enumerable)l.args.get(0)).enumerate()), map.getValue(l.args));
	    	                        } catch (UnmodifiableFunctionException e) {
	    	                        	throw new EngineError("Cannot set initial value for unmodifiable function " + functionNode.getName());
	    	                        }
	                    		}
	                    		else
		                        	throw new EngineError("Initial value of function " + functionNode.getName() + " does not match the function signature.");
	                    	}
		                        
	                    }                                  
	                }
	            }
	            function.setFClass(functionNode.getFunctionClass());
	        }
        }
    }
    
//    private void setStaticFunctions() {
//        for (String name: functions.keySet()) {
//            if (functionClass.get(name)!=null) {
//                if (functionClass.get(name).equals(FunctionClass.fcStatic)) {
//                    functions.get(name).setFClass(FunctionClass.fcStatic);
//                }
//            }
//        }       
//    }

    /*
     * This method is used to check the current update set
     * to make sure that all the values that are assigned 
     * to functions defined through the signature plugin 
     * are valid with respect to the signature of the function.
     */
    private void checkUpdateSet(boolean isUpdateSuccessful) {
        for (Update u: capi.getUpdateSet(0)) {
        	final AbstractStorage storage = capi.getStorage();
        	final String fname = u.loc.name;
        	
        	if (!storage.isFunctionName(fname)) {
        		String msg = "Function " + fname + " does not exists but there is an update for it.";
        		msg = msg + getContextInfo(u);
        		if (isUpdateSuccessful) {
        			capi.error(msg);
        			return;
        		} else {
        			capi.warning(PLUGIN_NAME, msg);
        			continue;
        		}
        	}
        	
        	FunctionElement func = capi.getStorage().getFunction(fname);
        	
            // function should have a signature
        	if (func.getSignature() == null)
        		continue;

        	// check that the update has enough arguments
        	if (u.loc.args.size() != func.getSignature().getDomain().size()) {
				String message = 
					"The arity of function in update '" + updateToString(u) +  
					"' does not match the signature of function '" +
					u.loc.name + ": " + func.getSignature() + "'." + 
				getContextInfo(u);
				
				if (typeCheckingMode == CheckMode.STRICT) {
					if (isUpdateSuccessful) {
					 	capi.error("Error: " + message);
					 	return;
					} else { 
						capi.warning(PLUGIN_NAME, message);
						continue;
					}
				}
				else if (typeCheckingMode == CheckMode.WARN) {
					logger.warn(message);
				}

        	} else {
        		// if the sizes matches
        	  	// check that all the arguments are in the domains of the function
            	int i = 0;
            	for (String domName: func.getSignature().getDomain()) {
            		final Element arg = u.loc.args.get(i);
            		if (!arg.equals(Element.UNDEF) ) {
	                    AbstractUniverse domain = capi.getStorage().getUniverse(domName);
	                    if (domain != null) {
	                    	if (!domain.member(arg)) {
	                            String message = 
	        						"The " + Tools.getIth(i+1) + " argument in update '" + updateToString(u) +  
	        						"' is not a member of " + domName + " and does not match the signature of " +
	        						"function '" + u.loc.name + ": " + func.getSignature() + "'." +
	                            	getContextInfo(u);
	                            	
	                            	/*
	                            	"There is an update for function '" + u.loc.name + 
	                                " : " + func.getSignature() + 
	                                "' with its " + Tools.getIth(i) + " argument being '" + u.value+ "' which is not " +
	                           		"in the domain of the function (i.e., not a member of " + domName + ".";
	                           		*/
	                            
	                            if (typeCheckingMode == CheckMode.STRICT) {
	                            	if (isUpdateSuccessful) {
	                            		capi.error("Error: " + message);
	                            		return;
	                            	} else {
	                            		capi.warning(PLUGIN_NAME, message);
	                            		continue;
	                            	}
	                            }
	                            else if (typeCheckingMode == CheckMode.WARN) {
	                                logger.warn(message);
	                            }
	                        }
	                    }
	                    else {
	                        capi.error("Could not find universe '" + domName + "'.");
	                        return;
	                    }
            		}
                    i++;
            	}
        	}
        	
        	// check that the value is in the range
        	if (!u.value.equals(Element.UNDEF)) {
	            String rangeName = func.getSignature().getRange();
	            AbstractUniverse range = capi.getStorage().getUniverse(rangeName);
	            if (range != null) {
	                if (!range.member(u.value)) {
	                    String message = 
							"The value of update '" + updateToString(u) +  
							"' is not a member of " + rangeName + " and does not match the signature of " +
							"function '" + u.loc.name + ": " + func.getSignature() + "'." +
	                    	getContextInfo(u);
	                    	/*
	                    	"There is an update for function '" + u.loc.name + 
	                        " : " + func.getSignature() + 
	                        "' with '" + u.value+ "' which is not a member of " + 
	                        rangeName + ".";
	                        */
	                    
	                    if (typeCheckingMode == CheckMode.STRICT) {
	                    	if (isUpdateSuccessful) {
	                    		capi.error("Error: " + message);
	                    		return;
	                    	} else {
	                    		capi.warning(PLUGIN_NAME, message);
	                    		continue;
	                    	}
	                    }
	                    else if (typeCheckingMode == CheckMode.WARN) {
	                    	logger.warn(message);
	                    }
	                }
	            }
	            else {
	                capi.error("Could not find universe '" + rangeName + "'.");
	                return;
	            }
        	}
        }
    }
    
    private String getContextInfo(Update u) {
    	StringBuffer result = new StringBuffer();
    	if (u.sources != null) {
    		result.append(Tools.getEOL() + EngineTools.getContextInfo("", u, capi.getParser(), capi.getSpec()));
//    		org.coreasm.engine.parser.Parser parser = capi.getParser();
//    		Specification spec = capi.getSpec();
//    		result.append(Tools.getEOL() + "Check the following " + ((u.sources.size()>1)?"lines":"line") + " of the specification:" + Tools.getEOL());
//    		for (ScannerInfo info: u.sources) {
//    			result.append("  - " + info.getContext(parser, spec));
//    		}
    	}
    	
    	return result.toString();
    }
    
    /*
     * A special toString() method for updates
     */
    private String updateToString(Update u) {
    	String result = u.loc.name + "(";
    	for (Element e: u.loc.args)
    		result = result + e.toString() + ", ";
    	final int i = result.lastIndexOf(',');
    	if (i > 0)
    		result = result.substring(0, i);
    	
    	return result + "):=" + u.value;
    }
    
    /**
     * 
     */
    private void processSignatures() {
    	// Don't do anything if the spec is not parsed yet
    	if (capi.getSpec().getRootNode() == null)
    		return;
    	
        processingSignatures = true;
        if (functions == null) {        
            functions = new HashMap<String,FunctionElement>();
            //functionClass = new HashMap<String,FunctionClass>();
        }
        if (universes == null) {
            universes = new HashMap<String,UniverseElement>();            
        }
        if (backgrounds == null) {
            backgrounds = new HashMap<String,BackgroundElement>();
        }
        if (rules == null) {
            rules = new HashMap<String,RuleElement>();
        }
        if (functionsWithInit == null)
        	functionsWithInit = new IdentityHashMap<FunctionNode, FunctionElement>();
        
        ASTNode node = capi.getParser().getRootNode().getFirst();
        
    	Interpreter interpreter = capi.getInterpreter().getInterpreterInstance(); 

    	while (node != null) {
            if ((node.getGrammarRule() != null) && node.getGrammarRule().equals("Signature")) {
                ASTNode currentSignature = node.getFirst();
                

            	while (currentSignature != null) {
                    if (currentSignature instanceof EnumerationNode) {
                        createEnumeration(currentSignature, interpreter);
                    }
                    else if (currentSignature instanceof FunctionNode) {
                        createFunction(currentSignature, interpreter);
                    }
                    else if (currentSignature instanceof UniverseNode) {
                        createUniverse(currentSignature, interpreter);
                    }
                    else if (currentSignature instanceof DerivedFunctionNode) {
                    	createDerivedFunction(currentSignature, interpreter);
                    }
                    
//                    else if (currentSignature.getToken().equals("checkmode")) {
//                        setCheckMode(currentSignature);
//                    }
//                    else if (currentSignature.getToken().equals("alias")) {
//                        createAlias(currentSignature);
//                    }
                    
                    
                    currentSignature = currentSignature.getNext();
                }
            }
            
            node = node.getNext();            
//            if (node == null) {
//                Logger.log(Logger.INFORMATION, Logger.global,
//                        "No signatures are specified.");
//                processingSignatures = false;
//                return;
//            }
        }        
        
        // if the user did not define a range function, 
        // add the default range function to functions
        if (functions.get(FunctionRangeFunctionElement.FUNCTION_NAME) == null)
        	functions.put(FunctionRangeFunctionElement.FUNCTION_NAME,
        			new FunctionRangeFunctionElement());
        // if the user did not define a domain function, 
        // add the default domain function to functions
        if (functions.get(FunctionDomainFunctionElement.FUNCTION_NAME) == null)
        	functions.put(FunctionDomainFunctionElement.FUNCTION_NAME,
        			new FunctionDomainFunctionElement());
        
        processingSignatures = false;
    }   
    
    private void addUniverse(String name, UniverseElement universe, ASTNode node, Interpreter interpreter) {
    	/*
        if (universes.containsKey(name)) {
            capi.error("Universe with name '"+name+"' already exists.",node);
        }        
        */
    	if (checkNameUniqueness(name, "universe", node, interpreter))
    		universes.put(name, universe);
    }
    
    private void addBackground(String name, BackgroundElement background, ASTNode node, Interpreter interpreter) {
    	/*
        if (backgrounds.containsKey(name)) {
            capi.error("Background with name '"+name+"' already exists.",node);
        }        
        */
    	if (checkNameUniqueness(name, "background", node, interpreter))
        backgrounds.put(name, background);
    }
    
    private void addFunction(String name, FunctionElement function, ASTNode node, Interpreter interpreter) {
    	/*
        if (functions.containsKey(name)) {
            capi.error("Function with name '"+name+"' already exists.",node);
        } 
        */
    	if (checkNameUniqueness(name, "function", node, interpreter))
    		functions.put(name, function);
    }
    
    private boolean checkNameUniqueness(String name, String type, ASTNode node, Interpreter interpreter) {
    	boolean result = true;
        if (rules.containsKey(name)) {
        	throw new CoreASMError("Cannot add " + type + " '" + name + "'." + 
            		" A derived function with the same name already exists.", node);
        }
        if (functions.containsKey(name)) {
//            capi.error("Cannot add " + type + " '" + name + "'." + 
//            		" A function with the same name already exists.", node, interpreter);
//            result = false;
        	throw new CoreASMError("Cannot add " + type + " '" + name + "'." + 
            		" A function with the same name already exists.", node);
        }
        if (universes.containsKey(name)) {
        	throw new CoreASMError("Cannot add " + type + " '" + name + "'." + 
            		" A universe with the same name already exists.", node);
        }
        if (backgrounds.containsKey(name)) {
        	throw new CoreASMError("Cannot add " + type + " '" + name + "'." + 
            		" A background with the same name already exists.", node);
        }
        return result;
    }
    
    private void createUniverse(ASTNode currentSignature, Interpreter interpreter) {
        UniverseNode universeNode = (UniverseNode) currentSignature;        
        UniverseElement u = new UniverseElement();
        
        addUniverse(universeNode.getName(),u,currentSignature, interpreter);
        
        ASTNode member = universeNode.getFirst().getNext();
        
        while (member!=null) {
            Element e = new EnumerationElement(member.getToken());
            u.member(e, true);
            MapFunction f = new MapFunction();
            
            try {
                f.setValue(ElementList.NO_ARGUMENT, e);
            } catch (UnmodifiableFunctionException e1) {
                capi.error("Cannot modify unmodifiable function.", universeNode, interpreter);
            }
            
            f.setFClass(FunctionClass.fcStatic);            
            addFunction(member.getToken(), f, universeNode, interpreter);
            member = member.getNext();
        }        
    }
    
    /**
     * @param currentSignature
     */
    private void createEnumeration(ASTNode currentSignature, Interpreter interpreter) {
        EnumerationNode enumerationNode = (EnumerationNode) currentSignature;
        List<EnumerationElement> members = enumerationNode.getMembers();
        String enumName = enumerationNode.getName();
        EnumerationBackgroundElement background = new EnumerationBackgroundElement(members);
        
        for (EnumerationElement e: members) {
            MapFunction f = new MapFunction();
            try {
                f.setValue(ElementList.NO_ARGUMENT,e);
            } catch (UnmodifiableFunctionException e1) {
                capi.error("Cannot modify unmodifiable function.", enumerationNode, interpreter);
            }
            f.setFClass(FunctionClass.fcStatic);
            addFunction(e.getName(),f,enumerationNode, interpreter);
            e.setBackground(enumName);
        }
        
        addBackground(enumName,background,enumerationNode, interpreter);
    }

    
    /**
     * @param currentSignature
     */
    private void createFunction(ASTNode currentSignature, Interpreter interpreter) {
        FunctionNode functionNode = (FunctionNode) currentSignature;
        MapFunction function = null;
        
        if (functionNode.getName().equals(AbstractStorage.PROGRAM_FUNCTION_NAME)) {
            // TODO: check signature for correct signature of program function
            function = (MapFunction) capi.getStorage().getFunction(AbstractStorage.PROGRAM_FUNCTION_NAME);
        }
        else if (functionNode.hasInitializer())
        	function = new DerivedMapFunction(capi, functionNode.getInitializerParams(), functionNode.getInitNode());
        else
        	function = new MapFunction();        

        Signature signature = new Signature();        
        signature.setDomain(functionNode.getDomain());
        signature.setRange(functionNode.getRange());
        function.setSignature(signature);

        if (!functionNode.getName().equals(AbstractStorage.PROGRAM_FUNCTION_NAME)) {
            addFunction(functionNode.getName(),function,functionNode, interpreter);
        }
    
        
        if (!functionNode.hasInitializer() && functionNode.getInitNode()!=null)
        	functionsWithInit.put(functionNode, function);
        else
        	function.setFClass(functionNode.getFunctionClass());
    }
    
    private void createDerivedFunction(ASTNode currentSignature, Interpreter interpreter) {
        DerivedFunctionNode derivedFuncNode = (DerivedFunctionNode) currentSignature;        

        ASTNode exprNode = derivedFuncNode.getExpressionNode();
		ASTNode idNode = derivedFuncNode.getNameSignatureNode().getFirst();
		
		// create structure for all parameters
		ArrayList<String> params = new ArrayList<String>();
		ASTNode currentParams = idNode.getNext();
		// while there are parameters to add to the list
		while (currentParams != null) {
			// add parameters to the list
			params.add(currentParams.getToken());
			// get next parameter
			currentParams = currentParams.getNext();
		}
		params.trimToSize();

		DerivedFunctionElement func = new DerivedFunctionElement(capi, params, exprNode);
		
		addFunction(idNode.getToken(), func, currentSignature, interpreter);

		/* 
         * The following code creates a new rule that returns 
         * the computed value. It is replaced with the code 
         * above.
        
        // create a "return Term in skip" rule node
        ASTNode ruleNode = new ReturnRuleNode(derivedFuncNode.getScannerInfo());
        ruleNode.addChild(derivedFuncNode.getExpressionNode());
        ruleNode.addChild(new SkipRuleNode(derivedFuncNode.getScannerInfo()));        
        
		// get name (ID) node of the function
		ASTNode idNode = derivedFuncNode.getNameSignatureNode().getFirst();
		
		// create structure for all parameters
		ArrayList<String> params = new ArrayList<String>();
		ASTNode currentParams = idNode.getNext();
		// while there are parameters to add to the list
		while (currentParams != null) {
			// add parameters to the list
			params.add(currentParams.getToken());
			// get next parameter
			currentParams = currentParams.getNext();
		}

		RuleElement rule = new RuleElement(idNode.getToken(), params, ruleNode);
		
		addRule(rule.getName(), rule, currentSignature);
		*/
    }
    
    /*
    private void setCheckMode(ASTNode currentSignature) {
        String checkModeString = currentSignature.getFirst().getToken();
        
            typeCheckingMode = CheckMode.cmStrict;
        }
        if (checkModeString.equals("strict")) {
        else if (checkModeString.equals("warn")) {
            typeCheckingMode = CheckMode.cmWarn;
        }
        else if (checkModeString.equals("off")) {
            typeCheckingMode = CheckMode.cmOff;
        }
    }
    */
        
    /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
     */
    public Map<String,FunctionElement> getFunctions() {
        if (functions == null) {
            processSignatures();
        }
        return functions;
    }

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		if (rules == null)
			processSignatures();
		return rules;
	}

   /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
     */
    public Map<String,UniverseElement> getUniverses() {
        if (universes == null) {
            processSignatures();
        }
        return universes;
    }

    /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
     */
    public Map<String,BackgroundElement> getBackgrounds() {
        if (backgrounds == null) {
            processSignatures();
        }
        return backgrounds;
    }

	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Set<String> getUniverseNames() {
		return getUniverses().keySet();
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}
    
    public void handleUndefinedIndentifier(Interpreter interpreter, ASTNode pos, String id, List<? extends Element> args) {
    	getIdCheckMode();
        if (processingSignatures || idCheckingMode != CheckMode.OFF) {
            if (functions.keySet().contains(id)) {
                FunctionElement f = functions.get(id);
                Location l = new Location(id,args);
                pos.setNode(l, null, f.getValue(l.args));
            }
            else {
            	String msg = "unknown identifier \""+id+"\".";
            	
				if (idCheckingMode == CheckMode.STRICT) {
					capi.error("Error: " + msg, pos, interpreter);
					return;
				}
				else if (idCheckingMode == CheckMode.WARN) {
					logger.warn(msg);
				}
            }
        }
    }

	@Override
	public Set<String> getDependencyNames() {
		if (dependencyNames == null) {
			dependencyNames = new HashSet<String>();
			dependencyNames.add("SetPlugin");
			dependencyNames.add("ListPlugin");
			dependencyNames.add("TurboASMPlugin");
		}
		return dependencyNames;
	}
    
}
