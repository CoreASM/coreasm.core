/*	
 * $Id: //CoreASM/development/main-concurrent/src/org/coreasm/engine/interpreter/InterpreterImp.java#17 $
 * 
 * InterpreterImp.java 	1.0 	$Revision: 253 $
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-05-15 01:40:58 +0200 (So, 15 Mai 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.interpreter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.EngineTools;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.NameElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.Node.NameNodeTuple;
import org.coreasm.engine.kernel.EnclosedTermNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.coreasm.engine.kernel.RuleOrFuncElementNode;
import org.coreasm.engine.kernel.UpdateRuleNode;
import org.coreasm.engine.parser.OperatorRegistry;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.UndefinedIdentifierHandler;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Implements the <code>Interpreter</code> interface.
 *   
 * @author Roozbeh Farahbod, Marcel Dausend
 */
public class InterpreterImp implements Interpreter {

	/** Keeps a mapping between running threads and interpreter instances */
	protected static final ThreadLocal<Interpreter> interpreters = new ThreadLocal<Interpreter>();

	private static final Logger logger = LoggerFactory.getLogger(InterpreterImp.class);
	
	/** Current node to be interpreted */
	protected ASTNode pos;
	
	/** Current value of 'self' */
	protected Element self = Element.UNDEF;
	
	/** Link to the ControlAPI module */
	private final ControlAPI capi;

	//private Map<String,Element> envMap;
	private final Map<String,Stack<Element>> envMap;
	
	/** Work copy of a tree */
	private final Map<ASTNode,Stack<ASTNode>> workCopy;
	
	/** Link to the abstract storage module */
	private final AbstractStorage storage;
	
	private OperatorRegistry oprReg = null;
	
	private final Map<String, Collection<String>> oprImpPluginsCache = new HashMap<String, Collection<String>>();
	
	private final Stack<CallStackElement> ruleCallStack = new Stack<CallStackElement>();

	/**
	 * Creates a new interpreter with a link to the given
	 * ControlAPI module.
	 */
	public InterpreterImp(ControlAPI capi) {
		((ch.qos.logback.classic.Logger)logger).setLevel(ch.qos.logback.classic.Level.ERROR);	// added this line to temporarily turn off the logger
		this.capi = capi;
		this.envMap = new HashMap<String, Stack<Element>>();
		this.storage = capi.getStorage();
		this.workCopy = new HashMap<ASTNode,Stack<ASTNode>>();
		interpreters.set(this);
	}
	
	public Interpreter getInterpreterInstance() {
		Interpreter result = interpreters.get();
		if (result == null)
			return this;
		else 
			return result;
	}
    
	public void executeTree() throws InterpreterException {
	
		// !!! IMPORTANT !!!
		// 'pos' should not be changed by other methods that are called
		// from within this method. It should be passed to them and a 
		// new value should be retrieved. 
		// As it is now on 28-Aug-2006.
	
		if (!pos.isEvaluated()) {
			// notification for observers (i.e. debugger)
			notifyListenersBeforeNodeEvaluation(pos);
			
			final String pName = pos.getPluginName();
			
			if (logger.isDebugEnabled()) {
				logger.debug("Interpreting node {} @ {}.", pos.toString(), pos.getContext(capi.getParser(), capi.getSpec()));
			}
			
			if (pName != null && !pName.equals(Kernel.PLUGIN_NAME)) {
				logger.debug("Using plugin {}.", pName);
				Plugin p = capi.getPlugin(pName);
				if (p instanceof InterpreterPlugin){
					pos = ((InterpreterPlugin)p).interpret(this, pos);
				} else {
					throw new InterpreterException("Pluging '" + p.getName() + "' is not an interpreter plugin.");
				}
			} else {
				pos = kernelInterpreter(pos);
			}

			// TODO Deviating from the spec (needs to be handled properly)
			//      the following statement deviates from the spec in response to 
			//      a problem with undefined identifiers being used where a rule 
			//      is expected
			// UPDATE: this should not be a problem anymore as unknown identifiers
			//         used as macro-call rules are prevented and reported by the 
			//         engine. But in general, this is not a bad guard.
			if (pos.isEvaluated() && pos.getUpdates() == null)
				pos.setNode(pos.getLocation(), new UpdateMultiset(), pos.getValue());
			
			if (pos.isEvaluated())
				notifyListenersAfterNodeEvaluation(pos);
			
		} else {
			if (pos.getParent() != null) 
				pos = pos.getParent();
		}
	}

	/**
	 * Notifies the listeners before a node is being evaluated.
	 * 
	 * @param pos the node being evaluated
	 */
	private void notifyListenersAfterNodeEvaluation(ASTNode pos) {
		for (InterpreterListener listener : capi.getInterpreterListeners())
			listener.afterNodeEvaluation(pos);
	}

	/**
	 * Notifies the listeners after a node is being evaluated.
	 * 
	 * @param pos the node being evaluated
	 */
	private void notifyListenersBeforeNodeEvaluation(ASTNode pos) {
		for (InterpreterListener listener : capi.getInterpreterListeners())
			listener.beforeNodeEvaluation(pos);
	}

