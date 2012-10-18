package org.coreasm.eclipse.debug.ui.views;

import org.coreasm.eclipse.debug.util.ASMDebugUtils;

/**
 * Implementation of a base class for elements of the ASM Update View
 * @author Michael Stegmaier
 *
 */
public class ASMUpdateViewElement {
	private String text;
	private String sourceName;
	private int lineNumber;
	
	public ASMUpdateViewElement(String text) {
		this.text = text;
		sourceName = ASMDebugUtils.parseSourceName(text);
		lineNumber = ASMDebugUtils.parseLineNumber(text);
		if (sourceName == null || lineNumber < 0)
			return;
	}
	
	/**
	 * Returns the given text
	 * @return the given text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Returns the number of the line in the assigned source file.
	 * @return the number of the line in the assigned source file
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Returns the name of the assigned source file.
	 * @return the name of the assigned source file
	 */
	public String getSourceName() {
		return sourceName;
	}
	
	@Override
	public String toString() {
		return getText();
	}
}
