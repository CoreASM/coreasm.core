package org.coreasm.compiler.components.variablemanager;

import org.coreasm.compiler.codefragment.CodeFragment;

/**
 * Represents a variable to be used in generated code.
 * It encapsulates a type and a variable name.
 * Practical use is discouraged, {@link CodeFragment} encapsulates
 * temporary variable declaration way better
 * @author Markus Brenner
 *
 */
public class CompilerVariable {
	private String type;
	private String name;
	
	/**
	 * Creates a new variable with the given type and name
	 * @param type The type of the variable
	 * @param name The name of the variable
	 */
	public CompilerVariable(String type, String name) {
		this.type = type;
		this.name = name;
	}
	
	/**
	 * Produces a string for the declaration of this variable.
	 * @return A string declaring the variable in java syntax,
	 * omitting the final ;
	 */
	public String declare(){
		return type + " " + name;
	}

	/**
	 * Produces a string declaring this variable and assigning it the given value.
	 * @param value A string representing the value for initialization
	 * @return A string declaring the variable and initializing it, including the final ;
	 */
	public String declare(String value){
		return type + " " + name + " = " + value + ";";
	}
	
	@Override
	public String toString(){
		return this.name;
	}
}
