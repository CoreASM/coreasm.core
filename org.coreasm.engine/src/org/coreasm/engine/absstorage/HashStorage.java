/*	
 * HashStorage.java  	$Revision: 243 $
 * 
 * Copyright (C) 2005-2007 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.absstorage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.EngineTools;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *	This is an implementation of the <code>AbstractStorage</code> interface that
 *  uses a <code>HashState</code>.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class HashStorage implements AbstractStorage {

	private static long lastStateId = 1000000;

	private static final Logger logger = LoggerFactory.getLogger(HashStorage.class);
	
	
	/** The state of the simulated machine. */
	private State state = null;
	// !!! IMPORTANT !!!
	// This state object (defined above) is used to implement the State 
	// interface. Developers SHOULD NOT refer to this object anywhere 
	// in this class (except in clearState and the State interface methods) 
	// and instead should call AbstractStorage methods; 
	// e.g., using this.setContent(...) instead of state.setContent(...).

	/** Link to the ControlAPI module. */
	private final ControlAPI capi;

	/** Stack of update sets 
	 * 
	 * We keep updates as map of locations to values (per interpreter thread) 
	 * to increase performance.
	 */
	private final ThreadLocal<Stack<Map<Location,Element>>> updateStack;
	/**
	 * Stack of the names of the plugins that pushed states
	 * 
	 * We want to make sure that only the plugin that has pushed the state can pop it.
	 */
	private final ThreadLocal<Stack<String>> updateStackPluginNames;
	
	/** Cache for monitored function values */
	private final ConcurrentMap<Location,Element> monitoredCache;
	
	/** keeps the last inconsistent updates */
	private Set<Update> lastInconsistentUpdates; 
	
	/** a cache of aggregator plugins */
	private Set<Aggregator> aggregatorPlugins = new HashSet<Aggregator>(); 
	
	/** Creates a new <code>HashStorage</code>. */
	public HashStorage(ControlAPI capi) {
		this.capi = capi;
		updateStack = new ThreadLocal<Stack<Map<Location,Element>>>() {
	         protected Stack<Map<Location,Element>> initialValue() {
	             return new Stack<Map<Location,Element>>();
	         }
		};
		updateStackPluginNames = new ThreadLocal<Stack<String>>() {
			@Override
			protected Stack<String> initialValue() {
				return new Stack<String>();
			}
		};
		monitoredCache = new ConcurrentHashMap<Location, Element>();
		lastInconsistentUpdates = null;
		
		// the following line is commented out by Roozbeh Farahbod on 03-Oct-2006
		// the idea is not to duplicate state initialization which is done 
		// by initAbstractStorage()
		// clearState();
		
		// instead of that, we have
		state = new HashState();
	}
	
	/**
	 * Returns the currently stacked updates.
	 * @return the currently stacked updates
	 */
	public Map<Location, Element> getStackedUpdates() {
		Stack<Map<Location, Element>> updateStack = getUpdateStack();
		if (updateStack.isEmpty())
			return Collections.emptyMap();
		
		Map<Location, Element> stackedUpdates = new HashMap<Location,Element>();
		for (Map<Location, Element> stackedUpdate : updateStack)
			stackedUpdates.putAll(stackedUpdate);
		
		return stackedUpdates;
	}
	
	private Stack<Map<Location, Element>> getUpdateStack() {
		return updateStack.get();
	}
	
	private Stack<String> getUpdateStackPluginNames() {
		return updateStackPluginNames.get();
	}
	
	private boolean isStateStacked() {
		return !getUpdateStack().isEmpty();
	}
	
	public void initAbstractStorage() {
		clearState();
		
		initAggregatorPluginCache();
		
        capi.getScheduler().setStepCount(0);
		try {
			// first load the kernel plugin
			Plugin kernel = capi.getPlugin(Kernel.PLUGIN_NAME);
			loadVocabularyExtender((VocabularyExtender)kernel);
			
			// add all functions, universes, and backgronds from plugins that are vocabulary extenders
			for (Plugin p : capi.getPlugins()) {
				if (p != kernel && p instanceof VocabularyExtender)
					loadVocabularyExtender((VocabularyExtender)p);
			}
			
		} catch (NameConflictException e) {
			throw new CoreASMError(e.getMessage());
		}
	}
	
	protected void initAggregatorPluginCache() {
		for (Plugin p: capi.getPlugins()) 
			if (p instanceof Aggregator)
				aggregatorPlugins.add((Aggregator)p);
	}

	private void loadVocabularyExtender(VocabularyExtender ve) throws NameConflictException {
		// add provided functions to the state
		Map<String,FunctionElement> veFunctions = ve.getFunctions();
		if (veFunctions != null)
			for (String name: veFunctions.keySet())
				addFunction(name, veFunctions.get(name));
		
		// add provided universes to the state
		Map<String,UniverseElement> veUniverses = ve.getUniverses();
		if (veUniverses != null)
			for (String name: veUniverses.keySet()) {
				UniverseElement u = veUniverses.get(name);
				
				// treat 'Agents' in a special manner
				if (name.equals(AbstractStorage.AGENTS_UNIVERSE_NAME)) {
					AbstractUniverse agents = this.getUniverse(AbstractStorage.AGENTS_UNIVERSE_NAME);
					if (agents != null) {
						// add all the agents that the plugin provides, 
						// to the 'Agents' universe
						for (Location l: u.getLocations(name))
							try {
								agents.setValue(l.args, u.getValue(l.args));
							} catch (UnmodifiableFunctionException e) {
								// this should not happen
								String msg = "Agents universe appears to be not modifiable!";
								logger.error(msg);
								throw new EngineError(msg);
							}
					} else
						addUniverse(name, veUniverses.get(name));
				} else
					// otherwise, just add the universe
					addUniverse(name, veUniverses.get(name));
			}
		
		// add provided backgrounds to the state
		Map<String,BackgroundElement> veBackgrounds = ve.getBackgrounds();
		if (veBackgrounds != null)
			for (String name: veBackgrounds.keySet())
				addUniverse(name, veBackgrounds.get(name));
		
		// add provided rules to the state
		Map<String,RuleElement> veRules = ve.getRules();
		if (veRules != null)
			for (String name: veRules.keySet()) {
				addRule(name, veRules.get(name));
			}
	}
	
	public synchronized void fireUpdateSet(Set<Update> updateSet) throws InvalidLocationException {
		// Cannot fire updates while state stack is not empty.
		// Doing this check will allow us to bypass calling setValue(...)
		if (isStateStacked()) 
			throw new EngineError("Cannot fire updates when the state stack is not empty.");

		//TODO this should be done in a transactional fashion
		for (Update u: updateSet) {
			state.setValue(u.loc, u.value);
		}
		monitoredCache.clear();
	}

	public Element getChosenProgram(Element agent) {
		Element p = null;
		Element s = null;
		
		try {
			p = getValue(new Location(PROGRAM_FUNCTION_NAME, ElementList.create(agent)));
			if (p instanceof RuleElement)
				return p;
			else {
				capi.error("Value of program(" + s + ") is not a Rule element.");
				return Element.UNDEF;
			}
		} catch (InvalidLocationException e) {
			String msg = "Cannot get value of 'self'/'program' in HashStorage.getChosenProgram(...)."; 
			logger.error(msg);
			throw new EngineError(msg);
		}
	}

