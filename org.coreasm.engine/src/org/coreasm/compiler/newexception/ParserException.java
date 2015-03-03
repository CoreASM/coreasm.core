package org.coreasm.compiler.newexception;

public class ParserException extends SpecificationLoadException {
	private static final long serialVersionUID = 1L;

	public ParserException(Exception error, Class<?> component){
		super(error, error.getMessage(), component);
	}
}
