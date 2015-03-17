package org.coreasm.compiler.mainprogram.statemachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.coreasm.compiler.classlibrary.EnumFile;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.ElementAlreadyExistsException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.InvalidCodeException;
import org.coreasm.compiler.exception.InvalidStateMachineException;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.mainprogram.statemachine.EngineState;
import org.coreasm.compiler.mainprogram.statemachine.EngineTransition;

/**
 * Represents the core state machine of the main class.
 * The main class is executed via state transitions, similar to the
 * one in the CoreASM interpreter.
 * <p>
 * The state machine consists of states and transitions codes.
 * Transition codes differ in their trigger condition:
 * <ul>
 * <li>On all transitions
 * <li>Upon entering a specific state
 * <li>Upon leaving a specific state
 * <li>Upon leaving a specific state to enter another specific state
 * </ul>
 * Transition code can render other transition code useless if used
 * without care.
 * <p>
 * Furthermore, each state can hold code of its own.
 * <p>
 * To use the State Machine, first fill its transitions and then
 * create the actual state code, using the makeTransition function as
 * a helper.
 * 
 * @author Markus Brenner
 *
 */
public class StateMachine {
	private ArrayList<EngineState> states;
	private HashMap<String, HashMap<String, ArrayList<EngineTransition>>> transitions;
	private HashMap<String, ArrayList<EngineTransition>> onEnter;
	private HashMap<String, ArrayList<EngineTransition>> onLeave;
	private ArrayList<EngineTransition> general;
	private CompilerEngine engine;
	
	/**
	 * Builds an empty state machine
	 * @param engine The compiler engine supervising the compilation process
	 */
	public StateMachine(CompilerEngine engine){
		this.engine = engine;
		states = new ArrayList<org.coreasm.compiler.mainprogram.statemachine.EngineState>();
		transitions = new HashMap<String, HashMap<String, ArrayList<org.coreasm.compiler.mainprogram.statemachine.EngineTransition>>>();
		onEnter = new HashMap<String, ArrayList<org.coreasm.compiler.mainprogram.statemachine.EngineTransition>>();
		onLeave = new HashMap<String, ArrayList<org.coreasm.compiler.mainprogram.statemachine.EngineTransition>>();
		general = new ArrayList<org.coreasm.compiler.mainprogram.statemachine.EngineTransition>();
	}
	
	/**
	 * Adds a new state to the state machine
	 * @param es The new state of the machine
	 * @return True, if the state was added successfully
	 */
	public boolean addState(EngineState es){
		if(states.contains(es)){
			return false;
		}
		states.add(es);
		return true;
	}
	
	/**
	 * Generates the code for a transition from start to end.
	 * The code will be added in the following order:
	 * <ul>
	 * <li>All transition code from a start to an end state
	 * <li>All transition code without a trigger specified
	 * <li>All transition code to the end state
	 * <li>All transition code from the start state 
	 * </ul>
	 * And will then be sorted by priority (lowest to highest). The actual transition
	 * (changing the current state variable) will happen afterwards.
	 * @param start The start state
	 * @param end The end state
	 * @return A CodeFragment holding the code for the transition
	 */
	public CodeFragment makeTransit(String start, String end){
		//collect all transitions
		ArrayList<EngineTransition> finalTransitions = new ArrayList<EngineTransition>();
		
		//transitions from start to end
		HashMap<String, ArrayList<EngineTransition>> tmp = transitions.get(start);
		CodeFragment result = new CodeFragment();
		if(tmp != null){
			ArrayList<EngineTransition> t = tmp.get(end);
			if(t != null){
				for(EngineTransition et : t){
					finalTransitions.add(et);
				}
				
			}
		}
		//general transitions
		for(EngineTransition cf : general){
			result.appendFragment(cf.getCode());
		}
		//transitions to end
		ArrayList<EngineTransition> list = onEnter.get(end);
		if(list != null){
			for(EngineTransition et : list){
				finalTransitions.add(et);
			}
		}
		//transitions from start
		list = onLeave.get(end);
		if(list != null){
			for(EngineTransition et : list){
				finalTransitions.add(et);
			}
		}
		
		//sort by priority
		Collections.sort(finalTransitions);
		
		//append code
		for(int i = finalTransitions.size() - 1; i >= 0; i--){
			result.appendFragment(finalTransitions.get(i).getCode());
		}
		
		if(engine.getOptions().logStateTransition) 
			result.appendLine("System.out.println(\"Transit from " + start + " to " + end + "\");\n");
		
		result.appendFragment(new CodeFragment("\t\t\t\t//Transit from " + start + " to " + end + "\n\t\t\t\tengineState = @RuntimePkg@.EngineState." + end + ";\n\t\t\t\tcontinue;\n"));
		return result;
	}
	
