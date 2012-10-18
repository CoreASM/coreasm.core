/**
 * 
 */
package org.coreasm.eclipse.engine;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.preferences.PreferenceConstants;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.EngineProperties;
import org.coreasm.util.CoreASMGlobal;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * A CoreASM Engine Factory for the Eclipse plugin.
 * 
 * @author Roozbeh Farahbod
 *
 */
public class CoreASMEngineFactory {

	/**
	 * Creates an instance of CoreASM Engine configured for the 
	 * CoreASM Eclipse plugin.
	 */
	public static CoreASMEngine createCoreASMEngine() {
		CoreASMEngine engine = null;
		CoreASMGlobal.setRootFolder(CoreASMPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.ROOT_FOLDER));
		engine = org.coreasm.engine.CoreASMEngineFactory.createEngine();
		setEngineProperties(engine);
		engine.setClassLoader(CoreASMEngineFactory.class.getClassLoader());
		engine.initialize();
		engine.waitWhileBusy();
		return engine;
	}
	
	private static void setEngineProperties(CoreASMEngine engine) {
		IPreferenceStore prefStore = CoreASMPlugin.getDefault().getPreferenceStore();
		
		engine.setProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY, 
				prefStore.getString(PreferenceConstants.ADDITIONAL_PLUGINS_FOLDERS));
	}

}
