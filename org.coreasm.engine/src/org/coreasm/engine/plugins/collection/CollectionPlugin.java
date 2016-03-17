/*
 * CollectionPlugin.java 		$Revision: 243 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.collection.CompilerCollectionPlugin;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base plugin of all the collection plugins
 *   
 * @author Roozbeh Farahbod
 * 
 */

public class CollectionPlugin extends Plugin 
	implements ParserPlugin, InterpreterPlugin, VocabularyExtender {

	protected static final Logger logger = LoggerFactory.getLogger(CollectionPlugin.class);

	/** plugin name */
	public static final String PLUGIN_NAME = CollectionPlugin.class.getSimpleName();
	
	/** version info */
	public static final VersionInfo version = new VersionInfo(0, 1, 1, "beta");
	
	private HashMap<String, GrammarRule> parsers = null;
	private Map<String, FunctionElement> functions = null;
	private Set<String> dependencyNames = null;
	
	private final String[] keywords = {"add", "to", "remove", "from"};
	private final String[] operators = {};
	
	private final CompilerPlugin compilerPlugin = new CompilerCollectionPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
	}

	@Override
	public Set<String> getDependencyNames() {
		if (dependencyNames == null) {
			dependencyNames = new HashSet<String>();
			dependencyNames.add("NumberPlugin");
		}
		return dependencyNames;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return version;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	
	/**
	 * @return <code>null</code>
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

	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> addtoRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("add", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("to", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Node node = new AddToRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("AddToCollectionRule",
					new GrammarRule("AddToCollectionRule", "'add' Term 'to' Term",
							addtoRuleParser, PLUGIN_NAME));
			
			//
			Parser<Node> removefromRuleParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser("remove", PLUGIN_NAME),
						termParser,
						pTools.getKeywParser("from", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						public Node map(Object[] vals) {
							Node node = new RemoveFromRuleNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("RemoveFromCollectionRule",
					new GrammarRule("RemoveFromCollectionRule", "'remove' Term 'from' Term",
							removefromRuleParser, PLUGIN_NAME));

			// Rule : AddToCollectionRule | RemoveFromCollectionRule
			parsers.put("Rule",
					new GrammarRule("SetRule", "AddToCollectionRule | RemoveFromCollectionRule",
							Parsers.or(addtoRuleParser, removefromRuleParser), PLUGIN_NAME));

		}
		
		return parsers;

	}

	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		ASTNode nextPos = pos;
		String gClass = pos.getGrammarClass();
        
		// if collection related rule
		if (gClass.equals(ASTNode.RULE_CLASS))
		{
			// add/to rule
			if (pos instanceof AddToRuleNode) {
				// add/to rule wrapper wrapper
				AddToRuleNode atNode = (AddToRuleNode)pos;
				ASTNode collectionNode = atNode.getToNode();
				
				nextPos = atNode.getUnevaluatedTerm();
				
				// no unevaluated terms
				if (nextPos == null)
				{
					// set next pos to current position
					nextPos = pos;
					
					if (atNode.getToLocation() != null) {
					
						if (collectionNode.getValue() instanceof ModifiableCollection) {

							try {
							// set vul for node
							pos.setNode(
									null, 
									((ModifiableCollection)collectionNode.getValue()).computeAddUpdate(
											atNode.getToLocation(),
											atNode.getAddElement(),
											interpreter.getSelf(),
											pos),
											
									null);
							} catch (InterpreterException e) {
								throw new CoreASMError(e.getMessage(), pos);
							}

						} else
							throw new CoreASMError("Incremental add update only applies to modifiable enumerables." + Tools.getEOL() 
									+ "Failed to add " + atNode.getAddElement() + " to " + atNode.getToLocation() + " because " + atNode.getToLocation() + " was " + collectionNode.getValue() + ".",
									atNode);
					} else
						throw new CoreASMError("Cannot perform incremental add update on a non-location!", atNode);
				}
				
			}
			// remove/from rule
			else if (pos instanceof RemoveFromRuleNode)
			{
				// remove/from rule wrapper wrapper
				RemoveFromRuleNode rfNode = (RemoveFromRuleNode)pos;
				ASTNode collectionNode = rfNode.getFromNode();
				
				nextPos = rfNode.getUnevaluatedTerm();
				
				// no unevaluated terms
				if (nextPos == null)
				{
					// set next pos to current position
					nextPos = pos;
				
					if (rfNode.getFromLocation() != null) {
						
						if (collectionNode.getValue() instanceof ModifiableCollection) {

							try{
								// set vul for node
								pos.setNode(
										null, 
										((ModifiableCollection)collectionNode.getValue()).computeRemoveUpdate(
												rfNode.getFromLocation(), 
												rfNode.getRemoveElement(), 
												interpreter.getSelf(),
												pos),
										null);
							} catch (InterpreterException e) {
								throw new CoreASMError(e.getMessage(), pos);
							}
							
						} else
							throw new CoreASMError("Incremental remove update only applies to modifiable enumerables." + Tools.getEOL() 
										+ "Failed to remove " + rfNode.getRemoveElement() + " from " + rfNode.getFromLocation() + " because " + rfNode.getFromLocation() + " was " + collectionNode.getValue() + ".",
										rfNode);
					} else
						throw new CoreASMError("Cannot perform incremental remove update on a non-location!", rfNode);
				}
			}
		}
            
        return nextPos;
	}

	public Set<String> getBackgroundNames() {
		return Collections.emptySet();
	}

	public Map<String, BackgroundElement> getBackgrounds() {
		return null;
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();
			
			// moved back to NumberPlugin
			//functions.put(SizeFunctionElement.NAME, new SizeFunctionElement());

			functions.put(MapFunctionElement.NAME, new MapFunctionElement(capi));
			functions.put(FilterFunctionElement.NAME, new FilterFunctionElement(capi));
			functions.put(FoldFunctionElement.FOLD_NAME, new FoldFunctionElement(capi, true));
			functions.put(FoldFunctionElement.FOLDR_NAME, new FoldFunctionElement(capi, true));
			functions.put(FoldFunctionElement.FOLDL_NAME, new FoldFunctionElement(capi, false));
		}
		return functions;
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return null;
	}

	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	public Map<String, UniverseElement> getUniverses() {
		return null;
	}

}
