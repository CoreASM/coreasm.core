package org.coreasm.eclipse.preferences;

import java.io.IOException;
import java.net.URL;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.util.Tools;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CoreASMPlugin.getDefault()
				.getPreferenceStore();

		URL u;
		try {
			u = FileLocator.toFileURL(CoreASMPlugin.getDefault().getBundle().getEntry("/"));
			store.setDefault(PreferenceConstants.ROOT_FOLDER,u.getPath());
		} catch (IOException e) {
			store.setDefault(PreferenceConstants.ROOT_FOLDER, Tools.getRootFolder());
		}
		
		store.setDefault(PreferenceConstants.ADDITIONAL_PLUGINS_FOLDERS, "");

		store.setDefault(PreferenceConstants.MAX_PROCESSORS, 1);

		/*		
		
		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.P_STRING,
				"Default value");
		 */
	}

}
