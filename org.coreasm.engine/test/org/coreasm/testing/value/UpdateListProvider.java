package org.coreasm.testing.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;

public class UpdateListProvider {
	private List<UpdateProvider> updates;
	
	public UpdateListProvider(UpdateProvider...up){
		updates = Arrays.asList(up);
	}
	
	public UpdateMultiset interpreterValue(){
		List<Update> elements = new ArrayList<Update>();
		for(UpdateProvider up : updates){
			elements.add(up.interpreterValue());
		}
		UpdateMultiset um = new UpdateMultiset(elements);
		return um;
	}
	
	public String compilerValue(){
		String result = "";
		result += "__init_update_list__ = new java.util.ArrayList<CompilerRuntime.Update>();\n";
		for(UpdateProvider up : updates){
			result += up.compilerValue();
			result += "__init_update_list__.add((CompilerRuntime.Update)evalStack.pop());\n";
		}
		result += "evalStack.push(new CompilerRuntime.UpdateList(__init_update_list__));\n";
		return result;
	}
	
	public boolean equalsCompiler(Object o){
		if(o == null) return false;
		if(!(o instanceof ArrayList<?>)) return false;
		
		ArrayList<?> updates = (ArrayList<?>) o;
		if(updates.size() != this.updates.size()) return false;
		for(int i = 0; i < this.updates.size(); i++){
			if(!this.updates.get(i).equalsCompiler(updates.get(i))) return false;
		}
		return true;
	}
	
	public boolean equalsInterpreter(UpdateMultiset u){
		if(u == null) return false;
		int i = 0;
		if(u.size() != this.updates.size()) return false;
		
		for(Iterator<Update> iter = u.iterator(); iter.hasNext(); ){
			if(!this.updates.get(i).equalsInterpreter(iter.next())) return false;
			i++;
		}
		
		return true;
	}
}
