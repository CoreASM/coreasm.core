/*	
 * ForallRulePlugin.java 	1.5 	$Revision: 243 $
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.forallrule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.UpdateMultiset;
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
import org.coreasm.util.Tools;

/** 
 *	Plugin for forall rule
 *   
 *  @author  George Ma, Roozbeh Farahbod
 *  
 */
public class ForallRulePlugin extends Plugin implements ParserPlugin,
        InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 9, 3, "");
	
	public static final String PLUGIN_NAME = ForallRulePlugin.class.getSimpleName();
	
	private final String[] keywords = {"forall", "in", "with", "do", "endforall"};
	private final String[] operators = {};
	
    private ThreadLocal<Map<Node,Stack<List<Element>>>> remains;
    private ThreadLocal<Map<Node,UpdateMultiset>> updates;
    
    private Map<String, GrammarRule> parsers;
    
    @Override
    public void initialize() {
        //considered = new HashMap<Node,ArrayList<Element>>();
        remains = new ThreadLocal<Map<Node, Stack<List<Element>>>>() {
			@Override
			protected Map<Node, Stack<List<Element>>> initialValue() {
				return new IdentityHashMap<Node, Stack<List<Element>>>();
			}
        };
        updates= new ThreadLocal<Map<Node,UpdateMultiset>>() {
			@Override
			protected Map<Node, UpdateMultiset> initialValue() {
				return new IdentityHashMap<Node, UpdateMultiset>(); 
			}
        };
    }
 
    private Map<Node, Stack<List<Element>>> getRemainsMap() {
    	return remains.get();
    }

    private Map<Node, UpdateMultiset> getUpdatesMap() {
    	return updates.get();
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
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();
			Parser<Node> guardParser = kernel.getGuardParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			
			Parser<Node> forallParser = Parsers.array( new Parser[] {
					pTools.getKeywParser("forall", PLUGIN_NAME),
					idParser,
					pTools.getKeywParser("in", PLUGIN_NAME),
					termParser,
					pTools.seq(
							pTools.getKeywParser("with", PLUGIN_NAME),
							guardParser).optional(),
					pTools.getKeywParser("do", PLUGIN_NAME),
					ruleParser,
					pTools.getKeywParser("endforall", PLUGIN_NAME).optional()
					}).map(
					new ForallParseMap());
			parsers.put("Rule", 
					new GrammarRule("ForallRule", 
							"'forall' ID 'in' Term ('with' Guard)? 'do' Rule ('endforall')?", forallParser, PLUGIN_NAME));
		}
		return parsers;
    }

    public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
        
        if (pos instanceof ForallRuleNode) {
            ForallRuleNode forallNode = (ForallRuleNode) pos;
            
            Map<Node, Stack<List<Element>>> remains = getRemainsMap();
            Map<Node, UpdateMultiset> updates = getUpdatesMap();
            
            if (!forallNode.getDomain().isEvaluated()) {
                
                // SPEC: considered := {beta}
                //considered.put(forallNode.getDomain(),new ArrayList<Element>());
            	Stack<List<Element>> stack = remains.get(forallNode.getDomain());
            	if (stack == null) {
            		stack = new Stack<List<Element>>();
            		remains.put(forallNode.getDomain(), stack);
            	}
            	stack.push(null);
                
                // SPEC: [pos] := {undef,{},undef}
                updates.put(pos,new UpdateMultiset());
                
                // SPEC: pos := beta
                return forallNode.getDomain();
            }
            else if (!forallNode.getDoRule().isEvaluated() && 
                    // depending on short circuit evaluation
                     ((forallNode.getCondition() == null) || !forallNode.getCondition().isEvaluated())) {
                if (forallNode.getDomain().getValue() instanceof Enumerable) {
                    
                    // SPEC: s := enumerate(v)/considered                    
                    // ArrayList<Element> s = new ArrayList<Element>(((Enumerable) forallNode.getDomain().getValue()).enumerate());
                    // s.removeAll(considered.get(forallNode.getDomain()));
                	// 
                	// changed to the following to improve performance:
        			List<Element> s = remains.get(forallNode.getDomain()).peek();
                	if (s == null) {
            			Enumerable domain = (Enumerable)forallNode.getDomain().getValue();
            			if (domain.supportsIndexedView())
            				s = new ArrayList<Element>(domain.getIndexedView());
            			else
            				s = new ArrayList<Element>(((Enumerable) forallNode.getDomain().getValue()).enumerate());
            			remains.get(forallNode.getDomain()).pop();
                		remains.get(forallNode.getDomain()).push(s);
                	}
                    
                    if (s.size() > 0) {
                        // choose t in s, for simplicty choose the first 
                        // since we have to go through all of them
                        Element chosen = s.get(0);
                        
                        // SPEC: AddEnv(x,t)
                        interpreter.addEnv(forallNode.getVariable().getToken(),chosen);
                        
                        // SPEC: considered := considered union {t}
                        //considered.get(forallNode.getDomain()).add(chosen);
                        s.remove(0);
                        
                        if (forallNode.getCondition() != null) {                            
                            // pos := gamma
                            return forallNode.getCondition();
                        }
                        else {
                            // pos := gamma
                            return forallNode.getDoRule();
                        }
                    }   
                    else {
                        //we're done
                        
                    	//considered.remove(forallNode.getDomain());
                        remains.remove(forallNode.getDomain());
                        
                        pos.setNode(null,updates.remove(pos),null);                        
                        return pos;
                    }
                }
                else {
                    capi.error("Cannot perform a 'forall' over " + Tools.sizeLimit(forallNode.getDomain().getValue().denotation())
                    		+ ". Forall domain must be an enumerable element.", forallNode.getDomain(), interpreter);
                }
            }
            else if (((forallNode.getCondition() != null) && forallNode.getCondition().isEvaluated()) &&
                     !forallNode.getDoRule().isEvaluated()) {
                
                boolean value = false;            
                if (forallNode.getCondition().getValue() instanceof BooleanElement) {
                    value = ((BooleanElement) forallNode.getCondition().getValue()).getValue();
                }
                else {
                    capi.error("Value of forall condition is not Boolean.", forallNode.getCondition(), interpreter);
                }
                
                if (value) {
                    // pos := delta
                    return forallNode.getDoRule();
                }
                else {
                    // ClearTree(gamma)
                    interpreter.clearTree(forallNode.getCondition());
                    
                    // RemoveEnv(x)
                    interpreter.removeEnv(forallNode.getVariable().getToken());
                    
                    // pos := beta
                    return forallNode.getDomain();
                }
                
            }
            else if (((forallNode.getCondition() == null) || forallNode.getCondition().isEvaluated()) && 
                    (forallNode.getDoRule().isEvaluated())) {    
                
                // [pos] := (undef,updates(pos) union u,undef)                
                if (forallNode.getDoRule().getUpdates() != null) {
                    updates.get(pos).addAll(forallNode.getDoRule().getUpdates());
                }
                
                // RemoveEnv(x)
                interpreter.removeEnv(forallNode.getVariable().getToken());
                
                // ClearTree(gamma/delta)
                interpreter.clearTree(forallNode.getDoRule());
                
                if (forallNode.getCondition() != null) {
                    // ClearTree(gamma)
                    interpreter.clearTree(forallNode.getCondition());
                }
                
                // pos := beta;
                return forallNode.getDomain();
            }
        }
        
        return pos;
    }

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public static class ForallParseMap //extends ParseMapN<Node> {
	extends ParserTools.ArrayParseMap {

		String nextChildName = "alpha";
		
		public ForallParseMap() {
			super(PLUGIN_NAME);
		}

		public Node map(Object... vals) {
			nextChildName = "alpha";
            Node node = new ForallRuleNode(((Node)vals[0]).getScannerInfo());
            addChildren(node, vals);
			return node;
		}

		@Override
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode)
				parent.addChild(nextChildName, child);
			else {
				String token = child.getToken();
		        if (token.equals("with"))
		        	nextChildName = "guard";
		        else 
		        	if (token.equals("do"))
		        		nextChildName = "rule";
				super.addChild(parent, child);
			}
		}
		
	}

	/**
	 * @return <code>null</code>
	 */
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}
	
}
