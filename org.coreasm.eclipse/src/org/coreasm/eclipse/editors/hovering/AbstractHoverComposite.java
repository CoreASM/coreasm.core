package org.coreasm.eclipse.editors.hovering;

import org.coreasm.eclipse.editors.errors.AbstractError;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract super class for all body composites
 * @author Markus Müller
 */
public abstract class AbstractHoverComposite 
extends Composite
{
	public static enum SubType {
		NONE, SIMPLE, SYNTAX_ERROR, UNDEFINED
	}
	
	public AbstractHoverComposite(Composite parent, int style)
	{
		super(parent, style);
		this.setBackground(parent.getBackground());
		this.setForeground(parent.getForeground());
		this.setBackgroundMode(SWT.INHERIT_DEFAULT);
	}
	
	/**
	 * This method is called by ASMInformationControl to set the data
	 * of this composite.
	 * @param data
	 */
	public abstract void setData(AbstractError data);
	
}
