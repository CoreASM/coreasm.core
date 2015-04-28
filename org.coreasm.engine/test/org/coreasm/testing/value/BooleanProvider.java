package org.coreasm.testing.value;

import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.testing.loading.ReflectionHelper;

public class BooleanProvider implements ElementProvider {
	private boolean value;
	
	public static final BooleanProvider TRUE = new BooleanProvider(true);
	public static final BooleanProvider FALSE = new BooleanProvider(false);
	
	public BooleanProvider(boolean val){
		this.value = val;
	}
	
	@Override
	public Element interpreterValue() {
		return (value) ? BooleanElement.TRUE : BooleanElement.FALSE;
	}

	
	public void test(int i) {}
	public void test(Integer i) {}
	
	@Override
	public String compilerValue() {
		return "evalStack.push(CompilerRuntime.BooleanElement." + ((value) ? "TRUE" : "FALSE") + ");\n";
	}

	@Override
	public boolean equalsCompiler(Object o) {
		if(o == null) return false;
		try{
			if(!o.getClass().getName().equals("BooleanMock")) return false;
			Boolean val = (Boolean) ReflectionHelper.getField(o, "value");//o.getClass().getField("value").get(o);
			return val.booleanValue() == value;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean equalsInterpreter(Element e) {
		if(e == null) return false;
		if(!(e instanceof BooleanElement)) return false;
		if(value)
			return e.equals(BooleanElement.TRUE);
		else 
			return e.equals(BooleanElement.FALSE);
	}

}
