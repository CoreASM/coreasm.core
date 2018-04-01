/*  
 * BlockRulePlugin.java    $Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Copyright (C) 2006 George Ma
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.blockrule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.blockrule.CompilerBlockRulePlugin;
import org.coreasm.engine.EngineTools;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Plugin for BlockRule construct:
 *    par
 *       rule_1
 *       rule_2
 *       ...
 *       rule_n
 *    endpar
 *   
 *  @author  George Ma, Roozbeh Farahbod
 *  
 */

public class BlockRulePlugin extends Plugin 
		implements InterpreterPlugin, ParserPlugin {
 
	private static final Logger logger = LoggerFactory.getLogger(BlockRulePlugin.class);
	
	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 1, "");
	
	public static final String PLUGIN_NAME = BlockRulePlugin.class.getSimpleName();

	private Map<String, GrammarRule> parsers = null;
	
	private final String[] keywords = {"par", "endpar"};
	private final String[] operators = {"{", "}"};
	
	private final CompilerPlugin compilerPlugin = new CompilerBlockRulePlugin(this);
	
    public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
        String gRule = pos.getGrammarRule();
        
        if ((gRule != null) && (gRule.equals("BlockRule"))) {
            ASTNode currentRule = pos.getFirst();
   
            // check if all rules in the block have been
            // interpreted.  if not, interpret them by
            // giving the uninterpreted rule node back to the
            // interpreter
            while (currentRule != null) {
                if (!currentRule.isEvaluated()) {
                    return currentRule;
                }
                currentRule = currentRule.getNext();
            }

            // all rules have been evaluated.
            // accumulate all the updates for this block
            currentRule = pos.getFirst();
            UpdateMultiset updates = new UpdateMultiset();
            
            while (currentRule != null) {
            	// TODO A decision needs to be made on the following pattern
            	//      Do we want to have this pattern in other plugins as well?
            	if (!EngineTools.hasUpdates(interpreter, currentRule, capi, logger)) {
        			return pos;
            	} else {
            		updates.addAll(currentRule.getUpdates());
            		currentRule = currentRule.getNext();
            	}
            }
            
            // set the UpdateMultiset for this node
            pos.setNode(null,updates,null);
            return pos;
        }
        else {
            return null;
        }
    }

    @Override
    public void initialize() {
        
    }

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
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

	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			
			Parser<Node> ruleParser = 
				((KernelServices)capi.getPlugin("Kernel").getPluginInterface()).getRuleParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			
			Parser<Node> blockRuleParser = Parsers.array(  
						Parsers.or(
								pTools.getKeywParser("par", this.getName()),
								pTools.getOprParser("{")),
						pTools.plus(ruleParser),
						Parsers.or(
								pTools.getKeywParser("endpar", getName()),
								pTools.getOprParser("}"))
					).map(new ParserTools.ArrayParseMap(PLUGIN_NAME) {

						@Override
						public Node map(Object[] from) {
							ASTNode node = new ASTNode(
									BlockRulePlugin.PLUGIN_NAME,
									ASTNode.RULE_CLASS,
									"BlockRule",
									null,
									((Node)from[0]).getScannerInfo());
							addChildren(node, from);
							return node;
						}
						
						public void addChild(Node parent, Node child) {
							if (child instanceof ASTNode)
								parent.addChild("lambda", child);
							else
								parent.addChild(child); //super.addChild(parent, child);
						}
					});
			parsers.put("Rule", 
					new GrammarRule("Rule",
							"'par' Rule+ 'endpar'", blockRuleParser, this.getName()));
			
		}
		return parsers;
	}


	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
}
