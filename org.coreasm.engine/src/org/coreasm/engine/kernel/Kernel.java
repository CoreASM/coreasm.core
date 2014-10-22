/*	
 * Kernel.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 George Ma
 * Copyright (C) 2005 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.kernel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Token;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.Engine;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanBackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionBackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.MapFunction;
import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginAggregationAPI.Flag;
import org.coreasm.engine.absstorage.PluginCompositionAPI;
import org.coreasm.engine.absstorage.RuleBackgroundElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParseMap;
import org.coreasm.engine.parser.ParseMap2;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.PluginServiceInterface;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.kernel.CompilerKernelPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Provides essential services to Kernel.
 *   
 * @author  George Ma and Roozbeh Farahbod
 * 
 */

public class Kernel extends Plugin 
		implements VocabularyExtender, Aggregator, OperatorProvider, ParserPlugin, PluginServiceInterface {


	private static final Logger logger = LoggerFactory.getLogger(Kernel.class);
	
	public static final VersionInfo VERSION_INFO = Engine.VERSION_INFO;

	public static final String PLUGIN_NAME = Kernel.class.getSimpleName();

	/** Grammar Rule Names */
	public static final String GR_COREASM = "CoreASM";
	public static final String GR_USE_CLAUSE = "UseClauses";
	public static final String GR_HEADER = "Header";
	public static final String GR_NOSIGNATURE = "NoSignature";
	public static final String GR_ID = "ID";
	public static final String GR_INITIALIZATION = "Initialization";
	public static final String GR_RULEDECLARATION = "RuleDeclaration";
	public static final String GR_RULEDECLARATION_LIST = "RuleDeclarationList";
	public static final String GR_BOOLEAN_TERM = "BooleanTerm";
	public static final String GR_SKIP = "SkipRule";
	public static final String GR_FUNCTION_RULE_TERM = "FunctionRuleTerm";
	public static final String GR_TUPLE_TERM = "TupleTerm";
	public static final String GR_RULEELEMENT_TERM = "RuleElementTerm";
	public static final String GR_RULE_OR_FUNCTION_ELEMENT_TERM = "RuleOrFunctionElementTerm";
	public static final String GR_EXTENDED_TERM = "ExtendedTerm";
	public static final String GR_EXPRESSION = "Expression";
	public static final String GR_TERM = "Term";
	public static final String GR_GUARD = "Guard";
	
	/** keywords */
	public static final String KW_COREASM = "CoreASM";
	public static final String KW_USE = "use";
	public static final String KW_INIT = "init";
	public static final String KW_SKIP = "skip"; 
	public static final String KW_TRUE = "true";
	public static final String KW_FALSE = "false";
	public static final String KW_UNDEF = "undef";
	public static final String KW_SELF = "self";
	public static final String KW_RULEELEMENT = "ruleelement";
	public static final String KW_NOSIGNATURE = "nosignature";
    
	/** operators */
	public static final String OP_RULE_OR_FUNCTION_ELEMENT = "@";
    private static final String EQUALITY_OP = "=";
	
	/** List of kernel parsers */
	private Map<String, GrammarRule> parsers = null;
	
	private final Set<String> universeNames;
	private final Set<String> backgroundNames;
	private Map<String, FunctionElement> functions = null;
	private Map<String, UniverseElement> universes = null;
	
	private Map<String,BackgroundElement> backgroundElements = null;
	private Map<String,RuleElement> ruleElements = null;

	/** The object that creates the expression grammar rule */
	private ExpressionParserFactory exprFactory = null;
	
    /** List of update actions provided by this plugin (empty). */
    public static final String[] UPDATE_ACTIONS = {};
    
    private Map<String, Parser<Node>> exposedParsers = null;

    // OLD LAZY-PARSERS FROM OLD PARSER
    //private final Parser<Node>[] tupleTermParserArray = new Parser[1];
    //private final Parser<Node> tupleTermParser = ParserTools.lazy("TupleTerm", tupleTermParserArray);
    //private final Parser<Node>[] ruleParserArray = new Parser[1];
    //private final Parser<Node> ruleParser = ParserTools.lazy("Rule", ruleParserArray);
    //private final Parser<Node>[] termParserArray = new Parser[1];
    //private final Parser<Node> termParser = ParserTools.lazy("Term", termParserArray);
    //private final Parser<Node>[] constantTermParserArray = new Parser[1];
    //private final Parser<Node> constantTermParser = ParserTools.lazy("ConstantTerm", constantTermParserArray);
    //private final Parser<Node>[] basicTermParserArray = new Parser[1];
    //private final Parser<Node> basicTermParser = ParserTools.lazy("BasicTerm", basicTermParserArray);
    //private final Parser<Node>[] funcRuleTermParserArray = new Parser[1];
    //private final Parser<Node> funcRuleTermParser = ParserTools.lazy("FunctionRuleTerm", funcRuleTermParserArray);
    //private final Parser<Node>[] headerParserArray = new Parser[1];
    //private final Parser<Node> headerParser = ParserTools.lazy("Header", headerParserArray);
    //private final Parser<Node>[] ruleSignatureParserArray = new Parser[1];
    //private final Parser<Node> ruleSignatureParser = ParserTools.lazy("RuleSignature", ruleSignatureParserArray);

    private final String[] keywords = {"CoreASM", "nosignature", "use", "init", "rule", 
    		"ruleelement", "skip", "import", "do", "undef", "true", "false", "self"};
    private final String[] operators = {"=", "(", ")", ",", "@", ":=", "!!"};
         
    private final Parser.Reference<Node> refTupleTermParser = Parser.newReference();
    private final Parser.Reference<Node> refRuleParser = Parser.newReference();
    private final Parser.Reference<Node> refHeaderParser = Parser.newReference();
    private final Parser.Reference<Node> refTermParser = Parser.newReference();
    private final Parser.Reference<Node> refFuncRuleTermParser = Parser.newReference();
    private final Parser.Reference<Node> refConstantTermParser = Parser.newReference();
    private final Parser.Reference<Node> refBasicTermParser = Parser.newReference();
    private final Parser.Reference<Node> refRuleSignatureParser = Parser.newReference();
    private final Parser.Reference<Node> refBasicExprParser = Parser.newReference();
    private final Parser.Reference<Node> refRuleDeclarationParser = Parser.newReference();
    
    //compiler plugin
    private CompilerPlugin compilerPlugin;
    
    /**
     * Creates a new Kernel plugin.
     */
    public Kernel() {
    	universeNames = new HashSet<String>();
		universeNames.add(AbstractStorage.AGENTS_UNIVERSE_NAME);

    	backgroundNames = new HashSet<String>();
		backgroundNames.add(BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME);
		backgroundNames.add(FunctionBackgroundElement.FUNCTION_BACKGROUND_NAME);
		backgroundNames.add(RuleBackgroundElement.RULE_BACKGROUND_NAME);
		
		compilerPlugin = new CompilerKernelPlugin();
    }
 
	@Override
	public void setControlAPI(ControlAPI capi) {
		super.setControlAPI(capi);
	}

	/**
	 * Returns an instance of {@link KernelServices}. 
	 */
	@Override
	public PluginServiceInterface getPluginInterface() {
		return new KernelServices(this);
	}

	
	public Set<Parser<? extends Object>> getLexers() {
		HashSet<String> kws = new HashSet<String>();
		HashSet<String> oprs = new HashSet<String>();
       	Set<Parser<? extends Object>> lexers = new HashSet<Parser<? extends Object>>();

       	// Getting keywords and operators from all other plugins
       	Set<Plugin> plugins = capi.getPlugins();
       	for (Plugin p: plugins) 
       		if (p instanceof ParserPlugin) {
       			ParserPlugin pp = (ParserPlugin)p;
       			for (String kw: pp.getKeywords())
       				kws.add(kw);
       			for (String opr: pp.getOperators())
       				oprs.add(opr);
       			if ( p != this )
       				lexers.addAll(pp.getLexers());
       		}

       	// initializing the parser tools instance
       	String[] kwsArray = new String[0];
       	String[] oprsArray = new String[0];
       	kwsArray = kws.toArray(kwsArray);
       	oprsArray = oprs.toArray(oprsArray);
       	
       	ParserTools parserTools = ParserTools.getInstance(capi);
       	parserTools.init(kwsArray, oprsArray, lexers);
       	
       	return lexers;
	}

	/**
	 * Exposes some of the kernel grammar rule parsers to other plug-ins. 
	 * The following non-terminals are accepted:
	 * <p>
	 * Rule, Term, ConstantTerm, BasicTerm, FunctionRuleTerm,
	 * Header, RuleSignature
	 * 
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		if (exposedParsers == null) {
			exposedParsers = new HashMap<String, Parser<Node>>();
			exposedParsers.put("Rule", refRuleParser.lazy());
			exposedParsers.put("Term", refTermParser.lazy());
			exposedParsers.put("ConstantTerm", refConstantTermParser.lazy());
			exposedParsers.put("BasicTerm", refBasicTermParser.lazy());
			exposedParsers.put("FunctionRuleTerm", refFuncRuleTermParser.lazy());
			exposedParsers.put("Header", refHeaderParser.lazy());
			exposedParsers.put("RuleSignature", refRuleSignatureParser.lazy());
			exposedParsers.put("TupleTerm", refTupleTermParser.lazy());
			exposedParsers.put("BasicExpr", refBasicExprParser.lazy());
			exposedParsers.put("RuleDeclaration", refRuleDeclarationParser.lazy());
		}
		return exposedParsers.get(nonterminal);
	}

	/* 
	 * old code
	 * 
    public Map<String, GrammarRule> getParsers() {
    	
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();

			// TODO this can be done in a nicer way
			// Here we have to call getLexer() so that the 
			// ParserTools also gets initialized
			getLexers();

	       	Parser<Node> delimParser = parserTools.getDelimiterParser(); 
	    	Parser<Node> optionalDelimParser = parserTools.getOptionalDelimiterParser(); 
	    	Parser<Node> idParser = parserTools.getIdentifierParser(); 
	    	// CoreASM : 'CoreASM' ID ( UseClause )* ( Header )* 'init' ID
	    	Parser<Node> coreASMParser = Parsers.mapn(new Parser[] {
	    			optionalDelimParser,
	    			parserTools.getKeywordParser("CoreASM", this.getName()),
	    			delimParser,
	    			idParser,
	    			delimParser,
	    			},
	    			new CoreASMParseMap()
	    			);
	    	parsers.put("CoreASM", new GrammarRule("CoreASM", "'CoreASM' ID ( UseClause )*", coreASMParser, this.getName()));
		}
    	
    	return parsers;
    	
    }
    */
 	
    public Map<String, GrammarRule> getParsers() {

		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();

			ParserTools parserTools = ParserTools.getInstance(capi);
			
			// TODO this can be done in a nicer way
			// Here we have to call getLexer() so that the 
			// ParserTools also gets initialized
			// jetzt �berfl�ssig?
			Set<Parser<? extends Object>> lexers = getLexers();
			Parser<Object> tokenizer = Parsers.or(lexers);
			
			// Ignore-Parser
			Parser<Void> ignoreParser = 
					Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();
			
			Parser<Node> idParser = parserTools.getIdParser();
	
	    	// UseClause : 'use' ID
	    	Parser<Node> useClauseParser = Parsers.sequence(
	    			parserTools.getKeywParser("use", this.getName()),
	    			idParser,
	    			new ParseMap2(getName()) {
	
						public Node map(Node a, Node b) {
							Node node = new ASTNode(
									pluginName,
									ASTNode.DECLARATION_CLASS,
									Kernel.GR_USE_CLAUSE,
									null,
									a.getScannerInfo());
							node.addChild(a);
							node.addChild(b);
							return node;
						}
	    			}
	    			);
	    	parsers.put("UseClause", new GrammarRule("UseClause", "'use' ID", useClauseParser, this.getName()));

	    	createHeaderParser();
	    	
	    	// Inititialization: 'init' ID
	    	Parser<Node> initializationParser = Parsers.sequence(
	    			parserTools.getKeywParser("init", this.getName()),
	    			idParser,
	    			new ParseMap2(getName()) {
	
						public Node map(Node a, Node b) {
							Node node = new ASTNode(
									null,
									null,
									Kernel.GR_INITIALIZATION,
									null,
									a.getScannerInfo()
									);
							node.addChild(a);
							node.addChild(b);
							return node;
						}
	    				
	    			});
	    	parsers.put("Initialization", new GrammarRule("Initialization", "'init' ID", initializationParser, this.getName()));
	    	
	    	
	    	// RuleSignature : ID ( '(' ID (',' ID)* ')' )?
	    	Parser<Node> rulesignParser = Parsers.array(new Parser[] {
	    			idParser,
					Parsers.array(
							parserTools.getOprParser("("),
	    					parserTools.csplus(idParser),
	    					parserTools.getOprParser(")")
	    					).optional(),
	    			}).map(new ParserTools.RuleSignatureParseMap());
	    	refRuleSignatureParser.set(rulesignParser);
	    	parsers.put("RuleSignature", new GrammarRule("RuleSignature", "ID ( '(' ID (',' ID)* ')' )?", refRuleSignatureParser.lazy(), this.getName()));
	    	 
	
	    	// Rule : ...
	    	createRuleParser(parsers);
	    	
	    	// RuleDeclaration : 'rule' RuleSignature '=' Rule
	    	Parser<Node> ruleDeclarationParser = Parsers.array(new Parser[] {
	    			parserTools.getKeywParser("rule", this.getName()),
	    			refRuleSignatureParser.lazy(),
	    			parserTools.getOprParser("="),
	    			refRuleParser.lazy()}
	    			
	    			).map(new ParserTools.RuleDeclarationParseMap());
	    	refRuleDeclarationParser.set(ruleDeclarationParser);
	    	parsers.put("RuleDeclaration", new GrammarRule("RuleDeclaration", "'rule' RuleSignature '=' Rule", refRuleDeclarationParser.lazy(), this.getName()));
	
	    	// CoreASM : 'CoreASM' ID ( UseClause | Header | 'init' ID | RuleDeclaration)*
	    	Parser<Node> coreASMParser = Parsers.array(new Parser[] {
	    			parserTools.getKeywParser("CoreASM", this.getName()),
	    			idParser,
	    			parserTools.star(
	    					Parsers.or(
	    							useClauseParser,
	    							refHeaderParser.lazy(),
	    							initializationParser,
	    							ruleDeclarationParser
	    						)
	    				)
	    			}).map(new ParserTools.CoreASMParseMap())
	    			.followedBy(Parsers.EOF);
	    	parsers.put("CoreASM", new GrammarRule("CoreASM", 
	    			"'CoreASM' ID ( UseClause | Header | 'init' ID | RuleDeclaration)*", 
	    			coreASMParser, this.getName()));
		}
    	
    	return parsers;
    	
    }
    
    private void createHeaderParser() {
    	List<Parser<Node>> headerParsers = new ArrayList<Parser<Node>>();
    	
    	ParserTools parserTools = ParserTools.getInstance(capi);
    	
    	parsers.put("Header", new GrammarRule("Header", "'nosignature'", refHeaderParser.lazy(), this.getName()));

    	// Header : 'nosignature'
    	headerParsers.add(parserTools.getKeywParser("nosignature", this.getName()));
    	
       	// Getting header parsers from all the plugins
       	Set<Plugin> plugins = capi.getPlugins();
       	for (Plugin p: plugins) 
       		if (p instanceof ParserPlugin && p != this) {
       			GrammarRule gRule = ((ParserPlugin)p).getParsers().get("Header");
       			if (gRule != null) {
       				headerParsers.add(gRule.parser);
       			}
       		}
       	
       	Parser<Node> headerParser = Parsers.or(headerParsers);
       	refHeaderParser.set(headerParser);
       	
    }
    
    /*
     * Creates a parser to parse ASM Rules. It gathers all 
     * the pieces from other plug-ins and creates the Rule parser.
     * 
     */
    private void createRuleParser(Map<String, GrammarRule> parsers) {
    	List<Parser<Node>> rules = new ArrayList<Parser<Node>>();
    	
    	ParserTools parserTools = ParserTools.getInstance(capi);
    	Parser<Node> idParser = parserTools.getIdParser();
    	
    	// Rule : ... // open for future extensions
    	parsers.put("Rule", 
    			new GrammarRule("Rule", "", refRuleParser.lazy(), PLUGIN_NAME));
    	
    	// Rule : 'skip'
    	Parser<Node> skipRuleParser = parserTools.getKeywParser("skip", PLUGIN_NAME).map(
    			new ParseMap<Node, Node>(PLUGIN_NAME) {
					public Node map(Node v) {
						return new SkipRuleNode(v.getScannerInfo());
					}});
    	parsers.put("SkipRule", new GrammarRule("SkipRule", "'skip'", skipRuleParser, PLUGIN_NAME));
    	rules.add(skipRuleParser);
    	
    	createTermParser(parsers);

       	// UpdateRule : FunctionRuleTerm ':=' Term
       	Parser<Node> updateRuleParser = Parsers.array(
       			refFuncRuleTermParser.lazy(),
       			parserTools.getOprParser(":="),
       			refTermParser.lazy()
       			).map(new UpdateRuleParseMap());
       	parsers.put("UpdateRule", 
       			new GrammarRule("UpdateRule",
       					"FunctionRuleTerm ':=' Term", updateRuleParser, PLUGIN_NAME));
       	rules.add(updateRuleParser);
       	
       	// MacroCallRule : FunctionRuleTerm
       	Parser<Node> macroCallRule = Parsers.array(refFuncRuleTermParser.lazy()).map(
       			new ParseMap<Object[], Node>(PLUGIN_NAME) {

					public Node map(Object[] vals) {
						Node node = new MacroCallRuleNode(((Node)vals[0]).getScannerInfo());
						node.addChild("alpha", (Node)vals[0]);
						return node;
					}
       				
       			});
       	parsers.put("MacroCallRule",
       			new GrammarRule("MacroCallRule", "FunctionRuleTerm", macroCallRule, PLUGIN_NAME));
       	
       	// ImportRule : 'import' ID 'do' Rule 
       	Parser<Node> importRuleParser = Parsers.array(
       			parserTools.getKeywParser("import", PLUGIN_NAME),
       			idParser,
       			parserTools.getKeywParser("do", PLUGIN_NAME),
       			refRuleParser.lazy()
       			).map(new ImportRuleParseMap());
       	parsers.put("ImportRule", 
       			new GrammarRule("ImportRule",
       					"'import' ID 'do' Rule", importRuleParser, PLUGIN_NAME));
       	rules.add(importRuleParser);
       	
       	
       	// Getting rule parsers from all the plugins
       	Set<Plugin> plugins = capi.getPlugins();
       	for (Plugin p: plugins) 
       		if (p instanceof ParserPlugin && p != this) {
       			GrammarRule gRule = ((ParserPlugin)p).getParsers().get("Rule");
       			if (gRule != null) {
       				rules.add(gRule.parser);
       			}
       		}
       	
       	rules.add(macroCallRule);

       	Parser<Node> ruleParser = Parsers.longest(rules);
       	refRuleParser.set(ruleParser);

    }
    
    
    /*
     * Creates a parser to parse terms.
     */
    private Parser<Node> createTermParser(Map<String, GrammarRule> parsers) {
    	
    	ParserTools parserTools = ParserTools.getInstance(capi);
    	Parser<Node> idParser = parserTools.getIdParser();
    	
    	// Term : ... // placeholder for expression to use
       	parsers.put("Term", 
       			new GrammarRule("Term",
       					"Expression | ExtendedTerm", refTermParser.lazy(), PLUGIN_NAME));
    	
    	// TupleTerm: '(' ( Term  ( ',' Term )* )? ')'
    	Parser<Node> ttParser = Parsers.array(        //parserTools.seq(
    			parserTools.getOprParser("("),
				parserTools.csplus(refTermParser.lazy()).optional(),
    			parserTools.getOprParser(")")
			).map(new TupleTermParseMap());
    	refTupleTermParser.set(ttParser);
    	parsers.put("TupleTerm",
    			new GrammarRule("TupleTerm",
    					"'(' ( Term  ( ',' Term )* )? ')'", refTupleTermParser.lazy(), PLUGIN_NAME));

    	// FunctionRuleTerm : ID ( TupleTerm )?
       	createFunctionRuleTermParser();
    	
       	// Term : Expression | ExtendedTerm
       	refTermParser.set( createExpressionParser() );
       	
       	return refTermParser.lazy();
    }
    
    /*
     * Creates a parser to parse function/rule terms
     */
    private void createFunctionRuleTermParser() {

    	ParserTools parserTools = ParserTools.getInstance(capi);
    	Parser<Node> idParser = parserTools.getIdParser();
    	
    	List<Parser<Node>> frterms = new ArrayList<Parser<Node>>();
    	String grammarRule = "BasicFunctionRuleTerm";

    	// BasicFunctionRuleTerm : ID ( TupleTerm )?
       	Parser<Node> basicFunctionRuleTermParser = Parsers.array(
       			new Parser[] {
       				idParser,
       				refTupleTermParser.lazy().optional()
       				}).map(new ParserTools.FunctionRuleTermParseMap());
       	parsers.put("BasicFunctionRuleTerm", 
       			new GrammarRule("BasicFunctionRuleTerm",
       					"ID ( TupleTerm )?", basicFunctionRuleTermParser, PLUGIN_NAME));
       	frterms.add(basicFunctionRuleTermParser);

       	// Getting other function rule parsers from all other plugins
       	Set<Plugin> plugins = capi.getPlugins();
       	for (Plugin p: plugins) 
       		if (p instanceof ParserPlugin && p != this) {
       			GrammarRule gRule = ((ParserPlugin)p).getParsers().get(GR_FUNCTION_RULE_TERM);
       			if (gRule != null) {
       				frterms.add(gRule.parser);
       				grammarRule = grammarRule + " | " + gRule.name;
       			}
       		}
       	
    	// FunctionRuleTerm : BasicFunctionRuleTerm | ...
       	Parser<Node> frtParser = Parsers.longest(frterms);
       	refFuncRuleTermParser.set(frtParser);
       	
       	parsers.put(GR_FUNCTION_RULE_TERM,
    			new GrammarRule(GR_FUNCTION_RULE_TERM, 
    					grammarRule, 
    					refFuncRuleTermParser.lazy(), PLUGIN_NAME));
    }
    
    /*
     * Creates a parser to parse expressions. 
     */
    private Parser<Node> createExpressionParser() {
    	List<Parser<Node>> exps = new ArrayList<Parser<Node>>();
    	
    	ParserTools parserTools = ParserTools.getInstance(capi);
    	Parser<Node> idParsr = parserTools.getIdParser();
    	  	
    	// Expression : ... // Open for future extensions
    	Parser.Reference<Node> refExpParser = Parser.newReference();
    	
    	Parser<Node> funcRuleTermParser = parsers.get("FunctionRuleTerm").parser;
    	
    	// Guard : Term
    	Parser<Node> guardParser = refTermParser.lazy();
    	parsers.put("Guard", 
    			new GrammarRule("Guard", "Term", guardParser, PLUGIN_NAME));
    	
    	// KernelTerms : 'undef' | 'self'
    	Parser<Node> kernelTermsParser = Parsers.or(
    			parserTools.getKeywParser("undef", PLUGIN_NAME),
    			parserTools.getKeywParser("self", PLUGIN_NAME)).map(
    					new ParseMap<Node, Node>(PLUGIN_NAME) {
    						public Node map(Node v) {
    							Node node = new ASTNode(
    									pluginName, 
    									ASTNode.EXPRESSION_CLASS, 
    									"KernelTerms", 
    									v.getToken(), 
    									v.getScannerInfo(), 
    									Node.KEYWORD_NODE);
    							return node;
    						}
    					});
    	parsers.put("KernelTerms", 
    			new GrammarRule("KernelTerms", 
    					"'undef' | 'self'", kernelTermsParser, PLUGIN_NAME));
    	
    	// BooleanTerm : 'true' | 'false'
    	Parser<Node> booleanTermParser = Parsers.or( 
    			parserTools.getKeywParser("true", PLUGIN_NAME),
    			parserTools.getKeywParser("false", PLUGIN_NAME)).map(
    					new ParseMap<Node, Node>(PLUGIN_NAME) {
    						public Node map(Node v) {
    							Node node = new ASTNode(
    									pluginName, 
    									ASTNode.EXPRESSION_CLASS, 
    									"BooleanTerm", 
    									v.getToken(), 
    									v.getScannerInfo(), 
    									Node.KEYWORD_NODE);
    							return node;
    						}
    					});
    	parsers.put("BooleanTerm",
    			new GrammarRule("BooleanTerm", "'true' | 'false'", booleanTermParser, PLUGIN_NAME));
    	
    	createConstantTerm(booleanTermParser, kernelTermsParser);
 
    	createBasicTerm(funcRuleTermParser);
    	
    	// BasicExpr : BasicTerm | '(' Term ')'
    	Parser<Node> beParser = Parsers.or(refBasicTermParser.lazy(), 
    			parserTools.seq(	// '(' Term ')'
    					parserTools.getOprParser("("), 
    					refTermParser.lazy(),
    					parserTools.getOprParser(")")
    					).map(new ParseMap<Object[], Node>(PLUGIN_NAME){

							public Node map(Object[] v) {
								Node node = new EnclosedTermNode(((Node)v[0]).getScannerInfo());
								for (Object o:v) node.addChild((Node)o);
								return node;
							}
							
    					}
    				)
    		);
    	refBasicExprParser.set(beParser);
    	parsers.put("BasicExpr", 
    			new GrammarRule("BasicExpr", "BasicTerm | '(' Term ')'", refBasicExprParser.lazy(), PLUGIN_NAME));
    	
    	// creating an expression parser based on the operators 
    	// provided by the plugins
    	Set<Plugin> plugins = new HashSet<Plugin>();
    	ExpressionParserFactory expFactory = 
    		new ExpressionParserFactory(
    				capi,
    				parserTools,
    				refBasicExprParser.lazy(), 
    				refTermParser.lazy(), 
    				capi.getPlugins());
    	exps.add(expFactory.createExpressionParser());
    	
    	Parser<Node> exp_parser = Parsers.or(exps);
    	refExpParser.set(exp_parser);
    	
    	return refExpParser.lazy();
    }

    /*
     * Creates ConstantTerm gathering pieces from other plugins
     */
    private void createConstantTerm(Parser<Node> booleanTermParser, Parser<Node> undefTermParser) {
    	List<Parser<Node>> cterms = new ArrayList<Parser<Node>>();
    	String grammarRule = "BooleanTerm | UndefTerm";
       	cterms.add(booleanTermParser);
       	cterms.add(undefTermParser);
    	
       	// Getting constant term parsers from all the plugins
       	Set<Plugin> plugins = capi.getPlugins();
       	for (Plugin p: plugins) 
       		if (p instanceof ParserPlugin && p != this) {
       			GrammarRule gRule = ((ParserPlugin)p).getParsers().get("ConstantTerm");
       			if (gRule != null) {
       				cterms.add(gRule.parser);
       				grammarRule = grammarRule + " | " + gRule.name;
       			}
       		}
       	
    	// ConstantTerm : BooleanTerm | UndefTerm | ...
       	Parser<Node> ctParser = Parsers.or(cterms);
       	refConstantTermParser.set(ctParser);
       	
       	parsers.put("ConstantTerm",
    			new GrammarRule("ConstantTerm", grammarRule, refConstantTermParser.lazy(), PLUGIN_NAME));
    	
    }
    
    /*
     * Creates BasicTerm gathering pieces from other plugins
     */
    private void createBasicTerm(Parser<Node> functionRuleTermParser) {

    	ParserTools parserTools = ParserTools.getInstance(capi);
    	Parser<Node> idParser = parserTools.getIdParser();
    	
    	List<Parser<Node>> bterms = new ArrayList<Parser<Node>>();
    	String grammarRule = "FunctionRuleTerm | ConstantTerm";
       	bterms.add(functionRuleTermParser);
       	bterms.add(refConstantTermParser.lazy());
    	
    	// RuleElementTerm : 'ruleelement' ID
    	Parser<Node> ruleElementParser = Parsers.sequence(
    			parserTools.getKeywParser("ruleelement", PLUGIN_NAME),
    			idParser,
    			
    			new ParseMap2(PLUGIN_NAME) {

					public Node map(Node a, Node b) {
						Node node = new ASTNode(
								pluginName,
								ASTNode.EXPRESSION_CLASS,
								Kernel.GR_RULEELEMENT_TERM,
								null,
								a.getScannerInfo()
								);
						node.addChild(a);
						node.addChild("alpha", b);
						return node;
					}
    				
    			}
    	);
       	parsers.put("RuleElementTerm", 
       			new GrammarRule("RuleElementTerm", 
       					"'ruleelement' ID", ruleElementParser, PLUGIN_NAME));
       	
    	// RuleOrFunctionElementTerm : '@' ID
    	Parser<Node> ruleOrFunctionElementParser = Parsers.sequence(
    			parserTools.getOprParser("@"),
    			idParser,
    			new ParseMap2(PLUGIN_NAME) {

					public Node map(Node a, Node d) {
						Node node = new RuleOrFuncElementNode(a.getScannerInfo());
						node.addChild(a);
						node.addChild("alpha", d);
						return node;
					}
    				
    			}
    	);
       	parsers.put("RuleOrFunctionElementTerm", 
       			new GrammarRule("RuleOrFunctionElementTerm", 
       					"'@' ID", ruleOrFunctionElementParser, PLUGIN_NAME));

       	bterms.add(ruleElementParser);
       	bterms.add(ruleOrFunctionElementParser);
     
       	// Getting basic term parsers from all the plugins
       	Set<Plugin> plugins = capi.getPlugins();
       	for (Plugin p: plugins) 
       		if (p instanceof ParserPlugin && p != this) {
       			GrammarRule gRule = ((ParserPlugin)p).getParsers().get("BasicTerm");
       			if (gRule != null) {
       				bterms.add(gRule.parser);
       				grammarRule = grammarRule + " | " + gRule.name;
       			}
       		}
       	
    	// BasicTerm : FunctionRuleTerm | ConstantTerm | ...
       	Parser<Node> btParser = Parsers.longest(bterms);
       	refBasicTermParser.set(btParser);
    	
    	parsers.put("BasicTerm",
    			new GrammarRule("BasicTerm", grammarRule, refBasicTermParser.lazy(), PLUGIN_NAME));
    	
    }
    
    @Deprecated
	public List<GrammarRule> getGrammar() {
		if (parsers == null)
			getParsers();
		
		return new ArrayList<GrammarRule>(parsers.values());
//	        grammar.add(new GrammarRule("VariableTerm",
//	                                    "ID",
//	                                    getName(),
//	                                    org.coreasm.engine.parser.PassThroughObserver.class.getName()));
//	        
//	        grammar.add(new GrammarRule("BasicExpr",
//	                                    "BasicTerm | '(' " + GR_TERM + " ')'",
//	                                    getName(),
//	                                    org.coreasm.engine.parser.PassThroughObserver.class.getName(),
//	                                    GrammarRule.GRType.OP_BOTTOM_LEVEL));
//	        
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void initialize() {
		// Nothing.
    }

	/**
	 * This plugin returns an empty set of instructions.
	 */
	public String[] getUpdateActions() {
		return UPDATE_ACTIONS;
	}

	/**
	 * Basic Update Aggregator.
	 * 
	 * @param pluginAgg plugin aggregation API object.
	 */
	public void aggregateUpdates(PluginAggregationAPI pluginAgg) {
		
		// all locations on which basic updates occur
		Set<Location> basicUpdateLocations = pluginAgg.getLocsWithActionOnly(Update.UPDATE_ACTION);
		
		for (Location l : basicUpdateLocations) {
			// get all updates on the location
			UpdateMultiset updatesOnLoc = pluginAgg.getLocUpdates(l);
			
			// for all updates on the location
			for (Update u : updatesOnLoc)
			{
				// flag the update as succuessful
				pluginAgg.flagUpdate(u,Flag.SUCCESSFUL,this);
				
				// resultant update is this update
				pluginAgg.addResultantUpdate(u,this);
			}
		}
	}

	/**
	 * This is the basic update composer.
	 */
	public void compose(PluginCompositionAPI compAPI) {
		UpdateMultiset updateSet1 = compAPI.getAllUpdates(1);
		UpdateMultiset updateSet2 = compAPI.getAllUpdates(2);
		
		for (Update ui1: updateSet1) {
			if (!locUpdated(updateSet2, ui1.loc) && isBasicUpdate(updateSet1, ui1))
				compAPI.addComposedUpdate(ui1, this);
		}
		
		for (Update ui2: updateSet2) {
			if (isBasicUpdate(updateSet2, ui2))
				compAPI.addComposedUpdate(ui2, this);
		}
	}
	
	private boolean locUpdated(UpdateMultiset uMset, Location l) {
		for (Update u: uMset) 
			if (u.loc.equals(l))
				return true;
		return false;
	}

	private boolean isBasicUpdate(UpdateMultiset uMset, Update u) {
		for (Update update: uMset) 
			if (update.loc.equals(u.loc) && !update.action.equals(Update.UPDATE_ACTION))
				return false;
		return true;
	}
	
//	--------------------------------
	// Vocabulary Extender Interface
	//--------------------------------
	
	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
	 */
	public Map<String,FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			
			// self is not a function anymore
			// functions.put(SelfFunctionElement.NAME, new SelfFunctionElement());
			functions.put(AbstractStorage.PROGRAM_FUNCTION_NAME, new MapFunction(Element.UNDEF));
		}
		return functions;
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
	 */
	public Map<String,UniverseElement> getUniverses() {
		if (universes == null) {
			universes = new HashMap<String, UniverseElement>();
			
			universes.put(AbstractStorage.AGENTS_UNIVERSE_NAME, new UniverseElement());
		}
		return universes;
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
	 */
	public Map<String,BackgroundElement> getBackgrounds() {
		if (backgroundElements == null) {
			backgroundElements = new HashMap<String,BackgroundElement>();
		
			backgroundElements.put(
					BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME,
					new BooleanBackgroundElement());
			backgroundElements.put(
					FunctionBackgroundElement.FUNCTION_BACKGROUND_NAME,
					new FunctionBackgroundElement());
			backgroundElements.put(
					RuleBackgroundElement.RULE_BACKGROUND_NAME,
					new RuleBackgroundElement());
			backgroundElements.put(
					ElementBackgroundElement.ELEMENT_BACKGROUND_NAME,
					new ElementBackgroundElement());
					
		}
		
		return backgroundElements;
	}

	/**
	 * The kernel plug-in goes over all the rule declarations in the specification,
	 * creates a rule element (see {@link RuleElement}) for every one of those rules
	 * and provides the results to be added to the list of rules in the state.
	 * 
	 * @see VocabularyExtender#getRules()
	 */
	public Map<String, RuleElement> getRules() {
		if (ruleElements == null) {
			ruleElements = new HashMap<String, RuleElement>();
			
			// get root of tree
			ASTNode root = capi.getParser().getRootNode();
			
			List<ASTNode> ruleDeclarations = new ArrayList<ASTNode>();
			
			for (ASTNode child: root.getAbstractChildNodes())
				if (child.getGrammarRule().equals(Kernel.GR_RULEDECLARATION))
					ruleDeclarations.add(child);
			
			// while there is a rule declaration to process
			for (ASTNode currentRuleDeclaration: ruleDeclarations)
			{
				// get name (ID) node of rule
				final ASTNode idNode = currentRuleDeclaration.getFirst().getFirst();
				final String ruleName = idNode.getToken();
				
				if (ruleElements.get(ruleName) != null) 
					throw new CoreASMError(
							"Rule '" + ruleName + "' is defined more than once.", idNode);
				
				// create structure for all parameters
				ArrayList<String> params = new ArrayList<String>();
				ASTNode currentParams = idNode.getNext();
				// while there are parameters to add to the list
				while (currentParams != null)
				{
					// add parameters to the list
					params.add(currentParams.getToken());
					
					// get next parameter
					currentParams = currentParams.getNext();
				}
				
				// get root node of rule body
				ASTNode bodyNode = currentRuleDeclaration.getFirst().getNext();

				// create a copy of the body
				bodyNode = (ASTNode)capi.getInterpreter().copyTree(bodyNode);
				
				// create rule element
				ruleElements.put(ruleName, 
						new RuleElement(currentRuleDeclaration, idNode.getToken(), params,bodyNode));
			}
		}
		return ruleElements;
	}

	/**
	 * This method provides provides the equality operator, the 
	 * only operator provided in the kernel.
	 */
    public Collection<OperatorRule> getOperatorRules() {
        ArrayList<OperatorRule> opRules = new ArrayList<OperatorRule>();
        
        opRules.add(new OperatorRule(EQUALITY_OP,
                    OpType.INFIX_LEFT,
                    600,
                    getName()));

//        opRules.add(new OperatorRule("(", ")", OpType.INDEX, 900, getName()));
        
        return opRules;
    }

    /**
     * This method provides the interpretation of the equality operator.
     */
    public Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode) throws InterpreterException {
        Element result = null;
        String x = opNode.getToken();
        String gClass = opNode.getGrammarClass();
        
        // if class of operator is binary
        if (gClass.equals(ASTNode.BINARY_OPERATOR_CLASS)) {
            
            // get operand nodes
            ASTNode alpha = opNode.getFirst();
            ASTNode beta = alpha.getNext();
            
            // get operand values
            Element l = alpha.getValue();
            Element r = beta.getValue();
            
            if (x.equals(EQUALITY_OP)) {
                result = BooleanElement.valueOf(evaluateEquality(l, r));
            }
        } else
        	// simple application operator of the form: Term '(' Term ')'
        	if (gClass.equals(ASTNode.INDEX_OPERATOR_CLASS)) {
	        	ASTNode left = opNode.getFirst();
	        	ASTNode right = left.getNext();
	        	
	        	if (left.getValue() instanceof FunctionElement) {
	        		FunctionElement func = (FunctionElement)left.getValue();
	        		if (right == null)
	        			return func.getValue(ElementList.NO_ARGUMENT);
	        		else
	        			return func.getValue(ElementList.create(right.getValue()));
	        	} else
	        		return Element.UNDEF;
        	}
        
        return result;
    }

    /** 
     * Provides the semantics of the equality function to other plugins.
     * 
     * @param le element on the left
     * @param re element on the right
     */
    public final static boolean evaluateEquality(Element le, Element re) {
        return le.equals(re) || re.equals(le) ;
    }
    
	public Set<String> getBackgroundNames() {
		return backgroundNames;
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Set<String> getUniverseNames() {
		return universeNames;
	}

	public Set<String> getRuleNames() {
		return getRules().keySet();
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public String[] getOperators() {
		return operators;
	}
	
	public String[] getKeywords() {
		return keywords;
	}
	
	
	
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
}

//TODO remove the following lines!

//Parser<Binary<Node>> plusParser = null;
//Binary<Node> plusOpr = new Binary<Node>() {
//
//	public Node map(Node o1, Node o2) {
//		Node node = new ASTNode(
//				"", "", "+", "", o1.getScannerInfo());
//		node.addChild(o1);
//		node.addChild(new Node("", "+", o1.getScannerInfo()));
//		node.addChild(o2);
//		return node;
//	}
//	
//};
//plusParser = parserTools.getOprParser("+").seq(
//		Parsers.retn(plusOpr));
//
//Parser<Binary<Node>> multParser = null;
//Binary<Node> multOpr = new Binary<Node>() {
//
//	public Node map(Node o1, Node o2) {
//		Node node = new ASTNode(
//				"", "", "*", "", o1.getScannerInfo());
//		node.addChild(o1);
//		node.addChild(new Node("", "*", o1.getScannerInfo()));
//		node.addChild(o2);
//		return node;
//	}
//	
//};
//multParser = parserTools.getOprParser("*").seq(
//		Parsers.retn(multOpr));
//
//Parser<Unary<Node>> hashParser = parserTools.seq(
//		optionalDelimParser,
//		parserTools.getOprParser("#")
//		).map(new ParseMap<Object[], Unary<Node>>(PLUGIN_NAME) {
//
//	public Unary<Node> map(Object[] v) {
//		return new Unary<Node>() {
//
//			public Node map(Node o1) {
//				Node node = new ASTNode(
//						"", "", "#", "", o1.getScannerInfo());
//				node.addChild(o1);
//				node.addChild(new Node("", "#", o1.getScannerInfo()));
//				return node;
//			}
//    		
//    	};
//	}
//	
//});
//
//table = new OperatorTable<Node>().infixl(plusParser, 10).infixl(multParser, 20).postfix(hashParser, 30);
//exps.add(Expressions.buildExpressionParser(basicExprParser.followedBy(optionalDelimParser), table));
