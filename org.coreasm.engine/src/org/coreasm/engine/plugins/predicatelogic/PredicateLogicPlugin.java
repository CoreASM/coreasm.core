/*	
 * PredicateLogicPlugin.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2006 George Ma
 * Copyright (c) 2007 Roozbeh Farahbod
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.plugins.predicatelogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.predicatelogic.CompilerPredicateLogicPlugin;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Tools;

/** 
 * Plugin for predicate logic
 *   
 *  @author  George Ma, Roozbeh Farahbod
 *  
 */
public class PredicateLogicPlugin extends Plugin implements OperatorProvider, ParserPlugin, InterpreterPlugin {
    
	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 4, 9, "");
	
	public static final String PLUGIN_NAME = PredicateLogicPlugin.class.getSimpleName();

	public static final String IMPLY_OP = "implies";
    public static final String OR_OP = "or";
    public static final String XOR_OP = "xor";
    public static final String AND_OP = "and";
    public static final String NOT_OP = "not";
    public static final String FORALL_EXP_TOKEN = "forall";
    public static final String EXISTS_EXP_TOKEN = "exists";
    public static final String NOT_EQ_OP = "!=";
    public static final String IN_OP = "memberof";
    public static final String NOTIN_OP = "notmemberof";
    
    // for keeping track of considered elements in Exists and Forall expressions
    private ThreadLocal<Map<ASTNode, Iterator<? extends Element>>> iterators;
 
    private ArrayList<OperatorRule> opRules = null;
    private Map<String, GrammarRule> parsers = null; 
    
	private final String[] keywords = {IMPLY_OP, OR_OP, XOR_OP, AND_OP, NOT_OP, NOTIN_OP,
			"forall", "holds", "exists", "with", IN_OP, "in"};
	private final String[] operators = {"!="};
	
	private final CompilerPlugin compilerPlugin = new CompilerPredicateLogicPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	/**
	 * Create a new instance of PredicateLogicPlugin
	 */
	public PredicateLogicPlugin() {
		super();
        iterators = new ThreadLocal<Map<ASTNode, Iterator<? extends Element>>>() {
			@Override
			protected Map<ASTNode, Iterator<? extends Element>> initialValue() {
				return new IdentityHashMap<ASTNode, Iterator<? extends Element>>();
			}
        };
    }

	private Map<ASTNode, Iterator<? extends Element>> getIteratorMap() {
		return iterators.get();
	}

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.Plugin#initialize()
	 */
	@Override
	public void initialize() {
	            
	}

	//--------------------------------
	// Operator Implementor Interface
	//--------------------------------

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.OperatorProvider#getOperatorRules()
	 */
	public Collection<OperatorRule> getOperatorRules() {
	
		if (opRules == null) {
			opRules = new ArrayList<OperatorRule>();
			
			opRules.add(new OperatorRule(IMPLY_OP,
					    OpType.INFIX_LEFT,
					    375,
					    getName()));
	        
	        opRules.add(new OperatorRule(OR_OP,
	                    OpType.INFIX_LEFT,
	                    350,
	                    getName()));
	        
	        opRules.add(new OperatorRule(XOR_OP,
	                    OpType.INFIX_LEFT,
	                    350,
	                    getName()));
	        
	        opRules.add(new OperatorRule(AND_OP,
	                    OpType.INFIX_LEFT,
	                    400,
	                    getName()));
	        
	        opRules.add(new OperatorRule(NOT_OP,
	                    OpType.PREFIX,
	                    850,
	                    getName()));
	        
	        opRules.add(new OperatorRule(IN_OP,
	                    OpType.INFIX_LEFT,
	                    550,
	                    getName()));
	        
	        opRules.add(new OperatorRule(NOTIN_OP,
	                    OpType.INFIX_LEFT,
	                    550,
	                    getName()));
	        		
	        opRules.add(new OperatorRule(NOT_EQ_OP,
		                OpType.INFIX_LEFT,
		                600,
		                getName()));
		}
    		
		return opRules;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.plugin.OperatorProvider#interpretOperatorNode(org.coreasm.engine.interpreter.Node)
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
            
            if (x.equals(NOT_EQ_OP)) {
            	result = BooleanElement.valueOf(!Kernel.evaluateEquality(l, r));
            }
            else if (x.equals(IN_OP) || x.equals(NOTIN_OP)) {
            	if (r.equals(Element.UNDEF)) {
            		result = Element.UNDEF;
					capi.warning(PLUGIN_NAME, "The operand of the unary operator '" + x + "' was undef.", opNode, interpreter);
            	} else {
	                if (r instanceof Enumerable) {
		                Enumerable enumerableElement = (Enumerable) r;
		                
		                if (x.equals(IN_OP)) {
		                    result = BooleanElement.valueOf(enumerableElement.contains(l));
		                }
		                else if (x.equals(NOTIN_OP)) {
		                    result = BooleanElement.valueOf(!enumerableElement.contains(l));
		                }
	                }
	                else if (r instanceof AbstractUniverse) {
	                	AbstractUniverse universe = (AbstractUniverse)r;
						if (IN_OP.equals(x))
							result = BooleanElement.valueOf(universe.member(l));
						else if (NOTIN_OP.equals(x))
							result = BooleanElement.valueOf(!universe.member(l));
	                }
            	}
            }
            else {
                // confirm that operands are boolean elements, otherwise throw an error
            	if ((l instanceof BooleanElement || l.equals(Element.UNDEF)) 
            			&& (r instanceof BooleanElement || r.equals(Element.UNDEF))) {
            		if (r instanceof BooleanElement && l instanceof BooleanElement) {
                        // convert operands to boolean elements
                        BooleanElement eL = (BooleanElement)l;
                        BooleanElement eR = (BooleanElement)r;
                        
                        if (x.equals(IMPLY_OP))
                            result = BooleanElement.valueOf((!eL.getValue()) | eR.getValue());
                        else if (x.equals(OR_OP))
                            result = BooleanElement.valueOf(eL.getValue() | eR.getValue());
                        else if (x.equals(XOR_OP))
                            result = BooleanElement.valueOf(eL.getValue() ^ eR.getValue());
                        else if (x.equals(AND_OP))
                            result = BooleanElement.valueOf(eL.getValue() & eR.getValue());
            		} else {
    					result = Element.UNDEF;
    					if (l.equals(Element.UNDEF) && r.equals(Element.UNDEF))
    						capi.warning(PLUGIN_NAME, "Both operands of the '" + x + "' operator were undef.", opNode, interpreter);
    					else
    						if (l.equals(Element.UNDEF))
    							capi.warning(PLUGIN_NAME, "The left operand of the '" + x + "' operator was undef.", opNode, interpreter);
    						else
    							if (r.equals(Element.UNDEF))
    								capi.warning(PLUGIN_NAME, "The right operand of the '" + x + "' operator was undef.", opNode, interpreter);
            		}
            	}
            }
        }
        // if class of operator is unary
        if (gClass.equals(ASTNode.UNARY_OPERATOR_CLASS))
        {
            // get operand nodes
            ASTNode alpha = opNode.getFirst();
            
            // get operand values
            Element o = alpha.getValue();
            
            if (o.equals(Element.UNDEF)) {
            	result = Element.UNDEF;
				capi.warning(PLUGIN_NAME, "The operand of the unary operator '" + x + "' was undef.", opNode, interpreter);
        	} else 
	            // confirm that operand is Boolean element
	            if (o instanceof BooleanElement) {
		            // convert operand to boolean element
		            BooleanElement eO = (BooleanElement)o;
		            // logical negation
		            if (x.equals(NOT_OP)) {
		                result = BooleanElement.valueOf(! eO.getValue());
		            }
	            }
        }
        
        return result;
	}

    //--------------------------------
    // ParserPlugin Interface
    //--------------------------------
    
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
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();

			// ForallExp : 'forall' ID 'in' Term 'holds' Term
			Parser<Node> forallExpParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser(FORALL_EXP_TOKEN, PLUGIN_NAME),
						pTools.csplus(Parsers.array(idParser,
								pTools.getKeywParser("in", PLUGIN_NAME),
								termParser)),
						pTools.getKeywParser("holds", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object[] vals) {
							Node node = new ForallExpNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			} );
			parsers.put("forallExp",
					new GrammarRule("forallExp", 
							"'forall' ID 'in' Term 'holds' Term", forallExpParser, PLUGIN_NAME));

			// ExistsExp : 'exists' ID 'in' Term 'with' Term
			Parser<Node> existsExpParser = Parsers.array(
					new Parser[] {
						pTools.getKeywParser(EXISTS_EXP_TOKEN, PLUGIN_NAME),
						pTools.csplus(Parsers.array(idParser,
								pTools.getKeywParser("in", PLUGIN_NAME),
								termParser)),
						pTools.getKeywParser("with", PLUGIN_NAME),
						termParser
					}).map(
					new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object[] vals) {
							Node node = new ExistsExpNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			} );
			parsers.put("ExistsExp",
					new GrammarRule("ExistsExp", 
							"'exists' ID 'in' Term 'with' Term", existsExpParser, PLUGIN_NAME));
			
			// PredicateBasicTerm : ForallExp | ExistsExp
			Parser<Node> _parser = Parsers.or(forallExpParser, existsExpParser);
			parsers.put("BasicTerm", 
					new GrammarRule("PredicateBasicTerm", "ForallExp | ExistsExp",
							_parser, PLUGIN_NAME));
    	}
    	
    	return parsers;
    }
    
    //--------------------------------
    // InterpreterPlugin Interface
    //--------------------------------
    
    /* (non-Javadoc)
     * @see org.coreasm.engine.plugin.InterpreterPlugin#interpret(org.coreasm.engine.interpreter.Node)
     */
    public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
        if (pos instanceof ExistsExpNode) { 
            return interpretExists(interpreter, pos);
        }
        else if (pos instanceof ForallExpNode) {
            return interpretForall(interpreter, pos);
        }                        
        return null;
    }
    
    /**
     * Interprets a node representing an exists expression
     * @param pos
     * @return
     */
    private ASTNode interpretExists(Interpreter interpreter, ASTNode pos) {
        ExistsExpNode existsExpNode = (ExistsExpNode) pos;
        
        Map<ASTNode, Iterator<? extends Element>> iterators = getIteratorMap();
        Map<String, ASTNode> variableMap = null;
        
        try {
        	variableMap = existsExpNode.getVariableMap();
        }
        catch (CoreASMError e) {
        	capi.error(e);
        	return pos;
        }

        // evaluate all domains
        for (ASTNode domain : variableMap.values()) {
        	if (!domain.isEvaluated()) {
        		// SPEC: considered := {}
            	iterators.remove(domain);
                
                // SPEC: pos := beta
        		return domain;
        	}
        }
        
        if (!existsExpNode.getCondition().isEvaluated()) {
        	pos = existsExpNode.getCondition();
        	boolean shouldChoose = true;
        	for (Entry<String, ASTNode> variable : variableMap.entrySet()) {
	            if (variable.getValue().getValue() instanceof Enumerable) {
	            	   
                    // SPEC: s := enumerate(v)/considered                    
	            	Iterator<? extends Element> it = iterators.get(variable.getValue());
                	if (it == null) {
            			Enumerable domain = (Enumerable)variable.getValue().getValue();
            			if (domain.supportsIndexedView())
            				it = domain.getIndexedView().iterator();
            			else
            				it = domain.enumerate().iterator();
            			if (!it.hasNext()) {
                			for (Entry<String, ASTNode> var : variableMap.entrySet()) {
            	    			if (iterators.remove(var.getValue()) != null)
            	    				interpreter.removeEnv(var.getKey());
            	    		}
            				// [pos] := (undef,undef,ff)
                			existsExpNode.setNode(null, null, BooleanElement.FALSE);
            	            return existsExpNode;
                    	}
            			iterators.put(variable.getValue(), it);
                		shouldChoose = true;
                	}
                	else if (shouldChoose)
                		interpreter.removeEnv(variable.getKey());
                	
                	if (shouldChoose) {
	                    if (it.hasNext()) {
	                    	// SPEC: considered := considered union {t}
	                        Element chosen = it.next();
	                        shouldChoose = false;
	                        
	                        // SPEC: AddEnv(x,t)
	                        interpreter.addEnv(variable.getKey(),chosen);
	                    }   
	                    else {
	                        iterators.remove(variable.getValue());
	                        pos = existsExpNode;
	                    }
                	}
	            }
	            else {
	                capi.error("The 'exists' predicate does not apply to " + 
	                		Tools.sizeLimit(variable.getValue().getValue().denotation()) + 
	                		". The domain must be an enumerable element.", variable.getValue(), interpreter);
	            }
        	}
        	if (shouldChoose) {
        		// all combinations have been evaluated
    			// [pos] := (undef,undef,ff)             
        		pos.setNode(null,null,BooleanElement.FALSE);
        		return pos;
        	}
        }
        else {
            
            // get the value of the condition (gamma) and save it before we clear it
            boolean value = false;            
            if (existsExpNode.getCondition().getValue() instanceof BooleanElement) {
                value = ((BooleanElement) existsExpNode.getCondition().getValue()).getValue();
            }
            else {
                capi.error("value of exists condition is not Boolean.", existsExpNode.getCondition(), interpreter);
            }

            // ClearTree(gamma)
            interpreter.clearTree(existsExpNode.getCondition());
            
            if (value) {
            	for (Entry<String, ASTNode> variable : variableMap.entrySet()) {
        			if (iterators.remove(variable.getValue()) != null)
        				interpreter.removeEnv(variable.getKey());
        		}
                //considered.remove(existsExpNode.getDomain());
                
                // [pos] := (undef,undef,tt)                
                pos.setNode(null,null,BooleanElement.TRUE);
                return pos;
            }
            else {
                // pos := beta
                return existsExpNode;
            }
        }                                                
        
        return pos;
    }
    
    /**
     * Interprets a node representing a forall expression
     * @param pos
     * @return
     */
    private ASTNode interpretForall(Interpreter interpreter, ASTNode pos) {
        ForallExpNode forallExpNode = (ForallExpNode) pos;
        
        Map<ASTNode, Iterator<? extends Element>> iterators = getIteratorMap();
        Map<String, ASTNode> variableMap = null;
        
        try {
        	variableMap = forallExpNode.getVariableMap();
        }
        catch (CoreASMError e) {
        	capi.error(e);
        	return pos;
        }

        // evaluate all domains
        for (ASTNode domain : variableMap.values()) {
        	if (!domain.isEvaluated()) {
        		// SPEC: considered := {}
            	iterators.remove(domain);
                
                // SPEC: pos := beta
        		return domain;
        	}
        }
        
        if (!forallExpNode.getCondition().isEvaluated()) {
        	pos = forallExpNode.getCondition();
        	boolean shouldChoose = true;
        	for (Entry<String, ASTNode> variable : variableMap.entrySet()) {
	            if (variable.getValue().getValue() instanceof Enumerable) {
	            	   
                    // SPEC: s := enumerate(v)/considered                    
	            	Iterator<? extends Element> it = iterators.get(variable.getValue());
                	if (it == null) {
            			Enumerable domain = (Enumerable)variable.getValue().getValue();
            			if (domain.supportsIndexedView())
            				it = domain.getIndexedView().iterator();
            			else
            				it = domain.enumerate().iterator();
            			if (!it.hasNext()) {
                			for (Entry<String, ASTNode> var : variableMap.entrySet()) {
            	    			if (iterators.remove(var.getValue()) != null)
            	    				interpreter.removeEnv(var.getKey());
            	    		}
            				// [pos] := (undef,undef,tt)
                			forallExpNode.setNode(null, null, BooleanElement.TRUE);
            	            return forallExpNode;
                    	}
            			iterators.put(variable.getValue(), it);
                		shouldChoose = true;
                	}
                	else if (shouldChoose)
                		interpreter.removeEnv(variable.getKey());
                	
                	if (shouldChoose) {
	                    if (it.hasNext()) {
	                    	// SPEC: considered := considered union {t}
	                        Element chosen = it.next();
	                        shouldChoose = false;
	                        
	                        // SPEC: AddEnv(x,t)
	                        interpreter.addEnv(variable.getKey(),chosen);
	                    }   
	                    else {
	                        iterators.remove(variable.getValue());
	                        pos = forallExpNode;
	                    }
                	}
	            }
	            else {
	                capi.error("The 'forall' predicate does not apply to " + 
	                		Tools.sizeLimit(variable.getValue().getValue().denotation()) + 
	                		". The domain must be an enumerable element.", variable.getValue(), interpreter);
	            }
        	}
        	if (shouldChoose) {
        		// all combinations have been evaluated
    			// [pos] := (undef,undef,tt)             
        		pos.setNode(null,null,BooleanElement.TRUE);
        		return pos;
        	}
        }
        else {
            
            // get the value of the condition (gamma) and save it before we clear it
            boolean value = false;            
            if (forallExpNode.getCondition().getValue() instanceof BooleanElement) {
                value = ((BooleanElement) forallExpNode.getCondition().getValue()).getValue();
            }
            else {
                capi.error("value of forall condition is not Boolean.", forallExpNode.getCondition(), interpreter);
            }
            
            // ClearTree(gamma)
            interpreter.clearTree(forallExpNode.getCondition());
            
            if (value) {
                // pos := beta
                return forallExpNode;                
            }
            else {
            	for (Entry<String, ASTNode> variable : variableMap.entrySet()) {
        			if (iterators.remove(variable.getValue()) != null)
        				interpreter.removeEnv(variable.getKey());
        		}
                //considered.remove(forallExpNode.getDomain());
                
                // [pos] := (undef,undef,ff)             
                pos.setNode(null,null,BooleanElement.FALSE);
                return pos;
            }
        }                                                
        
        return pos;
    }

    public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

}
