package org.coreasm.compiler.variablemanager;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;

import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.EmptyContextStackException;

import org.coreasm.compiler.variablemanager.CompilerVariable;

/**
 * Manages the creation of variable names for code.
 * The variable manager can be polled to create variables for
 * a given type, ensuring that there will not be a variable
 * with the same name and type in the current context.
 * Direct use is not recommended, {@link CodeFragment} encapsulates
 * variable creation and management for easier use.
 * @author Markus Brenner
 *
 */
public class VarManager {
	private HashMap<String, Integer> variables;
	private HashMap<String, Stack<Integer>> contexts;
	
	private int contextCount;
	
	/**
	 * Creates a new variable manager and initializes its data structures.
	 */
	public VarManager(){
		variables = new HashMap<String, Integer>();
		contexts = new HashMap<String, Stack<Integer>>();
		contextCount = 0;
	}
	
	/**
	 * Signals the variable manager to start a new context.
	 * Variables in this context can have the same name as variables in
	 * previous contexts, as they won't collide with them.
	 */
	public void startContext(){
		contextCount++;
		for(Iterator<Entry<String, Stack<Integer>>> it = contexts.entrySet().iterator(); it.hasNext(); ){
			Entry<String, Stack<Integer>> entry = it.next();
			entry.getValue().push(variables.get(entry.getKey()));		
		}
		
	}
	
	/**
	 * Signals the variable manager to end the current context.
	 * Ending a context will throw away all variable assignments in the current context
	 * and returning to the previous name scheme.
	 * @throws EmptyContextStackException If the context stack is empty
	 */
	public void endContext() throws EmptyContextStackException{
		try{
			for(Iterator<Entry<String, Stack<Integer>>> it = contexts.entrySet().iterator(); it.hasNext(); ){
				Entry<String, Stack<Integer>> entry = it.next();
				Integer c = entry.getValue().pop();
				variables.put(entry.getKey(), c);		
			}
			contextCount--;
		}
		catch(EmptyStackException ese){
			throw new EmptyContextStackException("context stack is empty");
		}
	}
	
	/**
	 * Creates a new CompilerVariable with a name and type combination unique to the current context
	 * @param type The type of the variable
	 * @return A CompilerVariable unique to the current context
	 */
	public CompilerVariable createVariable(String type){		
		if(contexts.get(type) == null){
			Stack<Integer> stack = new Stack<Integer>();
			contexts.put(type, stack);
			for(int i = 0; i < contextCount; i++) stack.push(0);
		}
		
		if(variables.get(type) == null) variables.put(type, 0);
		String varname = "var_" + type.replace(".", "_").replace("<", "__").replace(">", "__").replace("?", "___").replace(" ", "").replace(",", "___").replace("[", "____").replace("]", "____") + "_" + variables.get(type);
		variables.put(type, variables.get(type) + 1);
		return new CompilerVariable(type, varname);
	}
}
