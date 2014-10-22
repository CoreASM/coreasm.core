package CompilerRuntime;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list of updates
 * @author Markus Brenner
 *
 */
public class UpdateList extends ArrayList<Update>{
	private static final long serialVersionUID = 1L;
	
	public UpdateList(){
		super();
	}
	
	public UpdateList(Collection<Update> set) {
		super(set);
	}
	
	public UpdateList(Update u){
		super();
		this.add(u);
	}

	@Override
	public String toString(){
		String s = "UpdateList\n";
		for(int i = 0; i < this.size(); i++){
			s = s + "(" + this.get(i).loc + ", " + this.get(i).action + ", " + this.get(i).value + ")\n";
		}
		return s;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof UpdateList){
			UpdateList ul = (UpdateList)o;
			if(ul.size() == this.size()){
				for(int i = 0; i < this.size(); i++){
					if(!this.get(i).equals(ul.get(i))) return false;
				}
				return true;
			}
		}
		return false;
	}
}
