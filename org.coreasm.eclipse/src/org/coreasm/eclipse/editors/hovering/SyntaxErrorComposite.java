package org.coreasm.eclipse.editors.hovering;

import java.util.LinkedList;
import java.util.List;

import org.coreasm.eclipse.editors.IconManager;
import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.eclipse.editors.errors.AbstractQuickFix;
import org.coreasm.eclipse.editors.errors.AbstractQuickFix.QF_Replace;
import org.coreasm.eclipse.editors.errors.SyntaxError;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

/**
 * Composite for displaying the body of a hover window for SyntaxErrors.
 * It displays the description of the errors and manages the retrieval of
 * QuickFixes for that error. The QucikFixes are shown in a separate composite
 * which is created by this class and added below it.
 * 
 * The composite displays the list of expected tokens as a list of links. If the
 * user clicks onto one of these links, its token is used for the Replace and
 * Insert QuickFixes.
 * 
 * @author Markus Müller
 */
public class SyntaxErrorComposite
extends AbstractHoverComposite 
{
	private static Image imageBullet = IconManager.getIcon("/icons/editor/bullet.gif");

	Label wEncountered;
	Link wExpected;
	Link wReplace;
	Link wInsert;
	String selectedChoice = "";
	QF_Replace qfReplace;
	QF_Replace qfInsert;
	
	public SyntaxErrorComposite(Composite parent, int style) 
	{
		super(parent, style);
		
		// Layout
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 10;
		gridLayout.verticalSpacing = 5;
		this.setLayout(gridLayout);
		GridData gridData;
		
		// Encountered
		Label lEncountered = new Label(this,0);
		lEncountered.setText("Encountered:");
		wEncountered = new Label(this,SWT.WRAP);
		
		// Expected
		Label lExpected = new Label(this,0);
		lExpected.setText("Expected:");
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		lExpected.setLayoutData(gridData);
		wExpected = new Link(this,SWT.WRAP);
		gridData =  new GridData();
		gridData.widthHint = 300;  // size hint needed for wrapping
		wExpected.setLayoutData(gridData);
		
		wExpected.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				updateChoice(event.text.trim());
			}
		});
		
	}
		
	/**
	 * This method updates the Replace and Insert QuickFixes with the given choice.
	 * It is called by the event listener of the expected widget.
	 */
	private void updateChoice(String choice)
	{
		selectedChoice = choice;
		qfReplace.setInsert(choice);
		qfInsert.setInsert(choice + " ");
		wReplace.setText("<a>Replace with:</a> " + choice);
		wInsert.setText("<a>Insert:</a> " + choice);
		
	}
	
	/**
	 * Sets the data of this composite which means setting the encountered
	 * and expected label with the data from the given error object.
	 */
	@Override
	public void setData(AbstractError error) 
	{
		if (error instanceof SyntaxError) {
			SyntaxError sError = (SyntaxError) error;
			wEncountered.setText(sError.getEncountered());
			
			String strExpected = "";
			for (int i=0; i<sError.getExpected().length; i++) {
				String e = sError.getExpected()[i];
				// add a space to the < operator (workaround for misinterpreting the < as a tag opener)
				if (e.equals("<"))
					e = "< ";
				// don't show links for EOF and terminal tokens.
				if (e.equals("EOF") || e.equals("IDENTIFIER") || e.equals("DECIMAL") || e.equals("string literal"))
					strExpected += e;
				else
					strExpected += "<a>" + e + "</a>";
				if (i < sError.getExpected().length-1)
					strExpected += ", ";
			}
			wExpected.setText(strExpected);
			
		}
		
		// Quick Fixes
		List<AbstractQuickFix> fixes = fetchFixes(error);
		addFixes(fixes, error);
		
	}
	
	/**
	 * Generates the QuickFixes for SyntaxErrors. There are three QuickFixes:
	 * <ul>
	 * <li>Delete the token which caused the error</li>
	 * <li>Replace the token with one from the expected list</li>
	 * <li>Insert a token from the expected list</li>
	 * </ul>
	 */
	private List<AbstractQuickFix> fetchFixes(AbstractError error)
	{
		List<AbstractQuickFix> fixes = new LinkedList<AbstractQuickFix>();
		
		qfReplace = new AbstractQuickFix.QF_Replace("Replace with: ", "", true);
		qfInsert = new AbstractQuickFix.QF_Replace("Insert: ", "", false);
		fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		fixes.add(qfReplace);
		fixes.add(qfInsert);
		
		return fixes;
	}
	
	/**
	 * Generates a new composite and adds all the given quick fixes. The composite
	 * is added below this composite. 
	 */
	private void addFixes(List<AbstractQuickFix> fixes, final AbstractError error)
	{
		if (fixes == null || fixes.size() == 0)
			return;
		
		Composite wQuickFixes = new Composite(getShell(),SWT.FILL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginBottom = 0;
		gridLayout.marginTop = 2;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		gridLayout.horizontalSpacing = 10;
		gridLayout.verticalSpacing = 2;
		wQuickFixes.setLayout(gridLayout);
		
		//GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gridData.horizontalSpan = 1;
		wQuickFixes.setLayoutData(gridData);
		
		Label label = new Label(wQuickFixes,0);
		label.setText("Quick Fixes:");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		
		for (final AbstractQuickFix fix: fixes) {
			
			Label wIcon = new Label(wQuickFixes, 0);
			wIcon.setText("");
			wIcon.setImage(imageBullet);
			
			// link prompt string
			Link link = new Link(wQuickFixes,0);
			link.setText("<a>" + fix.prompt + "</a>");
			link.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					fix.fix(error, null);
					getShell().dispose();
				}
			});		
			if (fix == qfReplace)
				wReplace = link;
			if (fix == qfInsert)
				wInsert = link;

		}
		
		wReplace.setText("Replace with: (select from above)");
		wInsert.setText("Insert: (select from above)");
		
	}
	

}
