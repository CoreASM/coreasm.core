package org.coreasm.testing.value;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.plugins.string.StringElement;

public class StringProvider implements ElementProvider {
	private String value;

	public StringProvider(String value) {
		this.value = value;
	}
	
	@Override
	public Element interpreterValue() {
		return new StringElement(value);
	}

	@Override
	public String compilerValue() {
		return "evalStack.push(new plugins.StringPlugin.StringElement(\"" + value + "\"));\n";
	}

	@Override
	public boolean equalsCompiler(Object o) {
		if(!o.getClass().getName().equals("StringMock")) return false;
		String s = o.toString();
		if(!s.equals(value)) return false;
		return true;
	}

	@Override
	public boolean equalsInterpreter(Element e) {
		if(!(e instanceof StringElement)){
			return false;
		}
		StringElement se = (StringElement) e;
		if(se.getValue().equals(value)) {
			return true;
		}
		return false;
	}

}
