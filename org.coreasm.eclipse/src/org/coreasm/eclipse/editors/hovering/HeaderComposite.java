package org.coreasm.eclipse.editors.hovering;

import org.coreasm.eclipse.editors.IconManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Composite for displaying the header of a hover window. It consists of
 * a Label which shows an icon and a StyledText label which shows the header
 * text of the error which is shown in the hover.
 * @author Markus Müller
 */
public class HeaderComposite extends Composite
{
	private Label wIcon;			// Label for the icon
	private StyledText wHeader;		// Label for the header text
	//private Label wSeperator;		// Label for a separator
	
	public HeaderComposite(Composite parent, int style) 
	{
		super(parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		
		wIcon = new Label(this,0);
		wHeader = new StyledText(this, 0);
		
//		wSeperator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
//		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
//		gd.horizontalSpan = 2;
//		wSeperator.setLayoutData(gd);
	}
	
	public void setIcon(Image icon) 
	{
		this.wIcon.setImage(icon);
	}
	
	public void setIcon(String filename)
	{
		setIcon(IconManager.getIcon(filename));
	}

	public void setHeader(String header) 
	{
		this.wHeader.setText(header);
		StyleRange sr = new StyleRange();
		sr.start = 0;
		sr.length = header.length();
		sr.fontStyle = SWT.BOLD;
		this.wHeader.setStyleRange(sr);
	}
	
}
