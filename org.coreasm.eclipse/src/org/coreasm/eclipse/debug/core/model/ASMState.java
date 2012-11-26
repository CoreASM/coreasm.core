package org.coreasm.eclipse.debug.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.State;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter.CallStackElement;
import org.coreasm.engine.plugins.set.SetElement;
import org.coreasm.engine.plugins.set.SetPlugin;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;
import org.coreasm.engine.plugins.turboasm.SeqRuleNode;

/**
 * Wrapper class for ASM states. It is needed for the history functionality.
 * @author Michael Stegmaier
 *
 */
public class ASMState {
	private ASTNode pos;
	private int step;
	private Set<Element> lastSelectedAgents = new HashSet<Element>();
	private Map<String, ASMFunctionElement> functions = new HashMap<String, ASMFunctionElement>();
	private Map<String, ASMFunctionElement> universes = new HashMap<String, ASMFunctionElement>();
	private Map<String, Stack<Element>> envMap;
	private Set<Update> updates;
	private Set<Element> agents = new HashSet<Element>();
	private Stack<CallStackElement> callStack = new Stack<CallStackElement>();
	private String sourceName;
	private int lineNumber;
	
	public ASMState(ASMState state) {
		this.step = state.step;
		this.lastSelectedAgents.addAll(state.lastSelectedAgents);
		for (Entry<String, ASMFunctionElement> entry : state.functions.entrySet())
			functions.put(entry.getKey(), new ASMFunctionElement(entry.getKey(), entry.getValue()));
		for (Entry<String, ASMFunctionElement> entry : state.getUniverses().entrySet())
			universes.put(entry.getKey(), new ASMFunctionElement(entry.getKey(), entry.getValue()));
		this.envMap = state.envMap;
		this.updates = state.updates;
		this.agents.addAll(state.agents);
		this.callStack.addAll(state.callStack);
		this.sourceName = state.sourceName;
		this.lineNumber = state.lineNumber;
	}
	
	public ASMState(int step, Set<? extends Element> lastSelectedAgents, State state, Map<String, Stack<Element>> envMap, Set<Update> updates, Set<? extends Element> agents, Stack<CallStackElement> callStack, String sourceName, int lineNumber) {
		this.step = step;
		this.lastSelectedAgents.addAll(lastSelectedAgents);
		for (Entry<String, FunctionElement> entry : state.getFunctions().entrySet())
			functions.put(entry.getKey(), new ASMFunctionElement(entry.getKey(), entry.getValue()));
		for (Entry<String, AbstractUniverse> entry : state.getUniverses().entrySet())
			universes.put(entry.getKey(), new ASMFunctionElement(entry.getKey(), entry.getValue()));
		this.envMap = envMap;
		this.updates = updates;
		this.agents.addAll(agents);
		this.callStack.addAll(callStack);
		this.sourceName = sourceName;
		this.lineNumber = lineNumber;
	}
	
	public void updateState(ASTNode pos, Set<? extends Element> lastSelectedAgents, Map<String, Stack<Element>> envMap, Set<Update> updates, Stack<CallStackElement> callStack, String sourceName, int lineNumber) {
		this.pos = pos;
		this.lastSelectedAgents = new HashSet<Element>();
		this.lastSelectedAgents.addAll(lastSelectedAgents);
		this.envMap = envMap;
		this.updates = updates;
		this.callStack = callStack;
		this.sourceName = sourceName;
		this.lineNumber = lineNumber;
		
		for (Update update : updates) {
			ASMFunctionElement function = getFunction(update.loc.name);
			if (function == null) {
				if (pos != null && Update.UPDATE_ACTION.equals(update.action) && (pos.getParent() instanceof SeqRuleNode || pos.getParent() instanceof ReturnRuleNode)) {
					Stack<Element> stack = envMap.get(update.loc.name);
					if (stack == null) {
						stack = new Stack<Element>();
						envMap.put(update.loc.toString(), stack);
					}
					stack.push(update.value);
				}
			}
			else if (SetPlugin.SETADD_ACTION.equals(update.action)) {
				HashSet<Element> resultantSet = new HashSet<Element>();
				Enumerable existingSet = (Enumerable)function.getValue(update.loc.args);
				if (existingSet != null) {
					for (Element e : existingSet.enumerate()) {
						if (!updates.contains(new Update(update.loc, e, SetPlugin.SETREMOVE_ACTION, (Element)null, null)))
							resultantSet.add(e);
					}
				}
				resultantSet.add(update.value);
				function.setValue(update.loc.args, new SetElement(resultantSet));
			}
			else if (SetPlugin.SETREMOVE_ACTION.equals(update.action)) {
				HashSet<Element> resultantSet = new HashSet<Element>();
				Enumerable existingSet = (Enumerable)function.getValue(update.loc.args);
				if (existingSet != null)
					resultantSet.addAll(existingSet.enumerate());
				resultantSet.remove(update.value);
				function.setValue(update.loc.args, new SetElement(resultantSet));
			}
			else
				function.setValue(update.loc.args, update.value);
		}
	}
	
	public int getStep() {
		return step;
	}
	
	public ASTNode getPosition() {
		return pos;
	}
	
	public Set<Element> getLastSelectedAgents() {
		return lastSelectedAgents;
	}
	
	public ASMFunctionElement getFunction(String name) {
		ASMFunctionElement function = functions.get(name);
		if (function == null)
			function = universes.get(name);
		return function;
	}
	
	public Element getValue(Location loc) {
		Element value = null;
		
		value = getFunction(loc.name).getValue(loc.args);
		if (value == null) {
			FunctionElement functionElement = getFunction(loc.name).getFunctionElement();
			if (functionElement.isReadable())
				value = functionElement.getValue(loc.args);
		}
		if (value == null)
			value = Element.UNDEF;
		
		return value;
	}
	
	public Map<String, ASMFunctionElement> getFunctions() {
		return functions;
	}
	
	public Map<String, ASMFunctionElement> getUniverses() {
		return universes;
	}
	
	public Map<String, Stack<Element>> getEnvMap() {
		return envMap;
	}
	
	public Set<Update> getUpdates() {
		return updates;
	}
	
	public Set<Element> getAgents() {
		return agents;
	}
	
	public Stack<CallStackElement> getCallStack() {
		return callStack;
	}

	public String getSourceName() {
		return sourceName;
	}

	public int getLineNumber() {
		return lineNumber;
	}
}
