package org.coreasm.testing.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.testing.loading.ReflectionHelper;

public class LocationProvider {
	private String name;
	private List<ElementProvider> params;
	
	public LocationProvider(String loc){
		this.name = loc;
		this.params = new ArrayList<ElementProvider>();
	}
	
	public LocationProvider(String loc, ElementProvider...params){
		this.name = loc;
		this.params = Arrays.asList(params);
	}
	
	public Location interpreterValue(){
		List<Element> p = new ArrayList<Element>();
		for(ElementProvider e : params){
			p.add(e.interpreterValue());
		}
		Location l = new Location(name, p);
		return l;
	}
	
	public String compilerValue(){
		String result = "";
		result += "__init_param_list__ = new java.util.ArrayList<CompilerRuntime.Element>();\n";
		for(ElementProvider e : params){
			result += e.compilerValue();
			result += "__init_param_list__.add((CompilerRuntime.Element)evalStack.pop());\n";
		}
		result += "evalStack.push(new CompilerRuntime.Location(\"" + name + "\", __init_param_list__));\n";
		return result;
	}
	
	public boolean equalsCompiler(Object o){
		if(o == null) return false;
		try{
			if(!ReflectionHelper.getField(o, "name").equals(name)){
				return false;
			}
			List<?> args = (List<?>) ReflectionHelper.getField(o, "args");;
			if(args.size() != params.size()) return false;
			
			for(int i = 0; i < args.size(); i++){
				if(!params.get(i).equalsCompiler(args.get(i))) return false;
			}
			
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	
	public boolean equalsInterpreter(Location loc){
		if(loc == null) return false;
		if(!loc.name.equals(name)){
			return false;
		}
		if(loc.args.size() != params.size()) return false;
		
		for(int i = 0; i < params.size(); i++){
			if(!params.get(i).equalsInterpreter(loc.args.get(i))) return false;
		}
		
		return true;
	}
}
