package org.coreasm.eclipse.wizards.compiler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class FileSelectionListener implements Listener {
	private Text selection;
	private String fieldName;
	
	public FileSelectionListener(Text txt, String fieldName){
		this.selection = txt;
		this.fieldName = fieldName;
	}
	
	@Override
	public void handleEvent(Event arg0) {
		//Display display = new Display();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();//new Shell(display);
		shell.open();
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		makeFilters(dialog);
		String dialogResult = dialog.open();
		if(dialogResult != null){
			selection.setText(dialogResult);
		}
	}

	private void makeFilters(FileDialog dialog){
		if(fieldName.equals("SpecificationName")){
			dialog.setFilterNames(new String[]{"CoreASM specifications", "All Files (*.*)"});
			dialog.setFilterExtensions(new String[]{"*.coreasm|*.casm", "*.*"});
		}
		else if(fieldName.equals("outputFile")){
			dialog.setFilterNames(new String[]{"jar Archives", "All Files (*.*)"});
			dialog.setFilterExtensions(new String[]{"*.jar", "*.*"});
		}
	}
}
