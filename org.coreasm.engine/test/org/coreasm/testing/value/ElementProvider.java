package org.coreasm.testing.value;

import org.coreasm.engine.absstorage.Element;

public interface ElementProvider {
	public Element interpreterValue();
	public String compilerValue();
	
	public boolean equalsCompiler(Object o);
	public boolean equalsInterpreter(Element e);
}
