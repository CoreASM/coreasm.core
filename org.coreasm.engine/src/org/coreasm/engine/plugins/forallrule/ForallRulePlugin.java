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
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.CoreASMError;
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
	
	private final String[] keywords = {"forall", "in", "with", "do", "ifnone", "endforall"};
	private final String[] operators = {};
	
    private ThreadLocal<Map<Node,List<Element>>> remained;
    private ThreadLocal<Map<Node,UpdateMultiset>> updates;
    
    private Map<String, GrammarRule> parsers;
    
    @Override
    public void initialize() {
        //considered = new IdentityHashMap<Node,ArrayList<Element>>();
        remained = new ThreadLocal<Map<Node, List<Element>>>() {
			@Override
			protected Map<Node, List<Element>> initialValue() {
				return new IdentityHashMap<Node, List<Element>>();
			}
        };
        updates= new ThreadLocal<Map<Node,UpdateMultiset>>() {
			@Override
			protected Map<Node, UpdateMultiset> initialValue() {
				return new IdentityHashMap<Node, UpdateMultiset>(); 
			}
        };
    }
 
    private Map<Node, List<Element>> getRemainedMap() {
    	return remained.get();
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
					pTools.csplus(Parsers.array(idParser,
						pTools.getKeywParser("in", PLUGIN_NAME),
						termParser)),
					pTools.seq(
						pTools.getKeywParser("with", PLUGIN_NAME),
						guardParser).optional(),
					pTools.getKeywParser("do", PLUGIN_NAME),
					ruleParser,
					pTools.seq(
						pTools.getKeywParser("ifnone", PLUGIN_NAME),
						ruleParser).optional(),
					pTools.getKeywParser("endforall", PLUGIN_NAME).optional()
					}).map(
					new ForallParseMap());
			parsers.put("Rule", 
					new GrammarRule("ForallRule", 
							"'forall' ID 'in' Term (',' ID 'in' Term) ('with' Guard)? 'do' Rule ('ifnone' Rule)? ('endforall')?", forallParser, PLUGIN_NAME));
		}
		return parsers;
    }

    public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
        
        if (pos instanceof ForallRuleNode) {
            ForallRuleNode forallNode = (ForallRuleNode) pos;
            Map<Node, List<Element>> remained = getRemainedMap();
            Map<Node, UpdateMultiset> updates = getUpdatesMap();
            Map<String, ASTNode> variableMap = null;
            
            try {
            	variableMap = forallNode.getVariableMap();
            }
            catch (CoreASMError e) {
            	capi.error(e);
            	return pos;
            }
            
            // evaluate all domains
            for (ASTNode domain : variableMap.values()) {
            	if (!domain.isEvaluated()) {
            		// SPEC: considered := {}
                	remained.remove(domain);
                    
                    // SPEC: pos := beta
            		return domain;
            	}
            }
            
            if (!forallNode.getDoRule().isEvaluated() &&
            		(forallNode.getIfnoneRule() == null || !forallNode.getIfnoneRule().isEvaluated()) &&
                    // depending on short circuit evaluation
                     ((forallNode.getCondition() == null) || !forallNode.getCondition().isEvaluated())) {
            	// pos := gamma
            	if (forallNode.getCondition() != null)
            		pos = forallNode.getCondition();
            	else
            		pos = forallNode.getDoRule();
            	boolean shouldChoose = true;
            	for (Entry<String, ASTNode> variable : variableMap.entrySet()) {
	                if (variable.getValue().getValue() instanceof Enumerable) {
	                    
	                    // SPEC: s := enumerate(v)/considered                    
	                    // ArrayList<Element> s = new ArrayList<Element>(((Enumerable) variable.getValue().getValue()).enumerate());
	                    // s.removeAll(considered.get(variable.getValue()));
	                	// 
	                	// changed to the following to improve performance:
	        			List<Element> s = remained.get(variable.getValue());
	                	if (s == null) {
	            			Enumerable domain = (Enumerable)variable.getValue().getValue();
	            			if (domain.supportsIndexedView())
	            				s = new ArrayList<Element>(domain.getIndexedView());
	            			else
	            				s = new ArrayList<Element>(((Enumerable) variable.getValue().getValue()).enumerate());
	            			if (s.isEmpty()) {
	            				if (forallNode.getIfnoneRule() == null) {
	                    			for (Entry<String, ASTNode> var : variableMap.entrySet()) {
	                	    			if (remained.remove(var.getValue()) != null)
	                	    				interpreter.removeEnv(var.getKey());
	                	    		}
	                				// [pos] := (undef,{},undef)
	                    			forallNode.setNode(null, new UpdateMultiset(), null);
	                	            return forallNode;
	            				}
                	         	// pos := delta
	                           	pos = forallNode.getIfnoneRule();
	                           	interpreter.addEnv(variable.getKey(), Element.UNDEF);
	                    	}
	            			remained.put(variable.getValue(), s);
	                		shouldChoose = true;
	                	}
	                	else if (shouldChoose)
	                		interpreter.removeEnv(variable.getKey());
	                	
	                	if (shouldChoose) {
		                    if (!s.isEmpty()) {
		                    	// SPEC: considered := considered union {t}
		                        // choose t in s, for simplicty choose the first 
		                        // since we have to go through all of them
		                        Element chosen = s.remove(0);
		                        
		                        // SPEC: AddEnv(x,t)
		                        interpreter.addEnv(variable.getKey(),chosen);
		                    }   
		                    else {
		                        remained.remove(variable.getValue());
		                        if (pos != forallNode.getIfnoneRule())
			            			pos = forallNode;
			            		shouldChoose = true;
			            		continue;
		                    }
	                	}
	                }
	                else {
	                    capi.error("Cannot perform a 'forall' over " + Tools.sizeLimit(variable.getValue().getValue().denotation())
	                    		+ ". Forall domain must be an enumerable element.", variable.getValue(), interpreter);
	                    return pos;
	                }
	                shouldChoose = false;
            	}
            	if (shouldChoose) {
        			if (forallNode.getIfnoneRule() == null || updates.containsKey(forallNode)) {
            			// we're done
        				UpdateMultiset updateSet = updates.remove(pos);
        				if (updateSet == null)
        					updateSet = new UpdateMultiset();
        				forallNode.setNode(null, updateSet, null);
        	            return forallNode;
        			}
        			// pos := delta
        			pos = forallNode.getIfnoneRule();
        		}
            }
            else if (((forallNode.getCondition() != null) && forallNode.getCondition().isEvaluated()) &&
                     !forallNode.getDoRule().isEvaluated() &&
                     (forallNode.getIfnoneRule() == null || !forallNode.getIfnoneRule().isEvaluated())) {
                
                boolean value = false;            
                if (forallNode.getCondition().getValue() instanceof BooleanElement) {
                    value = ((BooleanElement) forallNode.getCondition().getValue()).getValue();
                }
                else {
                    capi.error("Value of forall condition is not Boolean.", forallNode.getCondition(), interpreter);
                    return pos;
                }
                
                if (value) {
                    // pos := delta
                    return forallNode.getDoRule();
                }
                else {
                    // ClearTree(gamma)
                    interpreter.clearTree(forallNode.getCondition());
                    
                    // pos := beta
                    return forallNode;
                }
                
            }
            else if (((forallNode.getCondition() == null) || forallNode.getCondition().isEvaluated()) && 
                    (forallNode.getDoRule().isEvaluated())) {    
                
            	UpdateMultiset updateSet = updates.get(pos);
            	if (updateSet == null) {
	            	// SPEC: [pos] := {undef,{},undef}
            		updateSet = new UpdateMultiset();
	                updates.put(pos,updateSet);
            	}
                // [pos] := (undef,updates(pos) union u,undef)                
                if (forallNode.getDoRule().getUpdates() != null)
                	updateSet.addAll(forallNode.getDoRule().getUpdates());
                
                // ClearTree(gamma/delta)
                interpreter.clearTree(forallNode.getDoRule());
                
                if (forallNode.getCondition() != null) {
                    // ClearTree(gamma)
                    interpreter.clearTree(forallNode.getCondition());
                }
                
                return pos;
            }
            else if (forallNode.getIfnoneRule() != null && forallNode.getIfnoneRule().isEvaluated()) {
                // [pos] := (undef,u,undef)
                pos.setNode(null,forallNode.getIfnoneRule().getUpdates(),null);
                return pos;
            }
            if (pos == forallNode.getIfnoneRule()) {
            	// RemoveEnv(x)
        		for (Entry<String, ASTNode> variable : variableMap.entrySet()) {
        			if (remained.remove(variable.getValue()) != null)
        				interpreter.removeEnv(variable.getKey());
        		}
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

		public Node map(Object[] vals) {
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
		        else if (token.equals("do"))
	        		nextChildName = "rule";
		        else if (token.equals("ifnone"))
		        	nextChildName = "ifnone";
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
