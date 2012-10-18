package org.coreasm.eclipse.editors.hovering;


import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.eclipse.editors.errors.UndefinedError;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Composite for displaying the body of a hover window for UndefinedErrors.
 * It displays the description of the error. There are no QuickFixes for
 * UndefinedErrors.
 * @author Markus Müller
 */
public class UnknownErrorComposite extends AbstractHoverComposite {

	Label wDescription;
	
	public UnknownErrorComposite(Composite parent, int style)
	{
		super(parent, style);
		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.VERTICAL;
		this.setLayout(rowLayout);
		wDescription = new Label(this, 0);
	}
	
	@Override
	public void setData(AbstractError data) {
		if (data instanceof UndefinedError) {
			UndefinedError uError = (UndefinedError) data;
			wDescription.setText("Message delivered by error:\n" + uError.getDescription());
		}
	}

}