	/**
	 * Adds a transition to the state machine.
	 * All information is extracted from the transition.
	 * @param et The new transition
	 */
	public void addTransition(EngineTransition et){
		if(et.getStart() == null){
			if(et.getEnd() == null){
				general.add(et);
			}
			else{
				ArrayList<EngineTransition> tmp = onEnter.get(et.getEnd());
				if(tmp == null){
					tmp = new ArrayList<org.coreasm.compiler.mainprogram.statemachine.EngineTransition>();
					onEnter.put(et.getEnd(), tmp);
				}
				tmp.add(et);
			}
		}
		else if(et.getEnd() == null){
			ArrayList<EngineTransition> tmp = onLeave.get(et.getStart());
			if(tmp == null){
				tmp = new ArrayList<org.coreasm.compiler.mainprogram.statemachine.EngineTransition>();
				onLeave.put(et.getStart(), tmp);
			}
			tmp.add(et);
		}
		else{		
			HashMap<String, ArrayList<EngineTransition>> tmp = transitions.get(et.getStart());
			if(tmp == null){
				tmp = new HashMap<String, ArrayList<EngineTransition>>();
				transitions.put(et.getStart(), tmp);
			}
			ArrayList<EngineTransition> t = tmp.get(et.getEnd());
			if(t == null){
				t = new ArrayList<EngineTransition>();
				tmp.put(et.getEnd(), t);
			}
			t.add(et);
		}
	}
	
	/**
	 * Generates the classes and code required by the state machine.
	 * This will drop an enum for the states into the temporary directory
	 * and create the complete code for the state machine.
	 * @return The code for the state machine
	 * @throws InvalidCodeException If there was any invalid code in a transition or state
	 * @throws InvalidStateMachineException If the state machine was incomplete or the enum could not be created
	 */
	@SuppressWarnings("resource")
	public CodeFragment generateClasses() throws InvalidCodeException, InvalidStateMachineException{
		if(states.size() == 0){
			throw new InvalidStateMachineException();
		}
		EnumFile se = new EnumFile("EngineState", LibraryEntryType.RUNTIME, "Kernel", engine);
		CodeFragment mainBody = new CodeFragment("\t\t@RuntimePkg@.EngineState engineState = @RuntimePkg@.EngineState." + states.get(0).getName() + ";\n\t\twhile(isRunning){\n\t\t\t");
		
		for(int i = 0; i < states.size(); i++){
			try {
				se.addElement(states.get(i).getName());
			} catch (ElementAlreadyExistsException e) {
				throw new InvalidStateMachineException(e);
			}
				
			mainBody.appendLine("if(engineState == @RuntimePkg@.EngineState." + states.get(i).getName() + "){\n");
			mainBody.appendFragment(states.get(i).getCode());
			mainBody.appendLine("\n\t\t\t}\n");
			
			if(i != states.size() - 1){
				mainBody.appendLine("\t\t\telse ");
			}
		}		
		
		mainBody.appendLine("\t\t}\n");
		
		try {
			engine.getClassLibrary().addEntry(se);
		} catch (EntryAlreadyExistsException e) {
			throw new InvalidStateMachineException(e);
		}
		
		return mainBody;
	}
}