	/**
	 * <i>KernelInterpreter</i> rule that performs kernel interpretation of the
	 * current node at <i>pos</i>.
	 */
	private ASTNode kernelInterpreter(ASTNode pos) throws InterpreterException {
		ASTNode newPos = null;
		
		/*
		 * Here I needed to deviate from the ASM spec. The reason is, in the 
		 * spec whenever the pos is assigned to a new node, the change does not
		 * take effect in the current step, so other guards will not be activated 
		 * (e.g., an expression may change pos and pos will point to a rule call in the 
		 * next step). But in sequential programming world, the moment a rule (method) 
		 * changes pos, pos is changed! What happens then is that the rest of the 
		 * conditional statements will look at the new pos, while they shouldn't. 
		 * 
		 * To deal with this problem, I ask each method to give me the new value of pos
		 * as they wanted it to be, and won't call the rest if there already is a
		 * request to change pos (i.e., pos is interpreted already).  
		 * 
		 *  Roozbeh, 27-Jan-2006
		 * 
		 */
		
		// first trying literals
		newPos = interpretLiterals(pos);
		
		// if they didn't change pos 
		// I changed it so that if it is evaluated, it won't be evaluated again -- Roozbeh 21-May-2007
		if (newPos == pos && !pos.isEvaluated()) {
			// try expressions
			newPos = interpretExpressions(pos);
			if (newPos == pos && !pos.isEvaluated())
				// if pos was not an expression, try rules
				newPos = interpretRules(pos);
		}
		// return the new pointer to pos (could be the same as the old one)
		return newPos;
	}
	
	public boolean isExecutionComplete() {
		return (pos.getParent() == null && pos.isEvaluated());
	}

	public void setPosition(ASTNode node) {
		pos = node;
	}

	public ASTNode getPosition() {
		return pos;
	}
	 
	/**
	 * Sets the value of 'self' for this interpreter instance.
	 * Also, inserts current program of agent self into callStack
	 * 
	 * @param newSelf
	 *            reference to the self element of an agent
	 */
	public void setSelf(Element newSelf) {
//		if (capi.getEngineMode() == CoreASMEngine.EngineMode.emRunningAgents)
//			throw new EngineError("Cannot set value of 'self' while a program is being evaluated.");
		this.self = newSelf;
		ruleCallStack.insertElementAt(
				new CallStackElement((RuleElement)storage.getChosenProgram(newSelf)), 0);
	}
	
	public Element getSelf() {
		return this.self;
	}
	
	@Override
	public Map<String, Element> getEnvVars() {
		Map<String, Element> envVars = new HashMap<String, Element>();
		for (Entry<String, Stack<Element>> envVar : this.envMap.entrySet()) {
			Stack<Element> stack = envVar.getValue();
			if (!stack.isEmpty())
				envVars.put(envVar.getKey(), stack.peek());
		}
		return envVars;
	}

	public Element getEnv(String token) {
		Stack<Element> stack = envMap.get(token);
		if (stack == null || stack.size() == 0) 
			return null;
		else
			return stack.peek();
	}

	/*
	 * Sets the value of <i>env(token)</i>.
	public void setEnv(String token, Element value) {
		if (value == null)
			envMap.remove(token);
		else
			envMap.put(token, value);
	}
	*/
	

	public void addEnv(String name, Element value) {
		Stack<Element> stack = envMap.get(name);
		if (stack == null) {
			// if this is the first time
			// setting a value for this variable,
			// create the stack
			stack = new Stack<Element>();
			envMap.put(name, stack);
		}
		stack.push(value);
	}

	public void removeEnv(String name) {
		Stack<Element> stack = envMap.get(name);
		if (stack == null || stack.size() <= 0) 
			throw new IllegalStateException("Removing an undefined environment variable.");

		stack.pop();
	}
	
	/**
	 * Interpretation of literals
	 */
	private ASTNode interpretLiterals(ASTNode pos) {
		final String token = pos.getToken();
		if (token == null) 
			return pos;
		else if (token.equals(Kernel.KW_TRUE))
			pos.setNode(null, null, BooleanElement.TRUE);
		else if (token.equals(Kernel.KW_FALSE)) 
			pos.setNode(null, null, BooleanElement.FALSE);
		else if (token.equals(Kernel.KW_UNDEF)) 
			pos.setNode(null, null, Element.UNDEF);
		else if (token.equals(Kernel.KW_SELF))
			pos.setNode(null, null, self);
		return pos;
	}
	
