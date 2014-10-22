package org.coreasm.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ToJarExportWizardPage extends WizardPage {
	private Composite container;
	private Text txtOutputFile;
	private Text txtSpecFile;
	private Button btnKeepTmp;
	private IFile selected;
	
	public ToJarExportWizardPage(IFile selected) {
		super("Export information");
		setTitle("Export information");
		setDescription("Jar export: Information");
		this.selected = selected;
	}	
	
	
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		
		GridLayout grid = new GridLayout();
		container.setLayout(grid);
		grid.numColumns = 3;
		
		//line 1: specification file name
			Label lblSpecFile = new Label(container, SWT.NONE);
			lblSpecFile.setText("Specification:");
			
			txtSpecFile = new Text(container, SWT.SINGLE | SWT.BORDER);
			txtSpecFile.setEnabled(false);
			GridData gridDataSpec = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gridDataSpec.grabExcessHorizontalSpace = true;
			gridDataSpec.horizontalSpan = 1;
		    txtSpecFile.setLayoutData(gridDataSpec);
		    if(selected != null){
		    	txtSpecFile.setText(selected.getLocation().toOSString());
		    }
			
			Button btnSpecFile = new Button(container, SWT.PUSH);
			btnSpecFile.setText("...");
			
			btnSpecFile.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					//Display display = new Display();
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();//new Shell(display);
					shell.open();
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterNames(new String[]{"CoreASM specifications", "All Files (*.*)"});
					dialog.setFilterExtensions(new String[]{"*.coreasm|*.casm", "*.*"});
					
					String dialogResult = dialog.open();
					if(dialogResult != null){
						txtSpecFile.setText(dialogResult);
						if(txtOutputFile.getText() != "") setPageComplete(true);
					}
				}
			});
		
		//line 2: jar file name
			Label lblOutputFile = new Label(container, SWT.NONE);
			lblOutputFile.setText("Jar Path:");
			
			txtOutputFile = new Text(container, SWT.SINGLE | SWT.BORDER);
			txtOutputFile.setEnabled(false);
			GridData gridDataOutputFile = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gridDataOutputFile.grabExcessHorizontalSpace = true;
			gridDataOutputFile.horizontalSpan = 1;
		    txtOutputFile.setLayoutData(gridDataOutputFile);
			
			Button btnOutputFile = new Button(container, SWT.PUSH);
			btnOutputFile.setText("...");
			
			btnOutputFile.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					//Display display = new Display();
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();//new Shell(display);
					shell.open();
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterNames(new String[]{"Jar files", "All Files (*.*)"});
					dialog.setFilterExtensions(new String[]{"*.jar", "*.*"});
					
					String dialogResult = dialog.open();
					if(dialogResult != null){
						txtOutputFile.setText(dialogResult);
						if(txtSpecFile.getText() != "") setPageComplete(true);
					}
				}
			});
		//line 2: keep temp files
			Label lblKeepTmp = new Label(container, SWT.NONE);
			lblKeepTmp.setText("");
			
			btnKeepTmp = new Button(container, SWT.CHECK);
			btnKeepTmp.setText("Keep Temporary Files");
			
			Label lblKeepTmp2 = new Label(container, SWT.NONE);
			lblKeepTmp2.setText("");
		
		
		setControl(container);
		setPageComplete(false);
	}
	
	public String getSpec(){
		return txtSpecFile.getText();
	}
	
	public String getJar(){
		return txtOutputFile.getText();
	}
	
	public boolean keepTmp(){
		return btnKeepTmp.getSelection();
	}
}
