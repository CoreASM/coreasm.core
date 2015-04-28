package org.coreasm.testing.value;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.testing.loading.ReflectionHelper;

public class UpdateProvider {
	private LocationProvider loc;
	private ElementProvider elem;
	private String action;
		
	public UpdateProvider(LocationProvider loc, ElementProvider elem, String action){
		this.loc = loc;
		this.elem = elem;
		this.action = action;
	}
	
	public Update interpreterValue(){
		return new Update(loc.interpreterValue(), elem.interpreterValue(), action, (Element)null, null);
	}
	
	public String compilerValue(){
		String result = "";
		result += elem.compilerValue();
		result += loc.compilerValue();
		result += "evalStack.push(new CompilerRuntime.Update((CompilerRuntime.Location) evalStack.pop(), (CompilerRuntime.Element) evalStack.pop(), \"" + action + "\", (CompilerRuntime.Element) null, null));\n";
		return result;		
	}
	
	public boolean equalsCompiler(Object o){
		if(o == null) return false;
		try{
			Object loc = ReflectionHelper.getField(o, "loc");
			Object action = ReflectionHelper.getField(o, "action");
			Object value = ReflectionHelper.getField(o, "value");
			if(!this.loc.equalsCompiler(loc)) return false;
			if(!this.elem.equalsCompiler(value)) return false;
			if(!this.action.equals(action)) return false;
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	
	public boolean equalsInterpreter(Update u){
		if(u == null) return false;
		if(!loc.equalsInterpreter(u.loc)) return false;
		if(!elem.equalsInterpreter(u.value)) return false;
		if(!action.equals(u.action)) return false;
		return true;
	}
}