	/**
	 * Interpretation of kernel expressions
	 * @throws InterpreterException 
	 */
	private ASTNode interpretExpressions(ASTNode pos) throws InterpreterException {
		final AbstractStorage storage = capi.getStorage();
		final String gClass = pos.getGrammarClass();
		String x = pos.getToken();
		
		// If the current node is a function/rule term
		if (gClass.equals(ASTNode.FUNCTION_RULE_CLASS)) {
			if (pos instanceof FunctionRuleTermNode) {
				FunctionRuleTermNode frNode = (FunctionRuleTermNode)pos;
				
				// If the current node is of the form 'x' or 'x(...)'
				if (frNode.hasName()) {
	
					x = frNode.getName();
					
					// If the current node is of the form 'x' with no arguments
					if (!frNode.hasArguments()) {
						
						// If we have a local value for that...
						if (getEnv(x) != null)
							pos.setNode(null, null, getEnv(x));
						else {
							// If this 'x' refers to a function in the state...
							final FunctionElement f = storage.getFunction(x);
							if (f != null && f.isModifiable() || isUndefined(x)) {
								try {
									if (storage.getValue(new Location(x, ElementList.NO_ARGUMENT)) instanceof RuleElement)
										return pos;	// let interpretRules handle it
								} catch (InvalidLocationException e) {
									throw new EngineError("Location is invalid in 'interpretRules()'." + 
											"This cannot happen!");
								}
							}
//							if (storage.isFunctionName(x)) {
							if (f != null) {
								final Location l = new Location(x, ElementList.NO_ARGUMENT, f.isModifiable());
								try {
									pos.setNode(l, null, storage.getValue(l));
								} catch (InvalidLocationException e) {
									throw new EngineError("Location is invalid in 'interpretExpressions()'." + 
											"This cannot happen!");
								}
							} else
								// if this 'x' is not defined before...
								if (isUndefined(x)) {
									handleUndefinedIdentifier(pos, x, ElementList.NO_ARGUMENT);
								}
						}
					} else { // if current node is 'x(...)' (with arguments)
						
						// If this 'x' refers to a function in the state...
						final FunctionElement f = storage.getFunction(x);
						if (f != null && f.isModifiable() || isUndefined(x)) {
							try {
								Element e = storage.getValue(new Location(x, ElementList.NO_ARGUMENT));
								if (e instanceof RuleElement)
									return pos;	// let interpretRules handle it
							} catch (InvalidLocationException e) {
								throw new EngineError("Location is invalid in 'interpretRules()'." + 
										"This cannot happen!");
							}
						}
						if (f != null) {
							final List<ASTNode> args = frNode.getArguments();
							// look for the parameter that needs to be evaluated
							final ASTNode toBeEvaluated = getUnevaluatedNode(args);
							if (toBeEvaluated == null) {
								// if all nodes are evaluated...
								final ElementList vList = EngineTools.getValueList(args);
								final Location l = new Location(x, vList, f.isModifiable());
								try {
									pos.setNode(l, null, storage.getValue(l));
								} catch (InvalidLocationException e) {
									throw new EngineError("Location is invalid in 'interpretExpressions()'." + 
											"This cannot happen!");
								}
							} else
								pos = toBeEvaluated;
						} else
							// if 'x' is not defined
							if (isUndefined(x)) {
								final List<ASTNode> args = frNode.getArguments();
								// look for the parameter that needs to be evaluated
								final ASTNode toBeEvaluated = getUnevaluatedNode(args);
								if (toBeEvaluated == null) {
									// if all nodes are evaluated...
									ElementList vList = EngineTools.getValueList(args);
									handleUndefinedIdentifier(pos, x, vList);
								} else
									pos = toBeEvaluated;
							}
					}
					
				} // endif of the current node being 'x' or 'x(...)'
			} 
			
		} //endif of the current node being a function/rule term
		
		// if class is an operator then
		else if (gClass.equals(ASTNode.UNARY_OPERATOR_CLASS) || 
                 gClass.equals(ASTNode.BINARY_OPERATOR_CLASS) ||
                 gClass.equals(ASTNode.TERNARY_OPERATOR_CLASS) ||
                 gClass.equals(ASTNode.INDEX_OPERATOR_CLASS))
		{
			pos = interpretOperators(pos);
		}
		// else another general type of expression
		else if (gClass.equals(ASTNode.EXPRESSION_CLASS))
		{
			// for 'ruleelement' expression
			if (pos.getGrammarRule().equals(Kernel.GR_RULEELEMENT_TERM))
			{
				final ASTNode idNode = pos.getFirst();
					
				// attempt get rule element for given rule
				final String ruleName = idNode.getToken();
				final RuleElement ruleElement = capi.getStorage().getRule(ruleName);
							
				// if rule element exists
				if (ruleElement != null)
					pos.setNode(null,null,ruleElement);
				// else no such rule exists return undef
				else
					pos.setNode(null,null,Element.UNDEF);
			}
			
			else if (pos instanceof RuleOrFuncElementNode) {
				final RuleOrFuncElementNode node = (RuleOrFuncElementNode)pos;
				final String name = node.getElementName();
				
				Element e = storage.getRule(name);
				if (e == null) 
					e = storage.getFunction(name);
				
				if (e != null) {
				    if (e instanceof FunctionElement) {
                        if (((FunctionElement) e).isModifiable()) {
                            Location l = new Location(AbstractStorage.FUNCTION_ELEMENT_FUNCTION_NAME, ElementList.create(new NameElement(name)));
                            pos.setNode(l,null,e);
                        }
                        else {
                            pos.setNode(null,null,e);                            
                        }
                    }
                    else if (e instanceof RuleElement) {
                        Location l = new Location(AbstractStorage.RULE_ELEMENT_FUNCTION_NAME, ElementList.create(new NameElement(name)));
                        pos.setNode(l,null,e);
                    }
                    else {
                        pos.setNode(null,null,e);
                    }
                }
				else {
					pos.setNode(null, null, Element.UNDEF);
                }
			} else
				// if pos is of the form '(' ... ')'
				if (pos instanceof EnclosedTermNode) {
					final ASTNode innerNode = pos.getFirst();
					if (innerNode.isEvaluated())
						pos.setNode(null, null, innerNode.value);
					else
						pos = innerNode;
				}
		}
	
		return pos;
	}
    
