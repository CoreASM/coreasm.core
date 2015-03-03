package org.coreasm.compiler.newexception;

public class CompilerException extends Exception {
	private static final long serialVersionUID = 1L;
	private Class<?> component;
	
	public CompilerException(Exception parent, Class<?> component, String message){
		super("[" + component.getName() + "]: " + message, parent);
		this.component = component;
	}
	
	public Class<?> sourceComponent(){
		return component;
	}
}
