package org.coreasm.compiler.mainprogram.statemachine;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.mainprogram.statemachine.EngineState;

/**
 * A state of the state machine.
 * @author Markus Brenner
 *
 */
public class EngineState {
	private String name;
	private CodeFragment code;
	private CompilerEngine engine;
	
	/**
	 * Builds a new engine state with the given name.
	 * @param name The name of the engine state
	 */
	public EngineState(String name, CompilerEngine engine){
		this.name = name;
		code = new CodeFragment("");
		this.engine = engine;
	}
	
	/**
	 * Appends a piece of code to the state.
	 * @param cf A CodeFragment with the code to be appended.
	 */
	public void appendCode(CodeFragment cf){
		if(cf != null) this.code.appendFragment(cf);
		else
			engine.addWarning("attempted to add a 'null' CodeFragment to state " + this.name);
	}
	
	/**
	 * Appends a piece of code to the state.
	 * @param s A code string
	 */
	public void appendCode(String s){
		CodeFragment cf = new CodeFragment(s);
		this.code.appendFragment(cf);
	}
	
	/**
	 * Provides access to the code of the state
	 * @return The code of the state
	 */
	public CodeFragment getCode(){
		return this.code;
	}
	
	/**
	 * Provides access to the name of the state
	 * @return The name of the state
	 */
	public String getName(){
		return this.name;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof EngineState){
			EngineState oth = (EngineState) o;
			if(oth.name.equals(this.name)) return true; 
		}
		return false;
	}
}
