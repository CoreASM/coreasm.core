package CompilerRuntime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalStack {	
	private List<HashMap<String, Object>> variables;
	
	public LocalStack(){
		this.variables = new ArrayList<HashMap<String,Object>>();
		variables.add(new HashMap<String, Object>()); //add a base layer to prevent errors
	}
	
	/**
	 * adds a new layer to the stack.
	 * new variables will be added to this layer
	 */
	public void pushLayer(){
		this.variables.add(new HashMap<String, Object>());
	}
	
	/**
	 * Adds a new variable to the current layer.
	 * Using an already existing name will update the variable
	 * @param s The name of the variable
	 * @param o The value of the variable
	 */
	public void put(String s, Object o){
		HashMap<String, Object> top = variables.get(variables.size() - 1);
		top.put(s, o);
	}
	
	/**
	 * Querys the stack for the given element.
	 * @param s The name of the variable
	 * @return null or the value of the variable
	 */
	public Object get(String s){
		for(int i = variables.size() - 1; i >= 0; i--){
			HashMap<String, Object> current = variables.get(i);
			Object val = current.get(s);
			if(val != null) return val;
		}
		return null;
	}
	
	/**
	 * Pops the current layer with all its variables
	 * from the stack, effectively removing them from the local
	 * environment
	 */
	public void popLayer(){
		if(this.variables.size() >= 1){
			this.variables.remove(this.variables.size() - 1);
		}
	}
}
