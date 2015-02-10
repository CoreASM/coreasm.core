/*	
 * SignalsPlugin.java 	
 * 
 * Copyright (c) 2010 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.network.plugins.signals;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementBackgroundElement;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.InvalidLocationException;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.Signature;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;
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
import org.coreasm.engine.plugin.ServiceRequest;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.set.SetBackgroundElement;
import org.coreasm.engine.plugins.set.SetElement;
import org.coreasm.engine.plugins.set.SetPlugin;
import org.coreasm.network.plugins.signals.SignalsPlugin.SignalAttributesFunctionElement.AttType;
import org.coreasm.util.Logger;

/** 
 *	Plugin for Signalling mechanisms between agents
 *   
 *  @author  Roozbeh Farahbod
 *  @version 1.0.0-beta
 */
public class SignalsPlugin extends Plugin implements ParserPlugin,
        InterpreterPlugin, VocabularyExtender {

	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 0, "beta");
	
	public static final String PLUGIN_NAME = SignalsPlugin.class.getSimpleName();

	public static final String SIG_TYPE_FUNC_NAME = "signalType";
	public static final String SIG_SRC_FUNC_NAME = "signalSource";
	public static final String SIG_TRG_FUNC_NAME = "signalTarget";
	public static final String SIG_INBOX_FUNC_NAME = "signalInbox";
	
	protected static final String VARIABLE_NODE_NAME = "variable";
	protected static final String RULE_NODE_NAME = "dorule";

	private final String[] keywords = {"signal", "onsignal", "with", "as", "do", "of"};
	private final String[] operators = {};
	
	private Map<String, FunctionElement> functions = null;
	private Map<String, BackgroundElement> backgrounds = null;
    //private ThreadLocal<Map<Node,List<Element>>> remained;
    private Map<String, GrammarRule> parsers;
    private Set<String> dependencies = null;

    private ThreadLocal<Map<Node, SignalElement>> signals;

    public SignalsPlugin() {
    	dependencies = new HashSet<String>();
    	dependencies.add("SetPlugin");
    }
    
    @Override
    public void initialize() {
        signals = new ThreadLocal<Map<Node, SignalElement>>() {
			@Override
			protected Map<Node, SignalElement> initialValue() {
				return new IdentityHashMap<Node, SignalElement>();
			}
        };
    	// reset functions and as the result reset inbox values
    	if (functions != null) {
    		functions = null;
    		getFunctions();
    	}
    }
    
    private Map<Node, SignalElement> getSignalsMap() {
    	return signals.get();
    }
    

	@Override
	public void setControlAPI(ControlAPI capi) {
		super.setControlAPI(capi);
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
			
			KernelServices kernel = (KernelServices)capi.getPlugin("Kernel").getPluginInterface();
			
			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();
			//Parser<Node> guardParser = kernel.getGuardParser();
			
			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> idParser = pTools.getIdParser();
			
			// SignalRule : 'signal' Term 'with' Term ('as' ID 'do' Rule)?
			Parser<Node> signalRuleParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("signal", PLUGIN_NAME),
					termParser,
					pTools.getKeywParser("with", PLUGIN_NAME),
					termParser,
					pTools.seq(
							pTools.getKeywParser("as", PLUGIN_NAME),
							idParser,
							pTools.getKeywParser("do", PLUGIN_NAME),
							ruleParser).optional(),
					}).map(
					new SignalRuleParseMap());

			parsers.put(signalRuleParser.toString(), 
					new GrammarRule(signalRuleParser.toString(),
							"'signal' Term 'with' Term ('as' ID 'do' Rule)?", signalRuleParser, PLUGIN_NAME));

			// OnSignalRule : 'onsignal' ID 'of' Term 'do' Rule
			Parser<Node> onsignalRuleParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser("onsignal", PLUGIN_NAME),
					idParser,
					pTools.getKeywParser("of", PLUGIN_NAME),
					termParser,
					pTools.getKeywParser("do", PLUGIN_NAME),
					ruleParser
					}).map(
					new OnSignalRuleParseMap());

			parsers.put(onsignalRuleParser.toString(),
					new GrammarRule(onsignalRuleParser.toString(), "'onsignal' ID 'of' Term 'do' Rule",
							onsignalRuleParser, PLUGIN_NAME));

			// Rule : SignalRule | OnSignalRule
			parsers.put("Rule",
					new GrammarRule("SignalRules", "SignalRule | OnSignalRule",
							Parsers.or(signalRuleParser, onsignalRuleParser), PLUGIN_NAME));

		}
		return parsers;
	}

    public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
        if (pos instanceof SignalRuleNode) {
            SignalRuleNode signalNode = (SignalRuleNode) pos;
            
            // 1. evaluate first three parameters
            
            if (!signalNode.getTargetAgent().isEvaluated())
            	return signalNode.getTargetAgent();
            
            if (!signalNode.getType().isEvaluated())
            	return signalNode.getType();
            
            // 2. create a signal, 
            //    only if there is no rule or the rule is not evaluated yet
            Element agent = signalNode.getTargetAgent().getValue();
            
            if (signalNode.getDoRule() == null || !signalNode.getDoRule().isEvaluated()) {
            	SignalElement signal = new SignalElement(signalNode.getType().getValue());
            	Map<Node, SignalElement> signalsMap = getSignalsMap();
            	signalsMap.put(pos, signal);
            	
	            if (agent.equals(Element.UNDEF)) {
	            	String msg = "Cannot send a signal to an undefined agent.";
	            	capi.error(msg, pos, interpreter);
	            	Logger.log(Logger.ERROR, Logger.plugins, msg);
	            	return pos;
	            }
	            signal.src = interpreter.getSelf();
	            signal.target = agent;
            }

            // 3. if there is a rule, run the rule
            if (signalNode.getDoRule() != null) {
            	String varname = signalNode.getVariable().getToken();
            	if (!signalNode.getDoRule().isEvaluated()) {
            		interpreter.addEnv(varname, getSignalsMap().get(pos));
            		return signalNode.getDoRule();
            	}
            }

            // 4. gather the updates 
            UpdateMultiset updates = new UpdateMultiset();
            
            if (signalNode.getDoRule() != null) {
            	String varname = signalNode.getVariable().getToken();
            	interpreter.removeEnv(varname);
            	updates.addAll(signalNode.getDoRule().getUpdates());
            }	
            Location inboxLoc = new Location(SIG_INBOX_FUNC_NAME, new ElementList(agent));
            SignalElement newSignal = getSignalsMap().get(pos);
            updates.add(new Update(inboxLoc, newSignal, SetPlugin.SETADD_ACTION, interpreter.getSelf(), pos.getScannerInfo()));
            getSignalsMap().remove(pos);
            pos.setNode(null, updates, null);
            
            ServiceRequest sr = new ServiceRequest("debuginfo");
            sr.parameters.put("message", "Signal to be sent: " + newSignal);
            sr.parameters.put("channel", "Signals");
            capi.serviceCall(sr, false);
        }
        
        else if (pos instanceof OnSignalRuleNode) {
        	OnSignalRuleNode onsignalNode = (OnSignalRuleNode)pos;
        	
            if (!onsignalNode.getType().isEvaluated())
            	return onsignalNode.getType();
            
            // 1. before evaluating the rule
            
            if (!onsignalNode.getDoRule().isEvaluated()) {
            	Element inbox = null;
            	
            	// 1-1. get signal inbox
            	try {
					inbox = capi.getStorage().getValue(new Location(SIG_INBOX_FUNC_NAME, new ElementList(interpreter.getSelf())));
				} catch (InvalidLocationException e) {
					capi.error(e, pos, interpreter);
					return pos;
				}
				
				// 1-2. look for a matching signal
				if (inbox instanceof Enumerable) {
					Enumerable einbox = (Enumerable)inbox;
					SignalElement matchingSignal = null;
					for (Element s: einbox.enumerate()) 
						if (s instanceof SignalElement) {
							SignalElement se = (SignalElement)s;
							if (se.type.equals(onsignalNode.getType().getValue())) {
								matchingSignal = se;
								break;
							}
						}
					
					// if a matching signal found,
					// set the value of the env variable and evaluate the rule
					if (matchingSignal != null) {
						getSignalsMap().put(pos, matchingSignal);
						interpreter.addEnv(onsignalNode.getVariable().getToken(), matchingSignal);
			            
						ServiceRequest sr = new ServiceRequest("debuginfo");
			            sr.parameters.put("message", "Signal observed: " + matchingSignal + " by " 
			            		+ interpreter.getSelf());
			            sr.parameters.put("channel", "Signals");
			            capi.serviceCall(sr, false);

			            return onsignalNode.getDoRule();
					} else {
						pos.setNode(null, new UpdateMultiset(), null);
						return pos;
					}
				} else {
					String emsg = "The signal inbox of agent " + interpreter.getSelf() + " is invalid.";
					capi.error(emsg, pos, interpreter);
					Logger.log(Logger.ERROR, Logger.plugins, emsg);
					return pos;
				}
            } else {
            	// 2. after evaluating the rule

                UpdateMultiset updates = new UpdateMultiset();

                interpreter.removeEnv(onsignalNode.getVariable().getToken());
            	updates.addAll(onsignalNode.getDoRule().getUpdates());
	            Location inboxLoc = new Location(SIG_INBOX_FUNC_NAME, new ElementList(interpreter.getSelf()));
	            updates.add(new Update(inboxLoc, getSignalsMap().get(pos), SetPlugin.SETREMOVE_ACTION, interpreter.getSelf(), pos.getScannerInfo()));
	            getSignalsMap().remove(pos);
	            pos.setNode(null, updates, null);
            }
        }

        // in case of error
        return pos;
    }

	@Override
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	@Override
	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	@Override
	public Map<String, BackgroundElement> getBackgrounds() {
		if (backgrounds == null) {
			backgrounds = new HashMap<String, BackgroundElement>();
			backgrounds.put(SignalBackgroundElement.SIGNAL_BACKGROUND_NAME, new SignalBackgroundElement());
		}
		return backgrounds;
	}

	@Override
	public Set<String> getFunctionNames() {
		return functions.keySet();
	}

	@Override
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();

			functions.put(SIG_TYPE_FUNC_NAME, new SignalAttributesFunctionElement(AttType.type));
			functions.put(SIG_SRC_FUNC_NAME, new SignalAttributesFunctionElement(AttType.src));
			functions.put(SIG_TRG_FUNC_NAME, new SignalAttributesFunctionElement(AttType.trg));
			
			functions.put(SIG_INBOX_FUNC_NAME, new SignalInboxFunctionElement());
		}
		return functions;
	}

	@Override
	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, RuleElement> getRules() {
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, UniverseElement> getUniverses() {
		return Collections.emptyMap();
	}
	
	@Override
	public Set<String> getDependencyNames() {
		return dependencies;
	}

	/**
	 * Mapping of node elements into the signal rule node.
	 *   
	 * @author Roozbeh Farahbod
	 */
	public static class SignalRuleParseMap extends ParserTools.ArrayParseMap {

	    String nextChildName = "alpha";

	    public SignalRuleParseMap() {
			super(PLUGIN_NAME);
		}
		
		public Node map(Object[] v) {
			nextChildName = "alpha";
			ASTNode node = new SignalRuleNode(((Node)v[0]).getScannerInfo());

			addChildren(node, v);
			return node;
		}
		
		public void addChild(Node parent, Node child) {
			if (child instanceof ASTNode)
				parent.addChild(nextChildName, child);
			else {
				String token = child.getToken();
		        if (token.equals("as"))
		        	nextChildName = SignalsPlugin.VARIABLE_NODE_NAME;
		        else if (token.equals("do"))
		        	nextChildName = SignalsPlugin.RULE_NODE_NAME;
				super.addChild(parent, child);
			}
		}

	}
	
	/**
	 * Mapping of node elements to the onsignal rule node.
	 *   
	 * @author Roozbeh Farahbod
	 */
	public static class OnSignalRuleParseMap extends ParserTools.ArrayParseMap {

	    public OnSignalRuleParseMap() {
			super(PLUGIN_NAME);
		}
		
		public Node map(Object[] v) {
			ASTNode node = new OnSignalRuleNode(((Node)v[0]).getScannerInfo());
			addChildren(node, v);
			return node;
		}
	}
	
	/**
	 * Attribute functions on sinals.
	 */
	public static class SignalAttributesFunctionElement extends FunctionElement {

		protected static enum AttType {type, src, trg};
		private static final Signature signature = 
			new Signature(SignalBackgroundElement.SIGNAL_BACKGROUND_NAME, ElementBackgroundElement.ELEMENT_BACKGROUND_NAME);
		
		protected final AttType ftype;
		
		public SignalAttributesFunctionElement(AttType type) {
			this.ftype = type;
		}
		
		@Override
		public Element getValue(List<? extends Element> args) {
			if (args.size() == 1 && args.get(0) instanceof SignalElement) {
				SignalElement signal = (SignalElement)args.get(0);
				switch (ftype) {
				case type:	
					return signal.type;
				case src:
					return signal.src;
				case trg:
					return signal.target;
				default:
					return Element.UNDEF;
				}
			} else
				return Element.UNDEF;
		}

		@Override
		public Signature getSignature() {
			return signature;
		}

		
		@Override
		public FunctionClass getFClass() {
			if (ftype == AttType.type)
				return FunctionClass.fcDerived;
			else
				return FunctionClass.fcControlled;
		}

		@Override
		public void setValue(List<? extends Element> args, Element value)
				throws UnmodifiableFunctionException {
			if (ftype == AttType.type)
				throw new UnmodifiableFunctionException("Cannot change the type of a signal");
			
			if (args.size() == 1 && args.get(0) instanceof SignalElement) {
				SignalElement signal = (SignalElement)args.get(0);
				switch (ftype) {
				case src:
					signal.src = value;
				case trg:
					signal.target = value;
				default:
					return;
				}
			} 
		}
		
	}

	/**
	 * Inbox functions on sinals.
	 */
	public class SignalInboxFunctionElement extends FunctionElement {

		private final Signature signature = 
			new Signature(ElementBackgroundElement.ELEMENT_BACKGROUND_NAME, SetBackgroundElement.SET_BACKGROUND_NAME);
		
		private HashMap<Element, Element> inboxTable = new HashMap<Element, Element>();
		
		@Override
		public Element getValue(List<? extends Element> args) {
			if (args.size() == 1) {
				Element agent = args.get(0); 
				Element inbox = inboxTable.get(agent);
				if (inbox == null) { 
					inbox = new SetElement();
					inboxTable.put(agent, inbox);
				}
				return inbox;
			} else
				return Element.UNDEF;
		}

		@Override
		public Signature getSignature() {
			return signature;
		}

		@Override
		public void setValue(List<? extends Element> args, Element value)
				throws UnmodifiableFunctionException {
			if (args.size() == 1) {
				Element agent = args.get(0);
				if (!(value instanceof SetElement))
					Logger.log(Logger.WARNING, Logger.plugins, "Signal inbox of agent " + agent + " is updated with a non set element (" + value + ").");
				inboxTable.put(agent, value);
			}
		}
		
	}

}
