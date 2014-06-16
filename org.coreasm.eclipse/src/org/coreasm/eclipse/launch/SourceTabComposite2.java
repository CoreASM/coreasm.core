package org.coreasm.eclipse.launch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;


public class SourceTabComposite2 extends Composite {

	private Group group = null;
	private Group group1 = null;
	private Group group2 = null;
	private Label label = null;
	private Text project = null;
	private Button browseProjectButton = null;
	private Label label1 = null;
	private Text spec = null;
	private Button browseSpecButton = null;
	private Button stopOnErrors = null;
	private Label label2 = null;
	private Button stopOnFailedUpdates = null;
	private Label label3 = null;
	private Button stopOnEmptyUpdates = null;
	private Label label4 = null;
	private Button stopOnStableUpdates = null;
	private Label label5 = null;
	private Button stopOnEmptyActiveAgents = null;
	private Label label5_6 = null;
	private Button stopOnMaxSteps = null;
	private Label label6 = null;
	private Spinner maxSteps = null;
	private Composite composite = null;
	private Label label8 = null;
	private Combo logLevel = null;
	private Composite composite1 = null;
	private Button dumpUpdates = null;
	private Label label7 = null;
	private Button dumpState = null;
	private Label label9 = null;
	private Button dumpFinal = null;
	private Label label10 = null;
	private Button markSteps = null;
	private Label label11 = null;
	private Button printAgents = null;
	private Label label12 = null;