//	/**
//	 * @see org.coreasm.engine.absstorage.AbstractStorage#getState()
//	 */
//	public State getState() {
//		return this;
//	}
//
//	/** 
//	 * @see org.coreasm.engine.absstorage.AbstractStorage#setState(org.coreasm.engine.absstorage.State)
//	 */
//	public void setState(State newState) {
//		if (!stateStacked) 
//			this.state = newState;
//		else
//			throw new EngineError("Cannot set state when the state stack is not empty.");
//	}
//
	public Element getValue(Location l) throws InvalidLocationException {
		Element e = null;
		FunctionElement f = this.getFunction(l.name);
		
		if (f != null) {
			// Check if a monitored function is being probed
			if (f.getFClass() == FunctionElement.FunctionClass.fcMonitored) {
				// To make keep monitored functions consistent in one state, use caching
				if (monitoredCache.containsKey(l)) 
					return monitoredCache.get(l);
			}
		}

		e = this.getValueOverStack(l);
		
		if (f == null) {
			if (e == null)
				// if there is no such function and no new value for this location
				// is added in the stack (e.g., as part of a sequence) then there 
				// is a problem
				throw new InvalidLocationException("Location " + l + " does not exists.");
		} else {
			if (e == null) 
				e = Element.UNDEF;

			if (f.getFClass() == FunctionElement.FunctionClass.fcMonitored)
				monitoredCache.put(l, e);
		}
			
		return e;
	}

	public synchronized void setValue(Location l, Element v) throws InvalidLocationException {
		if (!isStateStacked()) 
			state.setValue(l, v);
		else
			throw new EngineError("Cannot set state content when the state stack is not empty.");
	}

	/*
	 * Gets the value of a location possibly going through the stack of states.
	 */
	private Element getValueOverStack(Location loc) throws InvalidLocationException {
		Element value = getStackedValue(loc);
		if (value != null)
			return value;
		return state.getValue(loc);
	}
	
	/**
	 * Returns the value from the stack of states. Returns <code>null</code> if the location is not part of the stack.
	 * @param loc
	 * @return the value from the stack of states or <code>null</code> if the location is not part of the stack
	 */
	private Element getStackedValue(Location loc) {
		if (!isStateStacked())
			return null;
		Stack<Map<Location, Element>> updateStack = getUpdateStack();

		// Looking through the stack from the top...
		// This relies on the Vector implementation of the stack
		Map<Location,Element> um = null;
		for (int i=updateStack.size()-1; i >= 0; i--) {
			um = updateStack.get(i);
			// Look in all the update multisets
			Element value = um.get(loc);
			if (value != null)
				return value;
		}
		return null;
	}

	public void aggregateUpdates() {
		logger.debug("Aggregating updates.");
		UpdateMultiset updateInsts = capi.getScheduler().getUpdateInstructions();
		Collection<Update> tempUpdateSet = null;

		tempUpdateSet = performAggregation(updateInsts);
		
		Collection<Update> uSet = capi.getScheduler().getUpdateSet();
		// clear the updateset of the scheduler 
		uSet.clear();
		// replace it with the new update set
		uSet.addAll(tempUpdateSet);
        
        // george on Aug 19, 2006
        updateInsts.clear();
	}

	/**
	 * @see AbstractStorage#compose(UpdateMultiset, UpdateMultiset)
	 */
	public UpdateMultiset compose(UpdateMultiset updateSet1, UpdateMultiset updateSet2) {
		// instantiate engine composition API, and set update multiset
		CompositionAPIImp compAPI = new CompositionAPIImp();
		compAPI.setUpdateInstructions(updateSet1, updateSet2);
		
		for (Aggregator p: aggregatorPlugins) 
//			synchronized (p) {
				((Aggregator)p).compose(compAPI);
//			}
		
//		if (compAPI.isConsistent() == false)
//			throw new EngineError("Aggregation Inconsistent.");
		
		// get resultant updates from agg API
		return compAPI.getComposedUpdates();
	}

	/**
	 * @see AbstractStorage#performAggregation(UpdateMultiset)
	 */
	public Set<Update> performAggregation(UpdateMultiset updateInsts) {
		// instantiate engine aggregation API, and set update multiset
		AggregationAPIImp aggAPI = new AggregationAPIImp();
		aggAPI.setUpdateInstructions(updateInsts);
		
		// for each plugin
		for (Aggregator p: aggregatorPlugins) 
			((Aggregator)p).aggregateUpdates(aggAPI);
		
		if (aggAPI.isConsistent() == false) {
			String msg = "Inconsistent aggregated results.";
			
			if (aggAPI.getFailedInstructions().size() > 0) {
				msg = msg + "\nFailed instructions: " + Tools.getEOL()
						+ EngineTools.getContextInfo("", aggAPI.getFailedInstructions(), capi.getParser(), capi.getSpec());
			}
			if (aggAPI.getUnprocessedInstructions().size() > 0) {
				msg = msg + "\nUnprocessed instructions: " + Tools.getEOL()
						+ EngineTools.getContextInfo("", aggAPI.getUnprocessedInstructions(), capi.getParser(), capi.getSpec());
			}
			throw new EngineError(msg);
		}
		
		// get resultant updates from agg API
		return aggAPI.getResultantUpdates();
	}
	
	public synchronized boolean isConsistent(Collection<Update> updateSet) {
		Collection<Update> uSet = updateSet;
		lastInconsistentUpdates = null;

		// updateMap is a hashmap from locations to updates. 
		// every update in the updateSet is added to the updateMap, 
		// except, the key (aka the location) already exists. 
		// In this case, the pair of the two conflicting updates is added
		// to lastInconsistentUpdates and this method is immediately exited
		// with result false. 
		HashMap<Location, Update> updateMap = new HashMap<Location, Update>();
		for (Update u: uSet) {
			if (updateMap.containsKey(u.loc)) {
				lastInconsistentUpdates = new HashSet<Update>();
				lastInconsistentUpdates.add(u);
				lastInconsistentUpdates.add(updateMap.get(u.loc));
				return false;
			}
			else
				updateMap.put(u.loc, u);
		}
		return true;
	}

	public Element getNewElement() {
		return new Element();
	}

	/*
	 * See TR-2006-09, page 33.
	 *
	public Element getNewElementFrom(AbstractUniverse bkg) {
		if (bkg instanceof BackgroundElement) {
			return ((BackgroundElement)bkg).getNewValue();
		} else 
			if (bkg instanceof UniverseElement) {
				Element a = getNewElement();
				synchronized (this) {((UniverseElement)bkg).member(a, true);}
				return a;
			}
			else {
				return null;
			}
	}
	*/

	@Override
	public void pushState(String pluginName) {
		Map<Location,Element> updates = new HashMap<Location,Element>();
		getUpdateStack().push(updates);
		getUpdateStackPluginNames().push(pluginName);
	}

	@Override
	public void popState(String pluginName) {
		Stack<Map<Location, Element>> updateStack = getUpdateStack();

		if (updateStack.isEmpty())
			throw new CoreASMError("Cannot pop state when the state stack is empty.");
		
		if (pluginName == null)
			throw new NullPointerException("pluginName");
		
		if (!pluginName.equals(getUpdateStackPluginNames().peek()))
			throw new CoreASMError(pluginName + " tried to pop the state which was pushed by " + getUpdateStackPluginNames().peek());
		
		updateStack.pop();
		getUpdateStackPluginNames().pop();
	}

	/**
	 * Applies the updates in the given update set to the current state.
	 * This method should only be called when there is a state in the stack.
	 * 
	 * @param updates the update multiset
	 * @see #pushState()
	 */
	public synchronized void apply(Set<Update> updates) {
		if (isStateStacked()) {
			Stack<Map<Location, Element>> updateStack = getUpdateStack();

			// adding updates to the current update set in the stack
			// this will overwrite updates to the same location
			Map<Location,Element> lastUpdates = updateStack.peek();
			for (Update u: updates)
				lastUpdates.put(u.loc, u.value);
			
		} else
			capi.error("Cannot apply updates when state stack is empty.");
	}

	public Map<String,AbstractUniverse> getUniverses() {
		return state.getUniverses();
	}

	public AbstractUniverse getUniverse(String name) {
		return state.getUniverse(name);
	}
	
	public synchronized void addUniverse(String name, AbstractUniverse universe) throws NameConflictException {
		state.addUniverse(name, universe);
	}

	public Map<String,FunctionElement> getFunctions() {
		return state.getFunctions();
	}

	public FunctionElement getFunction(String name) {
		return state.getFunction(name);
	}

	public synchronized void addFunction(String name, FunctionElement function) throws NameConflictException {
		state.addFunction(name, function);
	}

	public Map<String,RuleElement> getRules() {
		return state.getRules();
	}

	public RuleElement getRule(String name) {
		return state.getRule(name);
	}

	public synchronized void addRule(String name, RuleElement rule) throws NameConflictException {
		state.addRule(name, rule);
	}

	public Set<Location> getLocations() {
		return state.getLocations();
	}

	/**
	 * Return <code>true</code> if the given name is the name
	 * of a function in the state.
	 */
	public boolean isFunctionName(String token) {
		return getFunction(token) != null;
	}

	/**
	 * Return <code>true</code> if the given name is the name
	 * of a function in the state.
	 */
	public boolean isUniverseName(String token) {
		return getUniverse(token) != null;
	}

	/**
	 * Return <code>true<code> if the given name is the name
	 * of a rule in the state.
	 */
	public boolean isRuleName(String token) {
		return getRule(token) != null;
	}

	public synchronized void clearState() {
		state = new HashState();
		/*
		 * The following universe and functions are moved to Kernel
		try {
			state.addFunction(SELF_FUNCTION_NAME, new MapFunction(Element.UNDEF));
			state.addFunction(PROGRAM_FUNCTION_NAME, new MapFunction(Element.UNDEF));
			state.addUniverse(AGENTS_UNIVERSE_NAME, new UniverseElement());
		} catch (NameConflictException e) {
			// This should not happen!
			throw new EngineError(e);
		}
		/**/
	}

    public String getFunctionName(FunctionElement function) {
		return state.getFunctionName(function);
	}

	public String toString() {
		return state.toString();
	}

	public Set<Update> getLastInconsistentUpdate() {
		return lastInconsistentUpdates;
	}

	public FunctionElement getFunctionElementFunction() {
		return state.getFunctionElementFunction();
	}

	public FunctionElement getRuleElementFunction() {
		return state.getRuleElementFunction();
	}

	public FunctionElement getUniverseElementFunction() {
		return state.getUniverseElementFunction();
	}
	
	/**
	 * This class extends the {@link MapFunction} class and 
	 * provides a class of functions to keep named elements
	 * in the state.
	 * 
	 * @author Roozbeh Farahbod, 15-Sep-2006
	 */
	protected class NameTableFunction <E extends Element> extends FunctionElement {
		
		private Map<String,E>table;
		
		public NameTableFunction() {
			table = new HashMap<String,E>();
		}
		
		public void setValue(String name, E value) {
			table.put(name, value);
		}

    	@SuppressWarnings("unchecked")
        public void setValue(List<? extends Element> args, Element value) throws UnmodifiableFunctionException {
            if (args.size() == 1){
                try {
                    setValue(args.get(0).toString(),(E) value);
                }
                catch (ClassCastException e) {
                    capi.error(e);
                }
            }
            else {
                capi.error("NameTableFunctions can have only one argument.");
            }
        }
        
		public E getValue(String name) {
			return table.get(name);
		}
		
		public Collection<E> values() {
			return (Collection<E>)table.values();
		}

		public Collection<String> getNames() {
			return table.keySet();
		}
		
		@Override
		public Element getValue(List<? extends Element> args) {
			if (args.size() == 1)
				return this.getValue(args.get(0).toString());
			else
				return Element.UNDEF;
		}

		public Map<String,E> getTable() {
			return table;
		}
		
		public Map<String,E> getTableClone() {
			Map<String,E> result = new HashMap<String,E>();
			for (Entry<String,E> e: table.entrySet()) {
				result.put(e.getKey(), e.getValue());
			}
			return result;
		}
		
		public boolean containsName(String name) {
			return table.containsKey(name);
		}
        
        public Set<Location> getLocations(String name) {
            Set<Location> locations = new HashSet<Location>();
            
            for (String functionName: table.keySet()) {
                locations.add(new Location(name, ElementList.create(new NameElement(functionName))));
            }
            
            return locations;
        }

	}
	
	/** 
	 *	An implementation of <code>State</code> using <code>HashMap</code>.
	 *   
	 *  @author  Roozbeh Farahbod
	 *  
	 *  @see java.util.HashMap
	 */
	protected class HashState implements State {

		public final long id;
		
		/**
		 * Universes
		 */
		private NameTableFunction<AbstractUniverse> universeElements;
		
		/**
		 * Functions
		 */
		private NameTableFunction<FunctionElement> functionElements;
		
		/**
		 * Rules
		 */
		private NameTableFunction<RuleElement> ruleElements;
		
		/**
		 * Creates a new <code>HashState</code>.
		 */
		public HashState() {
			super();
			lastStateId++;
			id = lastStateId;
			universeElements = new NameTableFunction<AbstractUniverse>();
			functionElements = new NameTableFunction<FunctionElement>();
			ruleElements = new NameTableFunction<RuleElement>();
			functionElements.setValue(UNIVERSE_ELEMENT_FUNCTION_NAME, universeElements);
			functionElements.setValue(RULE_ELEMENT_FUNCTION_NAME, ruleElements);
			functionElements.setValue(FUNCTION_ELEMENT_FUNCTION_NAME, functionElements);
		}

		public Map<String,AbstractUniverse> getUniverses() {
			return universeElements.getTableClone();
		}

		public synchronized void addUniverse(String name, AbstractUniverse universe) throws NameConflictException {
			if (universe == null)
				throw new NullPointerException();
			if (nameExists(name))
				throw new NameConflictException("Identifier \"" + name + "\" is defined more than once.");
			universeElements.setValue(name, universe);
		}

		public Map<String,FunctionElement> getFunctions() {
			return functionElements.getTableClone();
		}

		public synchronized void addFunction(String name, FunctionElement function) throws NameConflictException {
			if (function instanceof AbstractUniverse)
				addUniverse(name, (AbstractUniverse)function);
			if (function == null)
				throw new NullPointerException();
			if (nameExists(name))
				throw new NameConflictException("Identifier \"" + name + "\" is defined more than once.");
			functionElements.setValue(name, function);
		}

		public Map<String,RuleElement> getRules() {
			return ruleElements.getTableClone();
		}

		public synchronized void addRule(String name, RuleElement rule) throws NameConflictException {
			if (rule == null)
				throw new NullPointerException();
			if (nameExists(name))
				throw new NameConflictException("Identifier \"" + name + "\" is defined more than once.");
			ruleElements.setValue(name, rule);
		}

		public Set<Location> getLocations() {
			HashSet<Location> locations = new HashSet<Location>();
			Map<String,FunctionElement> funcs = getFunctions();
			for (Entry<String,FunctionElement> e: funcs.entrySet()) 
				if (e.getValue().isModifiable()) 
					locations.addAll(e.getValue().getLocations(e.getKey()));
			return locations;
		}

		public Element getValue(Location loc) throws InvalidLocationException {
			if (nameExists(loc.name)) {
				Element id;
				try {
					id = getIdentifier(loc.name);
				} catch (IdentifierNotFoundException e) {
					throw new InvalidLocationException(e);
				}
				if (id instanceof FunctionElement) {
					FunctionElement f = (FunctionElement)id;
					if (f.isReadable()) 
						return ((FunctionElement)id).getValue(loc.args);
					else {
						String msg = "Reading from an out-function '" + loc + "' results in an undef value.";
//						CHANGE: Why print the same warning twice?
//						logger.warn(msg);
						capi.warning("Abstract Storage", msg);
						return Element.UNDEF;
					}
				}
				else
					throw new InvalidLocationException(loc + " is not a valid location.");
			}
			else
				return Element.UNDEF;
		}
		
		/**
		 * Sets a new value for a location in the state.
		 * If the location does not exist, it adds a new location using
		 * a {@link MapFunction} instance to the state and then sets its value.
		 * 
		 * @param l location 
		 * @param v value to be set for the given location
		 * 
		 * @throws InvalidLocationException if the location is not modifiable.
		 * @see State#setValue(Location, Element)
		 */
		public synchronized void setValue(Location l, Element v) throws InvalidLocationException {
			if (!nameExists(l.name)) {
		        FunctionElement f = new MapFunction(Element.UNDEF);
	            try {
					addFunction(l.name, f);
				} catch (NameConflictException e) {
		            throw new EngineError("There is a name conflict (in 'handleUndefinedIdentifier(String, ElementList)') for \"" + id + "\"."); 
				} 
			}
			Element id;
			try {
				id = getIdentifier(l.name);
			} catch (IdentifierNotFoundException e) {
				throw new InvalidLocationException(e);
			}
			if (id instanceof FunctionElement) {
				if (((FunctionElement)id).isModifiable()) {
					try {
						((FunctionElement) id).setValue(l.args, v);
					} catch (UnmodifiableFunctionException e) {
						throw new InvalidLocationException(e);
					}
				} else
					throw new InvalidLocationException(l + " is not a modifiable location.");
			} else
				throw new InvalidLocationException(l + " is not a valid location.");
		}

		/*
		public synchronized void setValue(Location l, Element v) throws InvalidLocationException {
			if (nameExists(l.name)) {
				Element id;
				try {
					id = getIdentifier(l.name);
				} catch (IdentifierNotFoundException e) {
					throw new InvalidLocationException(e);
				}
				if (id instanceof FunctionElement && ((FunctionElement)id).isModifiable())
					try {
						((FunctionElement) id).setValue(l.args, v);
					} catch (UnmodifiableFunctionException e) {
						throw new InvalidLocationException(e);
					}
				else
					throw new InvalidLocationException("Not a valid location.");
			} else {
		        FunctionElement f = new MapFunction(Element.UNDEF);
	            addFunction(id, f); 
				throw new InvalidLocationException("There is no such function in the state.");
			}
		}
		*/

		/**
		 * Returns the rule/function/universe in the state
		 * that has the given name.
		 *  
		 * @param name
		 * @return an <code>Element</code>
		 * @throws IdentifierNotFoundException if there 
		 * is no such rule/function/universe in the state
		 */
		public Element getIdentifier(String name)
				throws IdentifierNotFoundException {
			Element id = null;
			id = (Element)universeElements.getValue(name);
			// Fix for consistency issues with UniverseElements in sequential rules
			if (id != null) {
				Element e = getStackedValue(new Location(name, ElementList.NO_ARGUMENT));
				if (e instanceof UniverseElement)
					id = (UniverseElement)e;
			}
			if (id == null) {
				id = functionElements.getValue(name);
				if (id == null) {
					id = ruleElements.getValue(name);
					if (id == null)
						throw new IdentifierNotFoundException();
				}
			}
			return id;
		}

		/*
		 * Returns true if a rule/function/universe with
		 * the given name exists in the state.
		 */
		private boolean nameExists(String name) {
			return universeElements.containsName(name)
					|| functionElements.containsName(name)
					|| ruleElements.containsName(name);
		}

		public FunctionElement getFunction(String name) {
			FunctionElement res = functionElements.getValue(name);
			if (res == null) 
				res = universeElements.getValue(name);
			return res;
		}

		public AbstractUniverse getUniverse(String name) {
			return universeElements.getValue(name);
		}

		public RuleElement getRule(String name) {
			return ruleElements.getValue(name);
		}

		public String toString() {
			StringWriter strWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(strWriter);
			String tempStr = null;
			
//			writer.println("State #" + this.id);
			
			Set<Entry<String,AbstractUniverse>> universeEntries = 
					universeElements.getTable().entrySet();
			
			writer.println("  * Backgrounds:");
			for (Entry<String,AbstractUniverse> e: universeEntries) {
				if (e.getValue() instanceof BackgroundElement) {
					writer.println("    - " + e.getKey());
				}
			}

			writer.println("  * Universes:");
			for (Entry<String,AbstractUniverse> e: universeEntries) {
				if (e.getValue() instanceof UniverseElement) {
					UniverseElement ue = (UniverseElement)e.getValue();
					writer.print("    - " + e.getKey() + ": {");
					StringBuffer str = new StringBuffer();
					for (Location l: ue.getLocations(e.getKey())) {
						if (ue.getValue(l.args).equals(BooleanElement.TRUE)) {
							if (l.args.size() > 0) {
								str.append(l.args.get(0).denotation() + ", ");
							}
						}
					}
					if (str.length() > 2) {
						writer.print(str.substring(0, str.length() - 2));
					}
					writer.println("}");
				}
			}

			writer.println("  * Functions:");
			for (Entry<String,FunctionElement> e: functionElements.getTable().entrySet()) {
				FunctionElement f = e.getValue();
				if (f.isModifiable() && 
						!(f.equals(functionElements) || f.equals(universeElements) || f.equals(ruleElements))) {
					String name = e.getKey();
					writer.println("    - " + name);
					for (Location l: f.getLocations(name)) {
						writer.print("      " + name + "(");
						tempStr = "";
						for (Element arg: l.args)
							tempStr = tempStr + arg.denotation() + ", ";
						if (tempStr.length() > 0)
							tempStr = tempStr.substring(0, tempStr.length() - 2);
						writer.print(tempStr);
						writer.print(") = ");
						writer.println(reformatFunctionValue(f.getValue(l.args).denotation()));
					}
				}
			}
			
			return strWriter.toString();
		}

		/* (non-Javadoc)
         * @see org.coreasm.engine.absstorage.State#getFunctionName(org.coreasm.engine.absstorage.FunctionElement)
         */
        public String getFunctionName(FunctionElement function) {
            for (Entry<String, FunctionElement> f: functionElements.table.entrySet()) {
                if (f.getValue().equals(function))
                	return f.getKey();
            }
            for (Entry<String, AbstractUniverse> u : universeElements.table.entrySet()) {
            	if (u.getValue().equals(function))
            		return u.getKey();
            }
            return null;
        }

		/*
		 * Cut the string to a specific length
		 */
		private String reformatFunctionValue(String value) {
			StringBuffer result = new StringBuffer(value);
			int TRIM = 50;

			if (result.length() > TRIM) {
				result.delete(TRIM - 3, result.length());
				result.append("...");
			}
			
			return result.toString();
		}
		
		public FunctionElement getFunctionElementFunction() {
			return functionElements;
		}

		public FunctionElement getRuleElementFunction() {
			return ruleElements;
		}

		public FunctionElement getUniverseElementFunction() {
			return universeElements;
		}
	}

}