	/**
	 * Interpretation of kernel rules
	 * @throws InterpreterException 
	 */
	private ASTNode interpretRules(ASTNode pos) throws InterpreterException {
		final String gRule = pos.getGrammarRule();
		String x = pos.getToken();
		
		// If the current node is a macro call term...
		if (gRule.equals(Kernel.GR_FUNCTION_RULE_TERM) || pos instanceof MacroCallRuleNode) {
			FunctionRuleTermNode frNode = null;
			if (pos instanceof MacroCallRuleNode) 
				frNode = (FunctionRuleTermNode)pos.getFirst();
			else
				frNode = (FunctionRuleTermNode)pos;
			
			// If the current node is of the form 'x' or 'x(...)'
			if (frNode.hasName()) {

				x = frNode.getName();
				RuleElement theRule = null;
				
				if (storage.isRuleName(x))
					theRule = ruleValue(x);
				else {
					try {
						Element e = storage.getValue(new Location(x, ElementList.NO_ARGUMENT));
						if (e instanceof RuleElement)
							theRule = (RuleElement)e;
						else if (pos instanceof MacroCallRuleNode)
							capi.error("\"" + x + "\" is not a rule name.", pos, this);
					} catch (InvalidLocationException e) {
						throw new EngineError("Location is invalid in 'interpretRules()'." + 
								"This cannot happen!");
					}
				}
				
				if (theRule != null) {
				
					// If the current node is of the form 'x' with no arguments
					if (!frNode.hasArguments()) {
						
						// check the arity of the rule
						if (theRule.getParam().size() == 0) 
							pos = ruleCall(theRule, null, pos);
						else if (pos instanceof MacroCallRuleNode)
							capi.error("The number of arguments passed to '" + x  + 
									"' does not match its signature.", pos, this);
						else	// treat rules like RuleOrFuncElementNode if they aren't a MacroRuleCallNode
							pos.setNode(new Location(AbstractStorage.RULE_ELEMENT_FUNCTION_NAME, ElementList.create(new NameElement(x))),null,theRule);
					
					} else { // if current node is 'x(...)' (with arguments)
						
						if (theRule != null) {
							if (theRule.getParam().size() == frNode.getArguments().size())
								pos = ruleCall(theRule, frNode.getArguments(), pos);
							else
								capi.error("The number of arguments passed to '" + x  + 
										"' does not match its signature.", pos, this);
						}
					}
				}
			}
		}
		
		// If the current node is an assignment
		else if (pos instanceof UpdateRuleNode) {
			final ASTNode lhs = pos.getFirst();
			final ASTNode rhs = pos.getFirst().getNext();
			
			// if LHS is not evaluated...
			if (!lhs.isEvaluated())
				pos = lhs;
			else
				// if RHS is not evaluated...
				if (!rhs.isEvaluated())
					pos = rhs;
				else {
					Location l = lhs.getLocation();
					if (l != null) {
						// Updated by R. Farahbod on 03-Nov-2008
						if (l.isModifiable != null && l.isModifiable.equals(false)) 
							capi.error("Left hand side of the assignment, " +
									l + ", is not modifiable.", pos, this);
						else {
							Update u = new Update(l, rhs.getValue(), Update.UPDATE_ACTION, self, pos.scannerInfo);
							pos.setNode(null, new UpdateMultiset(u), null);
						}
					}
					else
						capi.error("Cannot update a non-location!", pos, this);
				}
		}
		
		//If the current node is an 'import'
		else if (gRule.equals("ImportRule")) {
			final String id = pos.getFirst().getToken();
			final ASTNode ruleNode = pos.getFirst().getNext();
			
			if (!ruleNode.isEvaluated()) {
				addEnv(id, capi.getStorage().getNewElement());
				pos = ruleNode;
			} else {
				removeEnv(id);
				pos.setNode(null, ruleNode.getUpdates(), null);
			}
			
		}

		//If the current node is an 'skip'
		else if (x != null && x.equals("skip")) {
			pos.setNode(null, new UpdateMultiset(), null);
		}
		

		return pos;
	}
	
