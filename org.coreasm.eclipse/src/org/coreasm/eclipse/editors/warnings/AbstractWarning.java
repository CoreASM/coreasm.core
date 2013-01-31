package org.coreasm.eclipse.editors.warnings;

public abstract class AbstractWarning {
	private String description;
	private int position;
	private int length;
	
	public AbstractWarning(String description, int position, int length) {
		this.description = description;
		this.position = position;
		this.length = length;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getLength() {
		return length;
	}
}
