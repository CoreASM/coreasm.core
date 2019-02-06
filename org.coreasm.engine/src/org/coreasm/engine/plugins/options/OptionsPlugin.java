/*	
 * OptionsPlugin.java  	$Revision: 243 $
 * 
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.options;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.options.CompilerOptionsPlugin;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.CoreASMIssue;
import org.coreasm.engine.CoreASMWarning;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This plug-in provides the means to set CoreASM engine properties. These
 * properties can later be used by various plugins to provide customized 
 * services.
 * <p>
 * The plugin provides the following syntax to the Header section of 
 * CoreASM specifications:
 * <p>
 * <code><b>option</b></code> property </code> <code> value </code>
 * <p>
 * value of the property ends by the end of the line
 *   
 * @author  Roozbeh Farahbod
 * 
 */
public class OptionsPlugin extends Plugin implements ParserPlugin,
		ExtensionPointPlugin {

	protected static final Logger logger = LoggerFactory.getLogger(OptionsPlugin.class);

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 1, "");
	
	public static final String PLUGIN_NAME = OptionsPlugin.class.getSimpleName();
	
    private Map<String, GrammarRule> parsers = null;

	private final String[] keywords = {"option"};
	private final String[] operators = {"."};
	
	private final Map<EngineMode, Integer> sourceModes;
	private final Map<EngineMode, Integer> targetModes;

	private final CompilerPlugin compilerPlugin = new CompilerOptionsPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	public OptionsPlugin() {
		sourceModes = new HashMap<EngineMode, Integer>();
		sourceModes.put(EngineMode.emParsingSpec, ExtensionPointPlugin.DEFAULT_PRIORITY);
		targetModes = new HashMap<EngineMode, Integer>();
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {

	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getKeywords()
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getOperators()
	 */
	public String[] getOperators() {
		return operators;
	}

	
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	/**
	 * Always returns <code>null</code>.
	 * 
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		if (parsers != null)
			return parsers.get(nonterminal).parser;
		else 
			return null;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParsers()
	 */
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			Parser<Node> termParser = kernel.getTermParser();
		
			// ID('.'ID)*
			Parser<Node> optionNameParser = Parsers.array(
					new Parser[] {
						idParser,
						pTools.many(pTools.seq(pTools.getOprParser("."), idParser))
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node apply(Object[] vals) {
							String str = objectToString(vals);
							return new ASTNode(
									PLUGIN_NAME,
									ASTNode.ID_CLASS,
									"PropertyName",
									str,
									((Node)vals[0]).getScannerInfo());
						}
						
						private String objectToString(Object obj) {
							String result = "";
							if (obj instanceof Object[])
								for (Object child: (Object[])obj)
									result = result + objectToString(child);
							else
								if (obj instanceof Node)
									result = ((Node)obj).getToken();
							return result;
						}
					});
			parsers.put("OptionName",
					new GrammarRule("OptionName",
							"ID('.'ID)*", optionNameParser, PLUGIN_NAME));

			// One Toekn (whatever)
			// FIXME do we need to change it?
			/*Parser<Node> optionValueParser = Parsers.token("OptionValue", new FromToken<Node>() {

				public Node fromToken(Tok tok) {
					return new ASTNode(
							PLUGIN_NAME, 
							ASTNode.DECLARATION_CLASS, 
							"OptionValue", 
							tok.toString().trim(),
							new ScannerInfo(tok)
							);
				}});
			
			parsers.put(optionValueParser.toString(),
					new GrammarRule(optionValueParser.toString(),
							"AnyToken", optionValueParser, PLUGIN_NAME));*/

			Parser<Node> optionParser = Parsers.array(
				new Parser[] {
					pTools.getKeywParser("option", PLUGIN_NAME),
					optionNameParser,
					pTools.seq(termParser.optional(null)).optional(null),
				}).map(
				new ParserTools.ArrayParseMap(PLUGIN_NAME) {

					@Override
					public Node apply(Object[] vals) {
						Node node = new OptionNode(((Node)vals[0]).getScannerInfo());
						addChildren(node, vals);
						return node;
					}
			});
			parsers.put("Option",
				new GrammarRule("Option",
						"'option' OptionName OptionValue", optionParser, PLUGIN_NAME));

			/*
			Parser<Node> optionsParser = Parsers.mapn("Options", 
					new Parser[] {
						pTools.star(optionParser)
					},
					new ParseMapN<Node>(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Object[] nodes = (Object[])vals[0];
							if (nodes.length == 0)
								return null;
							Node node = new ASTNode(
				        			PLUGIN_NAME,
				        			ASTNode.DECLARATION_CLASS,
				        			"Options",
				        			null,
				        			((Node)nodes[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}} 
			);
			parsers.put(optionsParser.toString(),
					new GrammarRule(optionsParser.toString(),
							"Option*", optionsParser, PLUGIN_NAME));
			*/
			
			parsers.put("Header", 
					new GrammarRule("Header", "Option", optionParser, PLUGIN_NAME));
		}
		
		return parsers;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ExtensionPointPlugin#fireOnModeTransition(org.coreasm.engine.CoreASMEngine.EngineMode, org.coreasm.engine.CoreASMEngine.EngineMode)
	 */
	public void fireOnModeTransition(EngineMode source, EngineMode target) {
		if (source == EngineMode.emParsingSpec)
			loadProperties();
	}

	/*
	 * Looks into the abstract syntax tree and loads the properties
	 * specified in the specification into the engine.
	 */
	private void loadProperties() {
		Set<String> definedOptions = capi.getSpec().getOptions();
        ASTNode currentNode = capi.getParser().getRootNode().getFirst();
        OptionNode optionNode = null;
        
        while (currentNode != null) {
            if (currentNode instanceof OptionNode) {
            	optionNode = (OptionNode)currentNode;
            	if (definedOptions.contains(optionNode.getOptionName())) {
            		try {
            			String pluginName = optionNode.getOptionName().substring(0, optionNode.getOptionName().indexOf('.'));
            			String optionName = optionNode.getOptionName().substring(pluginName.length() + 1);
            			capi.getPlugin(pluginName).checkOptionValue(optionName, optionNode.getOptionValue());
            			capi.setProperty(optionNode.getOptionName(), optionNode.getOptionValue());
                        logger.debug("Option '{}' is set to '{}'.", optionNode.getOptionName(), optionNode.getOptionValue());
            		} catch (CoreASMIssue e) {
            			if (e instanceof CoreASMWarning)
            				capi.warning(new CoreASMWarning(((CoreASMWarning)e).src, e.getMessage(), optionNode.getFirst().getNext()));
            			else if (e instanceof CoreASMError)
            				capi.error(new CoreASMError(e.getMessage(), optionNode.getFirst().getNext()));
            			else
            				throw e;
            		}
            	}
            	else
            		capi.warning(getName(), "The option '" + optionNode.getOptionName() + "' is undefined and will be ignored.", optionNode.getFirst(), capi.getInterpreter());
            }
            currentNode = currentNode.getNext();
        }
	}

	/*
	 * Looks into the abstract syntax tree and loads the properties
	 * specified in the specification into the engine.
	 */
	/*private void OLD_loadProperties() {
        ASTNode node = capi.getParser().getRootNode().getFirst();
        //Properties specProperties = new Properties();

        while (node != null) {
            while (node != null) {
                if ((node.getGrammarRule() != null) && node.getGrammarRule().equals("Options"))
                    break;
                
                node = node.getNext();            
                if (node == null) {
                    logger.debug("No options are specified.");
                    return;
                }
            }        
        }
        
        ASTNode currentNode = node.getFirst();
        OptionNode optionNode = null;
        
        while (currentNode != null) {
            if (currentNode instanceof OptionNode) {
            	optionNode = (OptionNode)currentNode; 
            	capi.setProperty(optionNode.getOptionName(), optionNode.getOptionValue());
            }
            currentNode = currentNode.getNext();
        }
        
	}*/

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ExtensionPointPlugin#getSourceModes()
	 */
	public Map<EngineMode, Integer> getSourceModes() {
		return sourceModes;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.ExtensionPointPlugin#getTargetModes()
	 */
	public Map<EngineMode, Integer> getTargetModes() {
		return targetModes;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

}

		