	/**
	 * This method initializes group	
	 *
	 */
	private void createGroup() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group = new Group(this, SWT.NONE);
		group.setText("Source");
		group.setLayout(gridLayout);
		group.setLayoutData(gridData);
		label = new Label(group, SWT.NONE);
		label.setText("Project");
		project = new Text(group, SWT.BORDER);
		project.setLayoutData(gridData3);
		browseProjectButton = new Button(group, SWT.NONE);
		browseProjectButton.setText("Browse...");
		label1 = new Label(group, SWT.NONE);
		label1.setText("Specification");
		spec = new Text(group, SWT.BORDER);
		spec.setLayoutData(gridData4);
		browseSpecButton = new Button(group, SWT.NONE);
		browseSpecButton.setText("Browse...");
	}

	/**
	 * This method initializes group1	
	 *
	 */
	private void createGroup1() {
		GridData gridData9 = new GridData();
		gridData9.horizontalSpan = 2;
		GridData gridData8 = new GridData();
		gridData8.horizontalSpan = 2;
		GridData gridData7 = new GridData();
		gridData7.horizontalSpan = 2;
		GridData gridData6 = new GridData();
		gridData6.horizontalSpan = 2;
		GridData gridData5 = new GridData();
		gridData5.horizontalSpan = 3;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 3;
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.grabExcessVerticalSpace = true;
		group1 = new Group(this, SWT.NONE);
		group1.setText("Termination condition");
		group1.setLayout(gridLayout1);
		group1.setLayoutData(gridData1);
		
		Composite comp1 = new Composite(group1, SWT.NONE);
		GridLayout compLayout = new GridLayout();
		compLayout.marginHeight = 0;
		compLayout.marginWidth = 0;
		compLayout.numColumns = 4;
		comp1.setLayout(compLayout);
		comp1.setLayoutData(gridData5);
		stopOnErrors = new Button(comp1, SWT.CHECK);
		label2 = new Label(comp1, SWT.NONE);
		label2.setText("Upon errors         ");
		stopOnFailedUpdates = new Button(comp1, SWT.CHECK);
		label3 = new Label(comp1, SWT.NONE);
		label3.setText("Upon failed updates");
		
		stopOnEmptyUpdates = new Button(group1, SWT.CHECK);
		label4 = new Label(group1, SWT.NONE);
		label4.setText("When a step returns an empty set of updates");
		label4.setLayoutData(gridData7);
		stopOnStableUpdates = new Button(group1, SWT.CHECK);
		label5 = new Label(group1, SWT.NONE);
		label5.setText("When a step returns the same set of updates as the previous one");
		label5.setLayoutData(gridData8);
		stopOnEmptyActiveAgents = new Button(group1, SWT.CHECK);
		label5_6 = new Label(group1, SWT.NONE);
		label5_6.setText("When there is no agent with a defined program.");
		label5_6.setLayoutData(gridData9);
		stopOnMaxSteps = new Button(group1, SWT.CHECK);
		label6 = new Label(group1, SWT.NONE);
		label6.setText("After this many steps have been performed: ");
		maxSteps = new Spinner(group1, SWT.NONE);
		maxSteps.setMaximum(999999);
	}

	/**
	 * This method initializes group2	
	 *
	 */
	private void createGroup2() {
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.grabExcessVerticalSpace = true;
		group2 = new Group(this, SWT.NONE);
		group2.setText("Verbosity");
		group2.setLayoutData(gridData2);
		createComposite();
		group2.setLayout(new GridLayout());
		createComposite1();
	}

	/**
	 * This method initializes composite	
	 *
	 */
	private void createComposite() {
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		gridLayout2.marginHeight = 0;
		composite = new Composite(group2, SWT.NONE);
		composite.setLayout(gridLayout2);
		label8 = new Label(composite, SWT.NONE);
		label8.setText("Log messages with at least the following severity level: ");
		createCombo();
	}

	/**
	 * This method initializes combo	
	 *
	 */
	private void createCombo() {
		GridData gridData9 = new GridData();
		gridData9.widthHint = 120;
		logLevel = new Combo(composite, SWT.READ_ONLY);
		logLevel.setText("test");
		logLevel.setLayoutData(gridData9);
	}

	/**
	 * This method initializes composite1	
	 *
	 */
	private void createComposite1() {
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 2;
		composite1 = new Composite(group2, SWT.NONE);
		composite1.setLayout(gridLayout3);
		dumpUpdates = new Button(composite1, SWT.CHECK);
		label7 = new Label(composite1, SWT.NONE);
		label7.setText("Dump updates after each step");
		dumpState = new Button(composite1, SWT.CHECK);
		label9 = new Label(composite1, SWT.NONE);
		label9.setText("Dump entire state after each step");
		dumpFinal = new Button(composite1, SWT.CHECK);
		label10 = new Label(composite1, SWT.NONE);
		label10.setText("Dump final report at termination");
		markSteps = new Button(composite1, SWT.CHECK);
		label11 = new Label(composite1, SWT.NONE);
		label11.setText("Mark the end of each step");
		printAgents = new Button(composite1, SWT.CHECK);
		label12 = new Label(composite1, SWT.NONE);
		label12.setText("Print the selected set of agents after each step.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
		 * for the correct SWT library path in order to run with the SWT dlls. 
		 * The dlls are located in the SWT plugin jar.  
		 * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
		 *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
		 */
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(new Point(300, 200));
		//SourceTabComposite2 thisClass = new SourceTabComposite2(shell, SWT.NONE);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public SourceTabComposite2(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		this.setLayout(new GridLayout());
		createGroup();
		createGroup1();
		createGroup2();
		setSize(new org.eclipse.swt.graphics.Point(517,800));
	}

	/**
	 * @return Returns the dumpState.
	 */
	public Button getDumpState() {
		return dumpState;
	}

	/**
	 * @return Returns the dumpUpdates.
	 */
	public Button getDumpUpdates() {
		return dumpUpdates;
	}

	/**
	 * @return Returns the logLevel.
	 */
	public Combo getLogLevel() {
		return logLevel;
	}

	/**
	 * @return Returns the maxSteps.
	 */
	public Spinner getMaxSteps() {
		return maxSteps;
	}

	/**
	 * @return Returns the project.
	 */
	public Text getProject() {
		return project;
	}

	/**
	 * @return Returns the spec.
	 */
	public Text getSpec() {
		return spec;
	}

	/**
	 * @return Returns the stopOnEmptyUpdates.
	 */
	public Button getStopOnEmptyUpdates() {
		return stopOnEmptyUpdates;
	}

	/**
	 * @return Returns the stopOnErrors.
	 */
	public Button getStopOnErrors() {
		return stopOnErrors;
	}

	/**
	 * @return Returns the stopOnFailedUpdates.
	 */
	public Button getStopOnFailedUpdates() {
		return stopOnFailedUpdates;
	}

	/**
	 * @return Returns the stopOnMaxSteps.
	 */
	public Button getStopOnMaxSteps() {
		return stopOnMaxSteps;
	}

	/**
	 * @return Returns the stopOnStableUpdates.
	 */
	public Button getStopOnStableUpdates() {
		return stopOnStableUpdates;
	}

	public Button getStopOnEmptyActiveAgents() {
		return stopOnEmptyActiveAgents;
	}
	
	/**
	 * @return Returns the browseProjectButton.
	 */
	public Button getBrowseProjectButton() {
		return browseProjectButton;
	}

	/**
	 * @return Returns the browseSpecButton.
	 */
	public Button getBrowseSpecButton() {
		return browseSpecButton;
	}

	/**
	 * @return Returns the dumpFinal.
	 */
	public Button getDumpFinal() {
		return dumpFinal;
	}

	/**
	 * @return mark steps check box
	 */
	public Button getMarkSteps() {
		return markSteps;
	}
	
	public Button getPrintAgents() {
		return printAgents;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
