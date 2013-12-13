package org.coreasm.eclipse.preferences;

import org.coreasm.eclipse.CoreASMPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class ASMPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public ASMPreferencePage() {
		super(GRID);
		setPreferenceStore(CoreASMPlugin.getDefault().getPreferenceStore());
		setDescription("Global preferences for CoreASM Eclipse Plugin");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		/*
		addField(new DirectoryFieldEditor(PreferenceConstants.ROOT_FOLDER, 
				"CoreASM &root folder:", getFieldEditorParent()));
		*/
		
		addField(new DirectoryFieldEditor(PreferenceConstants.ADDITIONAL_PLUGINS_FOLDERS, 
				"CoreASM additional plugins folder:", getFieldEditorParent()));

		addField(new IntegerFieldEditor(PreferenceConstants.MAX_PROCESSORS, 
				"Max. number of processors to use (beta):", getFieldEditorParent()));
		
		//preferences from the ASMEditor component regarding color and activation status of bracket highlighting
		addField(new ColorFieldEditor(PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR,
				"highlighting color for brackets" , getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_MATCHING_BRACKETS,
				"highlight brackets (), {}, []" , getFieldEditorParent()));
		
		/*
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
*/
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		// Angepasst an Modifikation in der Klasse PreferenceConstants:
		//PreferenceConstants.setDirtyBit();
		PreferenceConstants.setDirtyBit(true);
		return super.performOk();
	}

	@Override
	protected void performApply() {
		super.performApply();
		// Angepasst an Modifikation in der Klasse PreferenceConstants:
		//PreferenceConstants.setDirtyBit();
		PreferenceConstants.setDirtyBit(true);
	}
	
}