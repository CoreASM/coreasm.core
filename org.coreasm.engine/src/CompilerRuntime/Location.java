package CompilerRuntime;

import java.util.List;

/** 
 *	Implements LOCATION elements
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class Location {

	/**
	 * The name of the location function
	 */
	public final String name;
	
	/**
	 * Arguments; list of Elements
	 */
	public final ElementList args;
	
	/** if not null, indicates whether this location is modifiable or not. */
	public final Boolean isModifiable;
	
	/**
	 * Creates a new location with the given
	 * function and agruments.
	 * 
	 * @param name the name of the function element the new location
	 * @param args list of abstract object values as arguments
	 */
	public Location(String name, List<? extends Element> args) {
		if (name == null)
			throw new NullPointerException("Name of a location cannot be null.");
		if (args == null)
			throw new NullPointerException("Arguments of a location cannot be null.");
		this.args = ElementList.create(args);
		this.name = name;
		this.isModifiable = null;
	}

	/**
	 * Creates a new location with the given
	 * function and agruments.
	 * 
	 * @param name the name of the function element the new location
	 * @param args list of abstract object values as arguments
	 * @param isModifiable indicates whether this location is modifiable.
	 */
	public Location(String name, List<? extends Element> args, boolean isModifiable) {
		if (name == null)
			throw new NullPointerException("Name of a location cannot be null.");
		if (args == null)
			throw new NullPointerException("Arguments of a location cannot be null.");
		this.args = ElementList.create(args);
		this.name = name;
		this.isModifiable = isModifiable;
	}

	/**
	 * Provides a <code>String</code> representation of this 
	 * location.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		return "(" + name + ", " + args + ")";
		String result = args.toString();
		result = "(" + result.substring(1, result.length() -1) + ")";
		return name + result;
	}

	/**
	 * Indicates whether an object is equal to this location. 
	 *  
	 * @see Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof Location) {
			Location l = (Location)o;
			result = this.name.equals(l.name) && this.args.equals(l.args);
		}
		return result;
	}
	
	/**
	 * Hashcode for locations. Must be overridden because equality is overridden. 
	 *  
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = name.hashCode();
		for (Element e: args)
			result += e.hashCode();
		return result;
	}
	
}

