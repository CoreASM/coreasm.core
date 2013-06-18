package org.coreasm.engine.informationHandler;

import java.io.Serializable;

public class InformationObject implements Serializable{

	private static final long serialVersionUID = 1L;

	/** verbosity levels which are equal the verbosity levels
	 * use within the current carma inplementation */
	public static enum VerbosityLevel {OFF, ERROR, WARNING, INFO, DEBUG}

	/** source of the information object, i.e. where has been this object created */
	private final StackTraceElement[] source;
	/** information */
	private final String message;
	/** tag for verbosity level of the information (default: WARNING) */
	private final VerbosityLevel verbosity;
	/** object of interest, e.g. a node object which caused an error during interpretation */
	private final Object objectOfInterest;

	public InformationObject(String message, VerbosityLevel verbosity, Object objectOfInterest) {
		this.source = Thread.currentThread().getStackTrace();
		this.message = message;
		this.verbosity = verbosity;
		this.objectOfInterest = objectOfInterest;
	}

	public InformationObject(String message, Object objectOfInterest) {
		this(message, VerbosityLevel.WARNING, objectOfInterest);
	}

	public InformationObject(String message) {
		this(message, VerbosityLevel.WARNING, null);
	}

	public String getSourceAsString() {
		return "File "+this.source[1].getFileName()+", "+
				this.source[1].getClassName()+"."+this.source[1].getMethodName()+", "+
				"line "+this.source[1].getLineNumber()+".";
	}

	public StackTraceElement getSourceObject(){
		return this.source[3];
	}

	/**
	 * the stacktrace can be used to create an exception
	 * @return StackTraceElement[]
	 */
	public StackTraceElement[] getSourceStackTrace(){
		return this.source;
	}

	public String getMessage() {
		return this.message;
	}

	public VerbosityLevel getVerbosity() {
		return verbosity;
	}

	public Object getObjectOfInterest() {
		return objectOfInterest;
	}

	public String toString(){
		String out = "";
		out += this.getMessage()+", "+
			this.getSourceAsString()+", "+
			this.getVerbosity().toString();
		if (this.getObjectOfInterest() != null)
			out += ", "+this.getObjectOfInterest().toString();
		return out;
	}

	public boolean equals(InformationObject comparteTo) {
		if (
				this.getSourceObject().equals(comparteTo.getSourceObject())
				&& this.getMessage().equals(comparteTo.getMessage())
				&& this.getVerbosity().equals(comparteTo.getVerbosity())
				&& this.getObjectOfInterest().equals(comparteTo.getObjectOfInterest())
			)
			return true;
		else
			return false;
	}

}