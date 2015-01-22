package org.coreasm.eclipse.editors.warnings;


/**
 * An abstract implementation of a warning
 * @author Michael Stegmaier
 *
 */
public abstract class AbstractWarning {
	private final String description;
	private final String data;
	private final int position;
	private final int length;
	
	public AbstractWarning(String description, String data, int position, int length) {
		this.description = description;
		this.data = data;
		this.position = position;
		this.length = length;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getData() {
		return data;
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getLength() {
		return length;
	}
}
