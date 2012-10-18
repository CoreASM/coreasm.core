package org.coreasm.eclipse.editors.hovering;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.coreasm.eclipse.editors.IconManager;
import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.eclipse.editors.errors.AbstractQuickFix;
import org.coreasm.eclipse.editors.errors.SimpleError;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

/**
 * Composite for displaying the body of a hover window for SimpleErrors.
 * It displays the description of the errors and manages the retrieval of
 * QuickFixes for that error. The QucikFixes are shown in a separate composite
 * which is created by this class and added below it.
 * @author Markus Müller
 */
public class SimpleHoverSubComposite 
extends AbstractHoverComposite 
{
	private static Image imageBullet = IconManager.getIcon("/icons/editor/bullet.gif");
	
	Label wDescription;			// The label for the description
	String classname = null;	// The classname of the ErrorParser which created the shown error
	String errorID;				// The ID of the error, as defined and delivered by the ErrorParser

	public SimpleHoverSubComposite(Composite parent, int style)
	{
		super(parent, style);		

		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.VERTICAL;
		this.setLayout(rowLayout);

		wDescription = new Label(this, 0);

	}

	/**
	 * Sets the description of the composite and organizes the retrieval of
	 * the QuickFixes and the creation of the QuickFixes composite.
	 */
	@Override
	public void setData(AbstractError error)
	{
		if (error instanceof SimpleError) {
			SimpleError sError = (SimpleError) error;
			wDescription.setText(sError.getDescription());
			classname = sError.getClassname();
			errorID = sError.getErrorID();
		}

		List<AbstractQuickFix> fixes = fetchFixes(error);
		addFixes(fixes, error);

	}

	/**
	 * Fetches all QuickFixes for the error from the ErrorParser which has created
	 * the error. Because we only have the classname for that ErrorParser we use
	 * the Java Reflection API to create a new instance of this class and call
	 * the the getQuickFixes() method of this object.
	 */
	private List<AbstractQuickFix> fetchFixes(AbstractError error)
	{
		List<AbstractQuickFix> list = new LinkedList<AbstractQuickFix>();

		try {
			Class<?> errorParser = Class.forName(classname);
			Method method = errorParser.getMethod("getQuickFixes", String.class);
			Object o = method.invoke(null, errorID);
			List<AbstractQuickFix> l = (List<AbstractQuickFix>) o;
			// Sort out fixes which are not applicable for the given error
			for (AbstractQuickFix fix: l)
				if (fix.checkValidility(error) == true)
					list.add(fix);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		for (AbstractQuickFix fix: list)
			fix.initChoices(error);

		return list;
	}

	/**
	 * Generates a new composite and adds all the given quick fixes. The composite
	 * is added below this composite. 
	 */
	private void addFixes(List<AbstractQuickFix> fixes, final AbstractError error)
	{
		// We don't need a composite if there are no QuickFixes
		if (fixes == null || fixes.size()==0)
			return;

		Composite wQuickFixes = new Composite(this, 0);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginBottom = 0;
		gridLayout.marginTop = 2;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		wQuickFixes.setLayout(gridLayout);
		GridData gridData;
		
		Label label = new Label(wQuickFixes,0);
		label.setText("Quick Fixes:");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);

		for (final AbstractQuickFix fix: fixes) {
			
			Label wIcon = new Label(wQuickFixes, 0);
			wIcon.setText("");
			wIcon.setImage(imageBullet);
			
			// The label of each QuickFix is an instance of the SWT Link widget
			// Clicking on a link causes an event which runs the fix() method
			// of the corresponding QuickFix.
			
			// if there are no choces: link prompt string
			if (fix.choices.size() == 0) {
				Link link = new Link(wQuickFixes,0);
				link.setText("<a>" + fix.prompt + "</a>");
				link.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						fix.fix(error, null);
						getShell().dispose();
					}
				});		
			}
			// else: make a link for each choice
			// if the prompt contains the character '@', this character will be
			// replaced by the choices, otherwise the choices will be added to
			// the end of the prompt.
			else {
				String linktext = fix.prompt;
				String strChoices = "";
				if (linktext.indexOf('@') == -1)
					linktext += ": @";

				for (int i=0; i<fix.choices.size(); i++) {
					strChoices += "<a>" + fix.choices.get(i) + "</a>";
					if (i<fix.choices.size()-1)
						strChoices += ", ";
				}
				
				linktext = linktext.replaceFirst("@", strChoices);
				
				Link link = new Link(wQuickFixes,0);
				link.setText(linktext);
				link.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						fix.fix(error, event.text);
						getShell().dispose();
					}
				});						
			}
		}
		
	}

	

}