	/**
	 * Interpretation of operators
	 * 
	 * @throws InterpreterException 
	 */
	private ASTNode interpretOperators(ASTNode pos) throws InterpreterException {
		String gClass = pos.getGrammarClass();
		String x = pos.getToken();
		
		// evaluate children first:
		
		// find first unevaluated child
		ASTNode unevaluatedChild = pos.getFirst();
		while (unevaluatedChild != null && unevaluatedChild.isEvaluated() == true)
			unevaluatedChild = unevaluatedChild.getNext();
		
		// if there is an unevaluated child, then we need to pass control to that
		// child so that it can be evaluated
		if (unevaluatedChild != null)
			pos = unevaluatedChild;
		// else no unevaluated children, so we can commence operator interpretation
		else
		{
			if (oprReg == null) 
				oprReg = OperatorRegistry.getInstance(capi);
			
			// collection of all plugins which have an implementation for this operator
			Collection<String> impPlugins = oprImpPluginsCache.get(x + "__:X:__" + gClass);
			// that is a bad thing, no? ;-)
			if (impPlugins == null) {
				impPlugins = oprReg.getOperatorContributors(x,gClass);
				oprImpPluginsCache.put(x + "__:X:__" + gClass, impPlugins);
			}
			// hash table which holds all the results and errors
			final Hashtable<String,Element> impResults = new Hashtable<String,Element>();
			final Hashtable<String,InterpreterException> impErrors = new Hashtable<String,InterpreterException>();
			final HashSet<String> nullReturns = new HashSet<String>();
			
			// TODO What is the diff between returning 'null' and throwing an exception?
			
			// for each possible implementation
			Iterator<String> itImpPlugins = impPlugins.iterator();
			while (itImpPlugins.hasNext())
			{
				// load plugin
				String pluginName = itImpPlugins.next();
				OperatorProvider opImp = (OperatorProvider)capi.getPlugin(pluginName);
				
				// result can be a value or an interpreter exception thrown.
				try
				{
					Element result = null;
					result = opImp.interpretOperatorNode(this, pos);
					
					if (result == null)
						nullReturns.add(pluginName);
					else
						impResults.put(pluginName,result);
				}
				catch (InterpreterException error)
				{
					// add error to hash table
					impErrors.put(pluginName,error);
				}
				
				
			}
			
			// decide on what final result of operator evaluation is:
			
			// put results into a set
			final HashSet<Element> setResultElements = new HashSet<Element>();
			for(Element result: impResults.values())
					setResultElements.add(result);

			// if one of the results is undef but there are other results as well,
			// remove the undef value
			if ((setResultElements.size() > 1) && setResultElements.contains(Element.UNDEF)) 
				setResultElements.remove(Element.UNDEF);
			
			// one result so return it
			if (setResultElements.size() == 1)
				pos.setNode(null,null,(Element)setResultElements.toArray()[0]);
			// multiple results so error
			else if (setResultElements.size() > 1)
			{
				// build error message
				String errMessage = "Different results produced for operator \""+x+"\" by plugins implementing it:\n";
				for(String pluginName: impResults.keySet())
				{
					errMessage += "- plugin \""+pluginName+"\" resulted in value \""+impResults.get(pluginName).toString()+"\".\n";
				}
				capi.error(errMessage,pos, this);
                return pos;
			}
			// all plugins result in error or unknown semantics
			else if (setResultElements.size() == 0)
			{
				// build error message
				String operands = "(" + pos.getFirst().getValue().denotation();
				ASTNode opr = pos.getFirst().getNext();
				while (opr != null) {
					operands = operands + ", " + opr.getValue().denotation();
					opr = opr.getNext();
				}
				operands = operands + ")";
				
				String errMessage = "Cannot perform the \"" + x + "\" operation on " + operands + " as all the implementations failed:" + Tools.getEOL();
				for(String errorPlugin: impErrors.keySet())
					errMessage += "- " + errorPlugin + ": " + impErrors.get(errorPlugin).getMessage()  + Tools.getEOL();
				for(String nullReturnedPlugin: nullReturns)
					errMessage += "- " + nullReturnedPlugin + " has no semantics for the given combination of operator and operand(s)." + Tools.getEOL();
				
                capi.error(errMessage, pos, this);
                return pos;
			}
			
			
		}
			
		return pos;	
	}
		
	/**
	 * Return <code>true<code> if there is no rule or function in the
	 * state with the given token as its name.
	 * 
	 * NOTE: The implementation of this method is based on 
	 * <code>isFunctionName(String)</code> and <code>isRuleName(String)</code>
	 * and is different from the specification.  
	 */
	private boolean isUndefined(String token) {
		return !storage.isRuleName(token) 
				&& !storage.isFunctionName(token) 
				&& !storage.isUniverseName(token);
	}
	
	/**
	 * Takes care of undefined identifiers. 
	 * 
	 * In this implementation, it creates a new function in the 
	 * state with the given name and evaluates <i>pos</i> to point
	 * to this function both in terms of its location and its value (which is <i>undef</i>)
	 */
	private synchronized void handleUndefinedIdentifier(ASTNode pos, String id, ElementList list) {
    	// if it is still the case that the function is undefined
		if (!isUndefined(id))
			return;
		
        Location l = null;
        Element value = null;
        UpdateMultiset updates = null;
        for (Plugin p: capi.getPlugins()) {
		    if (p instanceof UndefinedIdentifierHandler) {
//		    	What is this line needed for?
//		    	It's significantly slowing down rulecalls on rules containing a return rule.
//		    	Especially recursive rulecalls are slowed down a lot. See fibonacci sample spec.
//              clearTree(pos);
                ((UndefinedIdentifierHandler) p).handleUndefinedIndentifier(this, pos, id, list);                
                
                if (pos.isEvaluated()) {
                    if (l!=null && value!=null && updates != null) {
                        if (!l.equals(pos.getLocation())
                        		|| !value.equals(pos.getValue())
                        		|| !updates.equals(pos.getUpdates())) {
                            throw new EngineError(
                            		"There is an amibuity in resolving identifier \""+id+"\". "
                            		+ "More than one plug-in can evaluate this node."); 
                        }
                    }
                    l = pos.getLocation();
                    value = pos.getValue();
                    updates = pos.getUpdates();
                }
            }
        }
        
        if (!pos.isEvaluated()) {
            kernelHandleUndefinedIndentifier(pos,id,list);
        }        
	}
	
