package org.coreasm.eclipse.wizards.compiler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.CompilerOptions;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ToJarExportWizardPage extends WizardPage {
	private Composite container;
	private IFile selected;
	private CompilerOptions resultOptions;
	private Map<Field, Button> booleanInputs;
	private Map<Field, Text> textInputs;
	private Map<Field, Text> fileInputs;
	private List<String> hideList;
	
	private Button btnCompileAndRun;
	
	public ToJarExportWizardPage(IFile selected) {
		super("Export information");
		setTitle("Export information");
		setDescription("Jar export: Information");
		this.selected = selected;
		makeHideList();
	}	
	
	private void makeHideList(){
		hideList = new ArrayList<String>();
		hideList.add("enginePath");
		hideList.add("tempDirectory");
	}
	
	@Override
	public void createControl(Composite parent) {
		resultOptions = new CompilerOptions();
		Class<?> options = CompilerOptions.class;
		
		container = new Composite(parent, SWT.NONE);
		
		GridLayout grid = new GridLayout();
		container.setLayout(grid);
		grid.numColumns = 3;
		
		booleanInputs = new HashMap<Field, Button>();
		textInputs = new HashMap<Field, Text>();
		fileInputs = new HashMap<Field, Text>();
	
		Field[] fields = options.getFields();
		for(Field f : fields){
			if(hideList.contains(f.getName())) continue;
			
			if(!f.isAccessible()){
				f.setAccessible(true);
			}
			
			if(f.getType() == File.class){
				Label lblPropertyText = new Label(container, SWT.NONE);
				lblPropertyText.setText(f.getName());
				
				Text txtProperty = new Text(container, SWT.SINGLE | SWT.BORDER);
				txtProperty.setEnabled(false);
				fileInputs.put(f, txtProperty);
				GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
				gridData.grabExcessHorizontalSpace = true;
				gridData.horizontalSpan = 1;
			    txtProperty.setLayoutData(gridData);
			    
			    if(f.getName().equals("SpecificationName") && selected != null){
			    	txtProperty.setText(selected.getLocation().toOSString());
			    }
			    else{
				    File file = null;
				    try {
						file = (File) f.get(resultOptions);
					} catch (Exception e){
						//should not happen
						e.printStackTrace();
					}
				    
				    if(file != null){
				    	txtProperty.setText(file.getAbsolutePath());
				    }
			    }
				
				Button btnProperty = new Button(container, SWT.PUSH);
				btnProperty.setText("...");
				
				btnProperty.addListener(SWT.Selection, new FileSelectionListener(txtProperty, f.getName()));
			}
			else if(f.getType() == boolean.class){
				makePlaceHolder(container);
				//check button for the property
				Button btnProperty = new Button(container, SWT.CHECK);
				btnProperty.setText(f.getName());
				booleanInputs.put(f, btnProperty);
				try {
					btnProperty.setSelection(f.getBoolean(resultOptions));
				} catch (Exception e){
					//should not happen
					e.printStackTrace();
				}
				makePlaceHolder(container);
			}
			else if(f.getType() == int.class){
				makePlaceHolder(container);
				Composite compContainer = new Composite(container, SWT.NONE);		
				compContainer.setLayout(new GridLayout(2, false));
				Text txtProperty = new Text(compContainer, SWT.BORDER);
				textInputs.put(f, txtProperty);
				try {
					txtProperty.setText("" + f.getInt(resultOptions));
				} catch (Exception e){
					//should not happen
					e.printStackTrace();
				}
				(new Label(compContainer, SWT.NONE)).setText(f.getName());
				makePlaceHolder(container);
			}
		}
		btnCompileAndRun = new Button(container, SWT.CHECK);
		btnCompileAndRun.setText("Run jar after compilation");
		
		setControl(container);
		setPageComplete(true);
	}
	
	public boolean runJar(){
		return btnCompileAndRun.getSelection();
	}
	
	public CompilerOptions getResult(){
		Field[] fields = resultOptions.getClass().getFields();
		for(Field f : fields){
			if(hideList.contains(f.getName())) continue;
			if(f.getType() == int.class){
				try{
					f.set(resultOptions, Integer.parseInt(textInputs.get(f).getText()));
				}
				catch(Exception e){
					//TODO: somehow print that the value was invalid
					System.out.println("invalid value: " + textInputs.get(f).getText());
					System.exit(0);
				}
			}
			else if(f.getType() == Boolean.class){
				try{
					f.set(resultOptions, booleanInputs.get(f).getSelection());
				}
				catch(Exception e){
					//this should not happen
				}
			}
			else if(f.getType() == File.class){
				try{
					f.set(resultOptions, new File(fileInputs.get(f).getText()));
				}
				catch(Exception e){
					//TODO: display appropriate errors
				}
			}
		}
		return resultOptions;
	}
	
	private void makePlaceHolder(Composite container){
		Label lblPlaceHolder = new Label(container, SWT.NONE);lblPlaceHolder.setText("");
	}
}
