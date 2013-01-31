package org.coreasm.eclipse.editors.hovering;

import java.util.Map;

import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.eclipse.editors.errors.SimpleError;
import org.coreasm.eclipse.editors.errors.SyntaxError;
import org.coreasm.eclipse.editors.errors.UndefinedError;
import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This class manages the creation of all hover windows for errors. A
 * hover window for errors consists of three parts:
 * <ul>
 * <li>A header composite, showing a header label</li>
 * <li>A body composite, showing the detailed information for the hover</li>
 * <li>A composite for QuickFixes, which is created by the body composite</li>
 * </ul>
 * The composites for the header and the body are created by this class.
 * There are different classes for body composite which must be chosen depending
 * on the type of the error. Because the type of the error is only available
 * when setting the contents of the hover window, the body compisite cannot 
 * be created earlier.
 * @author Markus Müller
 *
 */
public class ASMInformationControl
extends AbstractInformationControl
implements IInformationControlExtension2
{
	int maxHeight = Integer.MAX_VALUE;
	int maxWidth = Integer.MAX_VALUE;
	
	HeaderComposite cHeader;
	AbstractHoverComposite cBody;
	
	public ASMInformationControl(Shell parentShell) 
	{
		super(parentShell, true);
		create();		
	}
		
	@Override
	protected void createContent(Composite parent)
	{
		getShell().setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginBottom = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginLeft = 2;
		gridLayout.marginRight = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		getShell().setLayout(gridLayout);
	}

	@Override
	public boolean hasContents()
	{
		return true;
	}

	/**
	 * Sets the input of the hover window. This method is called by the editor
	 * with the data retrieved by ASMTextHover which is delivered through the input
	 * parameter.
	 * 
	 * @param input the input data as retrieved by ASMTextHover. This class expects
	 * the input object to be a Map<String,Object> with two entries:
	 * <ul>
	 * <li>"document": a reference to the document the hover belongs to</li>
	 * <li>"marker": a reference to the marker which has been hovered</li>
	 * </ul>
	 */
	@Override
	public void setInput(Object input) 
	{
		if ((input instanceof Map<?,?>)) {
			Map<String,Object> map = (Map<String, Object>) input;
			IDocument document = (IDocument) map.get("document");
			IMarker marker = (IMarker) map.get("marker");
			int startOffset = 0;
			int stopOffset = 0;
			try {
				startOffset = (Integer) marker.getAttribute(IMarker.CHAR_START);
				stopOffset = (Integer) marker.getAttribute(IMarker.CHAR_END);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			
			try {
				if (ASMEditor.MARKER_TYPE_WARNING.equals(marker.getType())) {
					cBody = new StringHoverComposite(getShell(), 0);
					cBody.setLayout(new RowLayout());
					Label label = new Label(cBody, 0);
					label.setText(marker.getAttribute(IMarker.MESSAGE, ""));
					cBody.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
					
					getShell().getChildren()[0].dispose();
					getShell().layout(true,true);
					getShell().pack(true);
				}
				else {
					// Retrieve the error object out of the marker
					AbstractError error = AbstractError.decode(marker.getAttribute("errordata", ""));
					error.setDocument(document);
					setContents(error);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			getShell().layout(true, true);
			getShell().pack();
		}
		else if (input instanceof String) {
			cBody = new StringHoverComposite(getShell(), 0);
			cBody.setLayout(new RowLayout());
			Label label = new Label(cBody, 0);
			label.setText((String)input);
			cBody.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			getShell().getChildren()[0].dispose();
			getShell().layout(true,true);
			getShell().pack(true);
		}
	}
	

	/**
	 * Helper method with generates the composites for header and body depending
	 * on the type of the error and fills both with the data of the error object.
	 */
	private void setContents(AbstractError error)
	{
		GridData gd;
		
		cHeader = new HeaderComposite(getShell(), 0);
		//gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		cHeader.setLayoutData(gd);
		cHeader.setBackground(getShell().getBackground());
		cHeader.setForeground(getShell().getForeground());
		
		if (error instanceof SimpleError) {
			SimpleError sError = (SimpleError) error;
			cHeader.setIcon("/icons/editor/error.gif");
			cHeader.setHeader(sError.getTitle());
			cBody = new SimpleHoverSubComposite(getShell(), 0);
			cBody.setData(error);
		}
		if (error instanceof SyntaxError) {
			SyntaxError sError = (SyntaxError) error;
			cHeader.setIcon("/icons/editor/error.gif");
			cHeader.setHeader("Syntax Error");
			cBody = new SyntaxErrorComposite(getShell(), 0);
			cBody.setData(error);
		}
		if (error instanceof UndefinedError) {
			UndefinedError uError = (UndefinedError) error;
			cHeader.setIcon("/icons/editor/error.gif");
			cHeader.setHeader("Unknown Error");
			cBody = new UnknownErrorComposite(getShell(), 0);
			cBody.setData(error);
		}

		//gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		cBody.setLayoutData(gd);
		
		getShell().getChildren()[0].dispose();
		getShell().layout(true,true);
		getShell().pack(true);
		
	}
	
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}
	
	/**
	 * Computes the optimal size for the hover window
	 */
	@Override
	public Point computeSizeHint()
	{		
		int x = SWT.DEFAULT;
		int y = SWT.DEFAULT;
		Point size = getShell().computeSize(x, y);
		if (size.x > maxWidth)
			x = maxWidth;
		if (size.y > maxHeight)
			y = maxHeight;
		if (x != SWT.DEFAULT || y != SWT.DEFAULT)
			size = getShell().computeSize(x, y, false);
		return size;
	}	
	
	public void setSize(int width, int height)
	{
		getShell().setSize(width, height);
	}
	
	public boolean isFocusControl()
	{
		return getShell().getDisplay().getActiveShell() == getShell();
	}
	
	@Override 
	public void setFocus()
	{
		getShell().forceFocus();
		if (cHeader != null)
			cHeader.setFocus();
	}

	/**
	 * This method creates an enriched version of this control. Since in our case
	 * the enriched control is the same as the original control, we just create
	 * another instance of this class. 
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				ASMInformationControl aic = new ASMInformationControl(parent);
				return aic;
			}
		};
	}


	
	
}