	/*
	 * Kernel's default behavior to handle undefined identifier 
	 */
    private synchronized void kernelHandleUndefinedIndentifier(ASTNode pos, String id, ElementList list) {
    	Element value = null;
    	Location loc = new Location(id, list);
    	try {
    		// in case there is a value in the stack
			value = storage.getValue(loc);
	        pos.setNode(loc, null, value);
		} catch (InvalidLocationException e) {
	        pos.setNode(loc, null, Element.UNDEF);
		} 
    }

	/*
	 * Kernel's DEPRECATED default behavior to handle undefined identifier 
	 *
    private synchronized void kernelHandleUndefinedIndentifier(ASTNode pos, String id, ElementList list) {
        FunctionElement f = new MapFunction(Element.UNDEF);
        try {
            storage.addFunction(id, f); 
            pos.setNode(new Location(id, list), null, Element.UNDEF);
        } catch (NameConflictException e) {
            throw new EngineError("There is a name conflict (in 'handleUndefinedIdentifier(String, ElementList)') for \"" + id + "\"."); 
        }
    }
    */

	/**
	 * The goal is to ensure that the given nodes are all evaluated. If there is 
	 * an unevaluated node, returns that node. If all the given nodes are evaluated
	 * returns <code>null</code>. 
	 * 
	 * @param nodes list of nodes
	 */
	private ASTNode getUnevaluatedNode(List<ASTNode> nodes) {
		for (ASTNode n: nodes) 
			if (!n.isEvaluated()) {
				return n;
			}
		return null;
	}
	
	/** 
	 * Returns the rule element of the state that has the specified name.
	 * @param name name of the rule 
	 */
	private RuleElement ruleValue(String name) {
		return storage.getRule(name);
	}
	
	/**
	 * Handles a call to a rule.
	 * 
	 * @param rule rule element
	 * @param args arguments
	 * @param pos current node being interpreted
	 */
	public synchronized ASTNode ruleCall(RuleElement rule, List<ASTNode> args, ASTNode pos) {
		if (logger.isDebugEnabled()) {
			logger.debug("Interpreting rule call '" + rule.name + "' (agent: " + this.getSelf() + ", stack size: " + ruleCallStack.size() + ")");
		}
		
		// Using a stack of workcopies because copyTreeSub returns an equal copy of the rule body
		Stack<ASTNode> wCopyStack = workCopy.get(pos);
		if (wCopyStack == null) {
			wCopyStack = new Stack<ASTNode>();
			workCopy.put(pos, wCopyStack);
		}
		ASTNode wCopy = null;
		// if the current pos is the parent of the workcopy on the top of the stack we have already evaluated the rulecall
		if (!wCopyStack.isEmpty() && pos == wCopyStack.peek().getParent())
			wCopy = wCopyStack.peek();
		// If there is no work copy created for this rule call
		if (wCopy == null) {
			// checking the parameters and the arguments
			// as their number should match
			ruleCallStack.push(new CallStackElement(rule));
			if ((rule.getParam() != null) && (args != null) 
					&& (args.size() != rule.getParam().size())) {  
				capi.error("Number of arguments does not match the number of parameters.", pos, this);
				return pos;
			}
			wCopy = copyTreeSub(rule.getBody(), rule.getParam(), args);
			wCopyStack.push(wCopy);
			wCopy.setParent(pos);
			notifyOnRuleCall(rule, args, pos, self);
			return wCopy; // as new value of 'pos'
		} else { // if there already is a work copy
			Element value = wCopy.getValue();
			if (value == null)	// make sure that the value of the node will not be set to null
				value = Element.UNDEF;
			pos.setNode(null, wCopy.getUpdates(), value);
		
			// making it easier for the garbage collector 
			// to throw this copy out! :)
			wCopy.dipose();
			
			wCopyStack.pop();
			if (wCopyStack.isEmpty())
				workCopy.remove(pos);
			ruleCallStack.pop();
			notifyOnRuleExit(rule, args, pos, self);
			return pos;
		}
	}
	
	/**
	 * Notifies the listeners on rule call.
	 * @param rule the rule that is being called
	 * @param args the arguments being passed with the call
	 * @param pos the node of the rule
	 * @param agent the executing agent
	 */
	private void notifyOnRuleExit(RuleElement rule, List<ASTNode> args, ASTNode pos, Element agent) {
		for (InterpreterListener listener : capi.getInterpreterListeners())
			listener.onRuleExit(rule, args, pos, agent);
	}
	
