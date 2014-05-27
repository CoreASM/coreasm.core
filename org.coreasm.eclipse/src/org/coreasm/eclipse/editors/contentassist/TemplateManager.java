package org.coreasm.eclipse.editors.contentassist;

import java.io.IOException;

import org.coreasm.eclipse.CoreASMPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;

/**
 * @author Tobias
 * 
 * Singleton manager for templates
 */

public class TemplateManager {
	/** Key to store custom templates. */
	private static final String CUSTOM_TEMPLATES_KEY = CoreASMPlugin.getDefault()
			.toString() + ".customtemplates";
	
	/** The shared instance. */
	private static TemplateManager instance;
	
	/** The template store. */
	private TemplateStore fStore;
	
	/** The context type registry. */
	private ContributionContextTypeRegistry fRegistry;

	private TemplateManager() {
		
	}

	public static TemplateManager getInstance() {
		if (instance == null) {
			instance = new TemplateManager();
		}
		
		return instance;
	}

	/**
	 * Returns this plug-in's template store.
	 * 
	 * @return the template store of this plug-in instance
	 */
	public TemplateStore getTemplateStore() {

		if (fStore == null) {
			fStore = new ContributionTemplateStore(getContextTypeRegistry(),
					CoreASMPlugin.getDefault().getPreferenceStore(),
					CUSTOM_TEMPLATES_KEY);
			try {
				fStore.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fStore;
	}

	/**
	 * Returns this plug-in's context type registry.
	 * 
	 * @return the context type registry for this plug-in instance
	 */ 
	public ContextTypeRegistry getContextTypeRegistry() {
		if (fRegistry == null) {
			fRegistry = new ContributionContextTypeRegistry();
			fRegistry.addContextType(ASMTemplateContextType.CONTEXT_TYPE);
		} 
		
		return fRegistry;
	}

	/* Forward methods to CoreASMPlugin plugin instance */
	public ImageRegistry getImageRegistry() {
		return CoreASMPlugin.getDefault().getImageRegistry();
	}
	
	public static ImageDescriptor imageDescriptorFromPlugin(String string, String default_image) {
		return CoreASMPlugin.imageDescriptorFromPlugin(string, default_image);
	}
	
	public IPreferenceStore getPreferenceStore() {
		return CoreASMPlugin.getDefault().getPreferenceStore();
	}

	public void savePluginPreferences() {
		CoreASMPlugin.getDefault().savePluginPreferences();
	}
}

