package org.coreasm.compiler.components.mainprogram.statemachine;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.components.mainprogram.statemachine.EngineTransition;

/**
 * An engine transition.
 * Can be a general transition, a onEnter, onLeave or onEnterLeave transition.
 * Holds code and a priority.
 * @author Markus Brenner
 *
 */
public class EngineTransition implements Comparable<EngineTransition>{
	private CodeFragment code;
	private String start;
	private String end;
	private int priority;
	
	/**
	 * Provides access to the start state of the transition
	 * @return The start state of the transition
	 */
	public String getStart(){
		return start;
	}
	
	/**
	 * Provides access to the end state of the transition
	 * @return The end state of the transition
	 */
	public String getEnd(){
		return end;
	}

	/**
	 * Builds a new EngineTransition.
	 * Leaving start or end empty will set that state requirement
	 * to unused.
	 * @param c The code for the transition
	 * @param start The start state or null, if it is not to be checked
	 * @param end The end state or null, if it is not to be checked
	 */
	public EngineTransition(CodeFragment c, String start, String end){
		this.start = start;
		this.end = end;
		this.code = c;
		this.priority = 50;
	}
	
	/**
	 * Builds a new EngineTransition.
	 * Leaving start or end empty will set that state requirement
	 * to unused.
	 * @param c The code for the transition
	 * @param start The start state or null, if it is not to be checked
	 * @param end The end state or null, if it is not to be checked
	 * @param priority The priority of the transition
	 */
	public EngineTransition(CodeFragment c, String start, String end, int priority){
		this.start = start;
		this.end = end;
		this.code = c;
		this.priority = priority;
	}
	

	/**
	 * Provides access to the code of the transition
	 * @return The CodeFragment of the transition
	 */
	public CodeFragment getCode() {
		return code;
	}
	@Override
	public int compareTo(EngineTransition o) {
		return this.priority - o.priority;
	}
	
	
	
}
