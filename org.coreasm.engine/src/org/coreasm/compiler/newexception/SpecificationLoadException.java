package org.coreasm.compiler.newexception;

public class SpecificationLoadException extends CompilerException {
	private static final long serialVersionUID = 1L;

	public SpecificationLoadException(Exception parent, String message, Class<?> component){
		super(parent, component, message);
	}
}