	/**
	 * Notifies the listeners on rule exit.
	 * @param rule the rule that is being exited
	 * @param args the arguments that have been passed with the call
	 * @param pos the node of the rule
	 * @param agent the executing agent
	 */
	private void notifyOnRuleCall(RuleElement rule, List<ASTNode> args, ASTNode pos, Element agent) {
		for (InterpreterListener listener : capi.getInterpreterListeners())
			listener.onRuleCall(rule, args, pos, agent);
	}

	/**
	 * @see Interpreter#copyTreeSub(ASTNode, List, List)
	 */
	public ASTNode copyTreeSub(ASTNode a, List<String> params, List<ASTNode> args) {
		return (ASTNode)copyTreeSub(a, params, args, null);
	}

	/**
	 * Returns a copy of the given parse tree, where every instance 
	 * of an identifier node in a given sequence (formal parameters) 
	 * is substituted by a copy of the corresponding parse tree in another 
	 * sequence (actual parameters, or arguments). We assume that the elements in the 
	 * formal parameters list are all distinct (i.e., it is not possible 
	 * to specify the same name for two different parameters).
	 * 
	 * @param a root of the parse tree
	 * @param params formal parameters
	 * @param args given arguments (replace parameters in the tree)
	 * @param parent parent of the created node
	 */
	private Node copyTreeSub(Node a, List<String> params, List<ASTNode> args, Node parent) {
		Node result = null;
		ASTNode ast = null;
		int i = 0;
		
		if (a instanceof ASTNode) 
			ast = (ASTNode)a;
		
		if (a != null) {
			// if this node belongs to the abstract syntax tree
			// and it is a FunctionRuleTerm and its child is a parameter of the rule
			if (a instanceof ASTNode 
					&& ast.getGrammarClass().equals(ASTNode.FUNCTION_RULE_CLASS) 
					&& (ast.getFirst().getGrammarClass().equals(ASTNode.ID_CLASS) 
							&& (i = params.indexOf(ast.getFirst().getToken())) >= 0)) {
				ASTNode arg = args.get(i);
				if (arg instanceof RuleOrFuncElementNode) {
					FunctionRuleTermNode frNode = new FunctionRuleTermNode(arg.getScannerInfo());
					frNode.addChild("alpha", arg.getFirst());
					arg = frNode;
				}
				result = copyTree(arg);
				for (NameNodeTuple child : ast.getChildNodesWithNames()) {
					if (!"alpha".equals(child.name))	// don't copy the id of this node
						result.addChild(child.name, copyTreeSub(child.node, params, args, result));
				}
				result.setParent(parent);
			} else { 
				result = a.duplicate();
				result.setParent(parent);
				for (NameNodeTuple child: a.getChildNodesWithNames())
					result.addChild(child.name, copyTreeSub(child.node, params, args, result));
			}
			
		}
		return result;
	}

	public Node copyTree(Node a) {
		return a.cloneTree();
	}
	
	/* The following methods are removed/changed by Roozbeh Farahbod */ 
//
//	/**
//	 * Returns a copy of the given parse tree, where every instance 
//	 * of an identifier node in a given sequence (formal parameters) 
//	 * is substituted by a copy of the corresponding parse tree in another 
//	 * sequence (actual parameters, or arguments). We assume that the elements in the 
//	 * formal parameters list are all distinct (i.e., it is not possible 
//	 * to specify the same name for two different parameters).
//	 * 
//	 * @param root root of the parse tree
//	 * @param params formal parameters
//	 * @param args given arguments (replace parameters in the tree)
//	 * @param parent parent of the created node
//	 */
//	private Node copyTreeSub(Node a, List<String> params, List<ASTNode> args, ASTNode parent) {
//		Node result = null;
//		ASTNode ast = null;
//		int i = 0;
//		
//		if (a instanceof ASTNode) 
//			ast = (ASTNode)a;
//		
//		if (a != null) {
//			// if this node belongs to the abstract syntax tree
//			// and it is a FunctionRuleTerm and its child is a parameter of the rule
//			if (a instanceof ASTNode 
//					&& ast.getGrammarClass().equals(ASTNode.FUNCTION_RULE_CLASS) 
//					&& (ast.getFirst().getGrammarClass().equals(ASTNode.ID_CLASS) 
//							&& (i = params.indexOf(ast.getFirst().getToken())) >= 0)) {
//				result = copyTree(args.get(i), parent, false);	
//				result.setNext(copyTreeSub(a.getNext(), params, args, result.getParent()));
//			} else { 
//				result = a.duplicate();
//				result.setParent(parent);
//				result.setFirst(copyTreeSub(a.getFirst(), params, args, result));
//				result.setNext(copyTreeSub(a.getNext(), params, args, result.getParent()));
//			}
//			
//			/* Old buggy code
//			if (a.getGrammarClass().equals(ASTNode.ID_CLASS) && (i = params.indexOf(a.getToken())) >= 0) {
//				result = copyTree(args.get(i), parent);	
//			} else { 
//				result = a.duplicate();
//				result.setParent(parent);
//				result.setFirst(copyTreeSub(a.getFirst(), params, args, result));
//				result.setNext(copyTreeSub(a.getNext(), params, args, result.getParent()));
//			}
//			*/
//		}
//		return result;
//	}
//
//	/**
//	 * @see Interpreter#copyTree(ASTNode)
//	 */
//	public ASTNode copyTree(ASTNode a) {
//		return copyTree(a, null, true);
//	}
//	
//	/**
//	 * Makes a deep copy of a sub tree with its root at <code>a</code>.
//	 * All the connected nodes (except the parent node) are duplicated.
//	 * It then sets the parent of the node to the given parent.
//	 * 
//	 * @param a root of a tree
//	 * @param parent parent of the new node
//	 * @param setNext if <code>true</code>, this method also copies the
//	 * 					next sibling of the node
//	 * @return a copy of the tree 
//	 */
//	private ASTNode copyTree(ASTNode a, ASTNode parent, boolean setNext) {
//		ASTNode result = null;
//		
//		if (a != null) {
//			result = a.duplicate();
//			result.setParent(parent);
//			result.setFirst(copyTree(a.getFirst(), result, true));
//			if (setNext)
//				result.setNext(copyTree(a.getNext(), result.getParent(), true));
//		}
//		return result;
//	}
	/* end of removed block */
	
