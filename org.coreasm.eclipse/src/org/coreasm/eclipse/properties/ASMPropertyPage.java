package org.coreasm.eclipse.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class ASMPropertyPage extends PropertyPage {

	private static final String PATH_TITLE = "Path:";
	private static final String AUTHOR_TITLE = "&Author:";
	private static final String AUTHOR_PROPERTY = "AUTHOR";
	private static final String DEFAULT_AUTHOR = "unknown";

	private static final int TEXT_FIELD_WIDTH = 50;

	private Text authorText;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ASMPropertyPage() {
		super();
	}

	private void addFirstSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		//Label for path field
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText(PATH_TITLE);

		// Path text field
		Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setText(((IResource) getElement()).getFullPath().toString());
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Label for author field
		Label authorLabel = new Label(composite, SWT.NONE);
		authorLabel.setText(AUTHOR_TITLE);

		// Author text field
		authorText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		authorText.setLayoutData(gd);

		// Populate author text field
		try {
			String author =
				((IResource) getElement()).getPersistentProperty(
					new QualifiedName("", AUTHOR_PROPERTY));
			authorText.setText((author != null) ? author : DEFAULT_AUTHOR);
		} catch (CoreException e) {
			authorText.setText(DEFAULT_AUTHOR);
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		addSecondSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		// Populate the author text field with the default value
		authorText.setText(System.getProperty("user.name",DEFAULT_AUTHOR));
	}
	
	public boolean performOk() {
		// store the value in the author text field
		try {
			((IResource) getElement()).setPersistentProperty(
				new QualifiedName("", AUTHOR_PROPERTY),
				authorText.getText());
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}