package org.coreasm.eclipse.preferences;

import org.coreasm.eclipse.editors.contentassist.TemplateManager;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * @author Tobias
 *	
 * The preference page of the template
 */
public class ASMTemplatePreferencePage extends TemplatePreferencePage implements IWorkbenchPreferencePage {

	public ASMTemplatePreferencePage() {
		try {
			setPreferenceStore(TemplateManager.getInstance()
					.getPreferenceStore());
			setTemplateStore(TemplateManager.getInstance()
					.getTemplateStore());
			setContextTypeRegistry(TemplateManager.getInstance()
					.getContextTypeRegistry());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}

	public boolean performOk() {
		boolean ok = super.performOk();
		TemplateManager.getInstance().savePluginPreferences();
		return ok;
	}
}

