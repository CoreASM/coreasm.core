package org.coreasm.eclipse.wizards;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	private IFile selected;
	private Text txtStepCount;
	
	private Map<String, Button> settings;
	
	public ToJarExportWizardPage(IFile selected) {
		super("Export information");
		setTitle("Export information");
		setDescription("Jar export: Information");
		this.selected = selected;
		
	}	
	
	
	@Override
	public void createControl(Composite parent) {
		settings = new HashMap<String, Button>();
		
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
			
		//other settings:
		/*
		 * 	keepTempFiles
			removeExistingFiles
			noCompile
			
			terminateOnError
			terminateOnFailedUpdate
			terminateOnEmptyUpdate
			terminateOnSameUpdate
			terminateOnUndefAgent
			terminateOnStepCount
			
			logUpdatesAfterStep
			logStateAfterStep
			logEndOfStep
			logAgentSetAfterStep
			logStateTransition


		 * 
		 * 
		 * 
		 */
		
			
		buildTextLine("Termination settings: ", container);
		
		buildButton("terminateOnError", "terminate on errors", container, true);
		buildButton("terminateOnFailedUpdate", "terminate on failed updates", container, false);
		buildButton("terminateOnEmptyUpdate", "terminate on empty update", container, false);
		buildButton("terminateOnSameUpdate", "terminate on same update", container, false);
		buildButton("terminateOnUndefAgent", "terminate on undef agent", container, false);
		//TODO: Step count
		new Label(container, SWT.NONE);
		
		Composite tmpComp = new Composite(container, SWT.NONE);		
		tmpComp.setLayout(new GridLayout(2, false));
		txtStepCount = new Text(tmpComp, SWT.BORDER);
		txtStepCount.setText("-1");
		(new Label(tmpComp, SWT.NONE)).setText("terminate after step count");
		new Label(container, SWT.NONE);
		
		
		buildTextLine("Logging settings: ", container);
		
		buildButton("logUpdatesAfterStep", "log updates after step", container, false);
		buildButton("logStateAfterStep", "log state after step", container, false);
		buildButton("logEndOfStep", "log end of step", container, false);
		buildButton("logAgentSetAfterStep", "log agent set after step", container, false);
		buildButton("logStateTransition", "log state transition", container, false);
		
		buildTextLine("Other settings: ", container);
		
		buildButton("keepTempFiles", "Keep Temporary files", container, false);
		buildButton("removeExistingFiles", "Remove Files in the temporary directory", container, true);
		buildButton("noCompile", "Only generate java source files", container, false);
			
		
		setControl(container);
		setPageComplete(false);
	}
	
	private void buildTextLine(String text, Composite container){
		Label lblTmp = new Label(container, SWT.NONE);
		lblTmp.setText(text);
		lblTmp = new Label(container, SWT.NONE);
		lblTmp = new Label(container, SWT.NONE);
	}
	
	private void buildButton(String setting, String text, Composite container, boolean state){
		Label lblTmp = new Label(container, SWT.NONE);
		lblTmp.setText("");
		Button btnTmp = new Button(container, SWT.CHECK);
		btnTmp.setText(text);
		btnTmp.setSelection(state);
		
		settings.put(setting, btnTmp);
		lblTmp = new Label(container, SWT.NONE);
		lblTmp.setText("");
	}
	
	public String getSpec(){
		return txtSpecFile.getText();
	}
	
	public Map<String, Boolean> getSettings(){
		Map<String, Boolean> result = new HashMap<String, Boolean>();
		for(Entry<String, Button> e : settings.entrySet()){
			result.put(e.getKey(), e.getValue().getSelection());
		}
		
		return result;
	}
	
	public String getJar(){
		return txtOutputFile.getText();
	}
	
	public int getStepCount(){
		try{
			return Integer.parseInt(txtStepCount.getText());
		}
		catch(Exception e){
			return -1;
		}
	}
}
