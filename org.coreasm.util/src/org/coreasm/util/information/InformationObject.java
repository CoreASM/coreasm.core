package org.coreasm.util.information;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author Marcel Dausend
 *
 */
public class InformationObject implements Serializable{

	private static final long serialVersionUID = 1L;

	/** verbosity levels which are equal the verbosity levels
	 * use within the current carma implementation */
	public static enum VerbosityLevel {OFF, ERROR, WARNING, INFO, DEBUG, COMMUNICATION}

	/** source of the information object */
	private final AbstractDispatcher sender;
	/** information */
	private final String message;
	/** tag for verbosity level of the information (default: WARNING) */
	private final VerbosityLevel verbosity;
	
	private final Map<String, String> data;
	
	private final ResponseHandler responseHandler;

	public InformationObject(AbstractDispatcher sender, String message, VerbosityLevel verbosity, Map<String, String> data, ResponseHandler responseHandler) {
		this.sender = sender;
		this.message = message;
		this.verbosity = verbosity;
		this.data = data;
		this.responseHandler = responseHandler;
	}
	
	public InformationObject(AbstractDispatcher sender, String message, VerbosityLevel verbosity, Map<String, String> data) {
		this(sender, message, verbosity, data, null);
	}
	
	public InformationObject(AbstractDispatcher sender, String message, Map<String, String> data) {
		this(sender, message, data, null);
	}

	public InformationObject(AbstractDispatcher sender, String message, Map<String, String> data, ResponseHandler responseHandler) {
		this(sender, message, VerbosityLevel.WARNING, data, responseHandler);
	}
	
	public InformationObject(AbstractDispatcher sender, String message) {
		this(sender, message, (ResponseHandler)null);
	}

	public InformationObject(AbstractDispatcher sender, String message, ResponseHandler responseHandler) {
		this(sender, message, VerbosityLevel.WARNING, Collections.<String, String>emptyMap(), responseHandler);
	}

	public String getMessage() {
		return message;
	}
	
	public String getSender() {
		return sender.getId();
	}

	public VerbosityLevel getVerbosity() {
		return verbosity;
	}

	public Map<String, String> getData() {
		return data;
	}
	
	public void respond(Map<String, String> response) {
		responseHandler.handleResponse(response);
	}

	@Override
	public String toString() {
		return verbosity + " from " + sender + ": " + message;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InformationObject other = (InformationObject) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (verbosity != other.verbosity)
			return false;
		return true;
	}
}