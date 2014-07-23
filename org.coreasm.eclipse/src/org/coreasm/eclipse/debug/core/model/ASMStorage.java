package org.coreasm.eclipse.debug.core.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.codehaus.jparsec.Parser;
import org.coreasm.eclipse.debug.ui.views.ASMUpdate;
import org.coreasm.eclipse.engine.debugger.WatchExpressionAPI;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.ElementList;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.FunctionElement.FunctionClass;
import org.coreasm.engine.absstorage.HashStorage;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.MapFunction;
import org.coreasm.engine.absstorage.NameConflictException;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.Interpreter.CallStackElement;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.ParserPlugin;

/**
 * Wrapper class for ASM storages. It is needed for the history functionality.
 * @author Michael Stegmaier
 *
 */
public class ASMStorage extends HashStorage {
	private WatchExpressionAPI wapi;
	private AbstractStorage storage;
	private int step;
	private Set<Element> lastSelectedAgents = new HashSet<Element>();
	private Map<String, Element> envVars;
	private Map<Location, Element> stackedUpdates;
	private Set<ASMUpdate> updates;
	private Set<Element> agents = new HashSet<Element>();
	private Stack<CallStackElement> callStack = new Stack<CallStackElement>();
	private String sourceName;
	private int lineNumber;
	private ASTNode pos;
	
	public ASMStorage(ASMStorage storage) {
		this(storage.wapi, storage.storage, storage.step, storage.lastSelectedAgents, storage.envVars, storage.updates, storage.agents, storage.callStack, storage.sourceName, storage.lineNumber);
	}
	
	public ASMStorage(WatchExpressionAPI wapi, ControlAPI capi) {
		this(wapi, capi.getStorage(), capi.getStepCount(), capi.getLastSelectedAgents(), capi.getInterpreter().getInterpreterInstance().getEnvVars(), ASMUpdate.wrapUpdateSet(capi), capi.getAgentSet(), capi.getInterpreter().getInterpreterInstance().getCurrentCallStack(), null, -1);
	}
	
	public ASMStorage(WatchExpressionAPI wapi, AbstractStorage storage, int step, Set<? extends Element> lastSelectedAgents, Map<String, Element> envVars, Set<ASMUpdate> updates, Set<? extends Element> agents, Stack<CallStackElement> callStack, String sourceName, int lineNumber) {
		super(wapi);
		this.wapi = wapi;
		this.storage = storage;
		this.step = step;
		this.lastSelectedAgents.addAll(lastSelectedAgents);
		for (Entry<String, FunctionElement> entry : storage.getFunctions().entrySet()) {
			try {
				if (entry.getValue() instanceof Enumerable)
					addFunction(entry.getKey(), new ASMEnumerableFunctionElement(entry.getKey(), entry.getValue()));
				else
					addFunction(entry.getKey(), new ASMFunctionElement(entry.getKey(), entry.getValue()));
			} catch (NameConflictException e) {
			}
		}
		for (Entry<String, AbstractUniverse> entry : storage.getUniverses().entrySet()) {
			try {
				if (entry.getValue() instanceof Enumerable)
					addUniverse(entry.getKey(), new ASMEnumerableUniverse(entry.getKey(), entry.getValue()));
				else
					addUniverse(entry.getKey(), new ASMUniverse(entry.getKey(), entry.getValue()));
			} catch (NameConflictException e) {
			}
		}
		this.envVars = envVars;
		this.updates = updates;
		this.agents.addAll(agents);
		for (Element agent : agents) {
			MapFunction f = new MapFunction();
			try {
				f.setValue(ElementList.NO_ARGUMENT, agent);
			} catch (UnmodifiableFunctionException e) {
			}
			f.setFClass(FunctionClass.fcStatic);
			try {
				addFunction(agent.denotation(), new ASMFunctionElement(agent.denotation(), f));
			} catch (NameConflictException e) {
			}
		}
		this.callStack.addAll(callStack);
		this.sourceName = sourceName;
		this.lineNumber = lineNumber;
		initAggregatorPluginCache();
	}
	
	public void updateState(ASTNode pos, Set<? extends Element> lastSelectedAgents, Map<String, Element> envVars, Set<ASMUpdate> updates, Stack<CallStackElement> callStack, String sourceName, int lineNumber) {
		this.pos = pos;
		this.lastSelectedAgents = new HashSet<Element>(lastSelectedAgents);
		this.envVars = envVars;
		this.stackedUpdates = ((HashStorage)storage).getStackedUpdates();
		this.updates = updates;
		this.callStack = callStack;
		this.sourceName = sourceName;
		this.lineNumber = lineNumber;
	}
	
	public Element evaluateExpression(ControlAPI capi, String expression) throws InterpreterException {
		ParserTools parserTools = ParserTools.getInstance(capi);
		Parser<Node> termParser = ((ParserPlugin)capi.getPlugin("Kernel")).getParser("Term");
		Parser<Node> parser = termParser.from(parserTools.getTokenizer(), parserTools.getIgnored());
		Element[] lastSelectedAgents = this.lastSelectedAgents.toArray(new Element[this.lastSelectedAgents.size()]);
		
		return wapi.evaluateExpression((ASTNode)parser.parse(expression), lastSelectedAgents[0], this);
	}
	
	public void applyStackedUpdates() {
		if (getStackedUpdates().isEmpty())
			pushState();
		HashSet<Update> updates = new HashSet<Update>();
		for (Entry<Location, Element> stackedUpdate : stackedUpdates.entrySet())
			updates.add(new Update(stackedUpdate.getKey(), stackedUpdate.getValue(), Update.UPDATE_ACTION, (Element)null, null));
		apply(updates);
	}
	
	public void discardStackedUpdates() {
		if (stackedUpdates != null && !stackedUpdates.isEmpty())
			popState();
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
	
	public Map<String, Element> getEnvVars() {
		return envVars;
	}
	
	public Set<ASMUpdate> getUpdates() {
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
	
	@Override
	public Map<String, RuleElement> getRules() {
		return storage.getRules();
	}

	@Override
	public RuleElement getRule(String name) {
		return storage.getRule(name);
	}

	@Override
	public boolean isRuleName(String token) {
		return storage.isRuleName(token);
	}
}
