package org.coreasm.testing.value;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.testing.loading.ReflectionHelper;

public class ParameterProvider {
	private LocationProvider location;
	private UpdateListProvider update;
	private ElementProvider element;
	
	public ParameterProvider(LocationProvider loc){
		this.location = loc;
	}
	
	public ParameterProvider(UpdateListProvider updates){
		this.update = updates;
	}
	
	public ParameterProvider(UpdateProvider ...updates){
		this.update = new UpdateListProvider(updates);
	}
	
	public ParameterProvider(ElementProvider element){
		this.element = element;
	}
	
	public ParameterProvider(LocationProvider location, UpdateListProvider update, ElementProvider element){
		this.location = location;
		this.update = update;
		this.element = element;
	}
	
	public ParameterProvider(LocationProvider location, ElementProvider element, UpdateProvider...update){
		this.location = location;
		this.update = new UpdateListProvider(update);
		this.element = element;
	}
	
	public Location interpreterLocation(){
		if(location == null) return null;
		return location.interpreterValue();
	}
	
	public Element interpreterElement(){
		if(element == null) return null;
		return element.interpreterValue();
	}
	
	public UpdateMultiset interpreterUpdate(){
		if(update == null) return null;
		return update.interpreterValue();
	}
	
	public String compilerValue(){
		String result = "";
		if(location != null) result += location.compilerValue();
		if(update != null) result += update.compilerValue();
		if(element != null) result += element.compilerValue();
		
		return result;
	}
	
	public boolean equalsCompiler(Object o){
		try{
			Object element = ReflectionHelper.getField(o, "element");//o.getClass().getDeclaredField("element").get(o);
			Object location = ReflectionHelper.getField(o, "location");//o.getClass().getDeclaredField("location").get(o);
			Object ulist = ReflectionHelper.getField(o, "ulist");//o.getClass().getDeclaredField("ulist").get(o);
			
			if(this.element == null && element != null) return false;
			if(this.location == null && location != null) return false;
			if(this.update == null && ulist != null) return false;
			
			return (this.element == null || this.element.equalsCompiler(element)) && 
					(this.location == null || this.location.equalsCompiler(location)) &&
					(this.update == null || this.update.equalsCompiler(ulist));
		}
		catch(Exception e){
			return false;
		}
	}
	
	public boolean equalsInterpreter(ASTNode node){
		Location loc = node.getLocation();
		Element val = node.getValue();
		UpdateMultiset upd = node.getUpdates();
		
		if(this.element == null && val != null) return false;
		if(this.location == null && loc != null) return false;
		if(this.update == null && upd != null) return false;
		
		return (this.element == null || this.element.equalsInterpreter(val)) &&
				(this.location == null || this.location.equalsInterpreter(loc)) &&
				(this.update == null || this.update.equalsInterpreter(upd));
	}
}
