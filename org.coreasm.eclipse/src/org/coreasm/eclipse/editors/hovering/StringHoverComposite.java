package org.coreasm.eclipse.editors.hovering;

import org.coreasm.eclipse.editors.errors.AbstractError;
import org.eclipse.swt.widgets.Composite;

/**
 * Simple composite to display a single String.
 * @author Michael
 *
 */
public class StringHoverComposite extends AbstractHoverComposite {

	public StringHoverComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void setData(AbstractError data) {
	}
}
