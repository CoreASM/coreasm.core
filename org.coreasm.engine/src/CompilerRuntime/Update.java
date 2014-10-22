package CompilerRuntime;

import java.util.ArrayList;
import java.util.List;

/**
 * A single (partial) update
 * @author Markus Brenner
 *
 */
public class Update {
	public static final String UPDATE_ACTION = "UPDATE";
	public final Location loc;
	public final Element value;
	public final String action;
	public final ArrayList<Rule> agents;
	
	public Update(Location loc, Element value, String action, Rule agent) {
		if (loc == null || value == null || action == null)
			throw new NullPointerException("Cannot create an update instruction with " + loc + ", " + value + ", " + action);
		this.loc = loc;
		this.value = value;
		this.action = action;
		this.agents = new ArrayList<Rule>();
		this.agents.add(agent);
	}
	
	public Update(Location loc, Element value, String action, List<Rule> agent) {
		if (loc == null || value == null || action == null)
			throw new NullPointerException("Cannot create an update instruction with a null location, value, or action.");
		this.loc = loc;
		this.value = value;
		this.action = action;
		this.agents = new ArrayList<Rule>();
		this.agents.addAll(agent);
	}
	
	@Override
	public String toString(){
		return "(" + loc + ", " +action + ", " + value.denotation() + ")";
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Update){
			Update u = (Update) o;
			return loc.equals(u.loc) && value.equals(u.value) && action.equals(u.action);
		}
		return false;
	}
}