	/**
	 * @see org.coreasm.engine.interpreter.Interpreter#clearTree(org.coreasm.engine.interpreter.ASTNode)
	 */
	public void clearTree(ASTNode root) {
		if (root != null) {
			root.setNode(null, null, null);
			clearTree(root.getFirst());
			clearTree(root.getNext());
		}
	}
	
	public void prepareInitialState() {
		AbstractStorage storage = capi.getStorage();
		
		// starting from the first child under CoreASM keyword
		ASTNode rootNode = capi.getParser().getRootNode();
		ASTNode initNode = null;
		
		for (ASTNode child: rootNode.getAbstractChildNodes())
			if (child.getGrammarRule().equals(Kernel.GR_INITIALIZATION))
				if (initNode == null)
					initNode = child;
				else {
					logger.error("Too many 'init' rule declarations.");
					capi.error("More than one init rule declarations found.", child, this);
					return;
				}
					
		if (initNode == null) {
			logger.debug("No init rule is specified.");
			capi.error("No init rule is specified.");
			return;
		}
		
		// node is pointing to the 'init' node, so we get
		// its first child which holds the name of the init rule
		String initRuleName = initNode.getFirst().getToken();
		
		// fetching the rule with the given name from the state
		RuleElement initRule = ruleValue(initRuleName);
		if (initRule == null) {
			logger.error("Init rule does not exists.");
			capi.error("Init rule '" + initRuleName + "' does not exists.", initNode, this);
			return;
		} else
			if (initRule.getParam().size() > 0) {
				logger.error("Init rule cannot have parameters.");
				capi.error("Init rule '" + initRuleName + "' should not have parameters.", initNode, this);
				return;
			}
		
		// creating the first agent to run the initial step
		Element initAgent = new InitAgent();
        capi.getScheduler().setInitAgent(initAgent);
		Location l = new Location(AbstractStorage.PROGRAM_FUNCTION_NAME, ElementList.create(initAgent));
		try {
			// assigning the init rule as the program of the agent
			storage.setValue(l, ruleValue(initRuleName));
		} catch (InvalidLocationException e) {
			e.printStackTrace();
		}
		
		// adding the agent to the univers of agents
		l = new Location(AbstractStorage.AGENTS_UNIVERSE_NAME, ElementList.create(initAgent));
		try {
			storage.setValue(l, BooleanElement.TRUE);
		} catch (InvalidLocationException e) {
			e.printStackTrace();
		}
        
		// in the next step (first step of the program) the init rule will be called		
	}

	/**
	 * @see Interpreter#initProgramExecution()
	 */
	public void initProgramExecution() {
		// clearing the program tree is not needed in the
		// concurrent version of the Engine
		// clearTree(pos);
		// removing environment (temporary) values
		envMap.clear();
		notifyInitProgramExecution(self, (RuleElement)storage.getChosenProgram(self));
 	}

	/**
	 * Notifies the listeners of an initialization of program execution.
	 * 
	 * @param agent the agent running the program
	 * @param program the program that is being initialized
	 */
	private void notifyInitProgramExecution(Element agent, RuleElement program) {
		for (InterpreterListener listener : capi.getInterpreterListeners())
			listener.initProgramExecution(agent, program);
	}

    public synchronized void interpret(ASTNode node, Element agent) throws InterpreterException {
    	ASTNode oldPos = pos;
    	pos = node;
    	Element oldSelf = self;
    	self = agent;
    	
    	// from now on, pos points to the new tree
        Node parent = pos.getParent();
        pos.setParent(null);

        try {
	        while (!isExecutionComplete() && !capi.hasErrorOccurred()) {
	            executeTree();
	        }
        } finally {
	        // set back the parent
	        pos.setParent(parent);
	        
	        // set back the pos
	        pos = oldPos;
	        self = oldSelf;
        }
    }

	@SuppressWarnings("unchecked")
	public synchronized Stack<CallStackElement> getCurrentCallStack() {
		return (Stack<CallStackElement>)ruleCallStack.clone();
	}

	public void cleanUp() {
		interpreters.set(this);
		workCopy.clear();
		envMap.clear();
		ruleCallStack.clear();
	}

}
