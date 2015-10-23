package org.coreasm.engine.plugins.foreachrule;

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
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.Update;
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
 *	Plugin for foreach rule
 *   
 *  @author  Michael Stegmaier
 *  
 */
public class ForeachRulePlugin extends Plugin implements ParserPlugin,
        InterpreterPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 9, 3, "");
	
	public static final String PLUGIN_NAME = ForeachRulePlugin.class.getSimpleName();
	
	private final String[] keywords = {"foreach", "in", "with", "do", "ifnone", "endforeach"};
	private final String[] operators = {};
	
    private ThreadLocal<Map<Node,List<Element>>> remained;
    private ThreadLocal<Map<Node,UpdateMultiset>> updates;
    
    private Map<String, GrammarRule> parsers;
    
    @Override
    public CompilerPlugin getCompilerPlugin(){
    	return null;
    }
    
    @Override
    public void initialize() {
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
			
			Parser<Node> foreachParser = Parsers.array( new Parser[] {
					pTools.getKeywParser("foreach", PLUGIN_NAME),
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
					pTools.getKeywParser("endforeach", PLUGIN_NAME).optional()
					}).map(
					new ForeachParseMap());
			parsers.put("Rule", 
					new GrammarRule("ForeachRule", 
							"'foreach' ID 'in' Term (',' ID 'in' Term) ('with' Guard)? 'do' Rule ('ifnone' Rule)? ('endforeach')?", foreachParser, PLUGIN_NAME));
		}
		return parsers;
    }

    public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
        
        if (pos instanceof ForeachRuleNode) {
            ForeachRuleNode foreachNode = (ForeachRuleNode) pos;
            Map<Node, List<Element>> remained = getRemainedMap();
            Map<Node, UpdateMultiset> updates = getUpdatesMap();
            Map<String, ASTNode> variableMap = null;
            AbstractStorage storage = capi.getStorage();
            
            try {
            	variableMap = foreachNode.getVariableMap();
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
            
            if (!foreachNode.getDoRule().isEvaluated() &&
            		(foreachNode.getIfnoneRule() == null || !foreachNode.getIfnoneRule().isEvaluated()) &&
                    // depending on short circuit evaluation
                     ((foreachNode.getCondition() == null) || !foreachNode.getCondition().isEvaluated())) {
            	// pos := gamma
            	if (foreachNode.getCondition() != null)
            		pos = foreachNode.getCondition();
            	else
            		pos = foreachNode.getDoRule();
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
	            				if (foreachNode.getIfnoneRule() == null) {
	                    			for (Entry<String, ASTNode> var : variableMap.entrySet()) {
	                	    			if (remained.remove(var.getValue()) != null)
	                	    				interpreter.removeEnv(var.getKey());
	                	    		}
	                				// [pos] := (undef,{},undef)
	                    			foreachNode.setNode(null, new UpdateMultiset(), null);
	                	            return foreachNode;
	            				}
                	         	// pos := delta
	                           	pos = foreachNode.getIfnoneRule();
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
		                        shouldChoose = false;
		                        
		                        // SPEC: AddEnv(x,t)
		                        interpreter.addEnv(variable.getKey(),chosen);
		                    }   
		                    else {
		                        remained.remove(variable.getValue());
		                        if (pos != foreachNode.getIfnoneRule())
			            			pos = foreachNode;
		                    }
	                	}
	                }
	                else {
	                    capi.error("Cannot perform a 'foreach' over " + Tools.sizeLimit(variable.getValue().getValue().denotation())
	                    		+ ". Foreach domain must be an enumerable element.", variable.getValue(), interpreter);
	                    return pos;
	                }
            	}
            	if (shouldChoose) {
        			if (foreachNode.getIfnoneRule() == null || updates.containsKey(foreachNode)) {
            			// we're done
        				UpdateMultiset updateSet = updates.remove(pos);
        				if (updateSet == null)
        					updateSet = new UpdateMultiset();
        				storage.popState();
        				foreachNode.setNode(null, updateSet, null);
        	            return foreachNode;
        			}
        			// pos := delta
        			pos = foreachNode.getIfnoneRule();
        		}
            }
            else if (((foreachNode.getCondition() != null) && foreachNode.getCondition().isEvaluated()) &&
                     !foreachNode.getDoRule().isEvaluated() &&
                     (foreachNode.getIfnoneRule() == null || !foreachNode.getIfnoneRule().isEvaluated())) {
                
                boolean value = false;            
                if (foreachNode.getCondition().getValue() instanceof BooleanElement) {
                    value = ((BooleanElement) foreachNode.getCondition().getValue()).getValue();
                }
                else {
                    capi.error("Value of foreach condition is not Boolean.", foreachNode.getCondition(), interpreter);
                    return pos;
                }
                
                if (value) {
                    // pos := delta
                    return foreachNode.getDoRule();
                }
                else {
                    // ClearTree(gamma)
                    interpreter.clearTree(foreachNode.getCondition());
                    
                    // pos := beta
                    return foreachNode;
                }
                
            }
            else if (((foreachNode.getCondition() == null) || foreachNode.getCondition().isEvaluated()) && 
                    (foreachNode.getDoRule().isEvaluated())) {
            	if (!updates.containsKey(pos))
            		storage.pushState();
                
            	if (foreachNode.getDoRule().getUpdates() != null) {
	            	UpdateMultiset composedUpdates = updates.get(pos);
					if (composedUpdates == null)
						composedUpdates = new UpdateMultiset();
					Set<Update> aggregatedUpdates = storage.performAggregation(foreachNode.getDoRule().getUpdates());
					if (!storage.isConsistent(aggregatedUpdates))
						throw new CoreASMError("Inconsistent updates computed in loop.", pos);
					storage.apply(aggregatedUpdates);
					updates.put(pos, storage.compose(composedUpdates, foreachNode.getDoRule().getUpdates()));
            	}
                
                // ClearTree(gamma/delta)
                interpreter.clearTree(foreachNode.getDoRule());
                
                if (foreachNode.getCondition() != null) {
                    // ClearTree(gamma)
                    interpreter.clearTree(foreachNode.getCondition());
                }
                
                return pos;
            }
            else if (foreachNode.getIfnoneRule() != null && foreachNode.getIfnoneRule().isEvaluated()) {
                // [pos] := (undef,u,undef)
                pos.setNode(null,foreachNode.getIfnoneRule().getUpdates(),null);
                return pos;
            }
            if (pos == foreachNode.getIfnoneRule()) {
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

	public static class ForeachParseMap //extends ParseMapN<Node> {
	extends ParserTools.ArrayParseMap {

		String nextChildName = "alpha";
		
		public ForeachParseMap() {
			super(PLUGIN_NAME);
		}

		public Node map(Object[] vals) {
			nextChildName = "alpha";
            Node node = new ForeachRuleNode(((Node)vals[0]).getScannerInfo());
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
