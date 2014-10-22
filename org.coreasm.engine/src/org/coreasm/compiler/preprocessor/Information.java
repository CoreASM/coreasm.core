package org.coreasm.compiler.preprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.preprocessor.Information;

/**
 * Represents a piece of information added in the pre-processing process.
 * Manages a tree structure, where each edge has a unique string identifying it.
 * Each node can therefore be addressed using a path consisting of several strings.
 * An arbitrary object can be stored at each node.
 * @author Markus Brenner
 *
 */
public class Information {
	private Map<String, Information> information;
	private Object value;
	
	/**
	 * Constructs an empty Information object
	 */
	public Information(){
		information = new HashMap<String, Information>();
	}
	
	/**
	 * Obtains the value of this instance
	 * @return The Object stored
	 */
	public Object getValue(){
		return value;
	}
	
	/**
	 * Finds all direct children of this information object.
	 * Does not return the information objects, but their
	 * respective child paths starting at this node.
	 * @return A list of strings representing the children of this information node
	 */
	public List<String> getChildren(){		
		return Collections.unmodifiableList(new ArrayList<String>(information.keySet()));
	}
	
	/**
	 * Searches for an information object with the given path
	 * @param path The path to the information object
	 * @return The requested information object or null, if the path was invalid
	 */
	public Information getInformation(String ...path){
		return find(false, path);
	}
	
	/**
	 * Sets the value of the information object with the given path.
	 * Will override already existing values and will create the given
	 * path if not existent.
	 * @param o The value for the instance
	 * @param path The path to the information instance
	 */
	public void setValue(Object o, String ...path){
		Information i = find(true, path);
		if(i != null){
			i.value = o;
		}
	}
	
	private Information find(boolean create, String ...path){
		//search for an information object at the given path
		Information current = this;
		
		if(path == null) return this;
		//iterate over the path until the end is reached
		for(String s : path){
			Information i = current.information.get(s);
			if(i == null){
				//if create is set to true, create non-existing paths
				if(create){
					i = new Information();
					current.information.put(s, i);
				}
				else{
					return null;
				}
			}
			current = i;
		}
		return current;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Information){
			Information i = (Information) o;
			
			boolean valueequal = (value == null && i.value == null) || (value != null && value.equals(i.value));
			boolean informationequal = information.equals(i.information);
			
			return valueequal && informationequal;
		}
		return false;
	}
	
	@Override
	public String toString(){
		String result = "(";
		
		result += "[" + value + "]; ";
		for(Entry<String, Information> e : information.entrySet()){
			result += e.getKey() + " : " + e.getValue() + ", ";
		}
		
		result += ")";
		return result;
	}
}
