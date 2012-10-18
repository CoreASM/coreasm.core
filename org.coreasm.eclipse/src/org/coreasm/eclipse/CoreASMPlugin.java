package org.coreasm.eclipse;


import org.coreasm.eclipse.preferences.PreferenceConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CoreASMPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.coreasm.eclipse";
	
	public static final String MAIN_ICON_PATH = "icons/icon16x16.gif";

	public static final String COREASM_FILE_EXT_1 = "casm";
	public static final String COREASM_FILE_EXT_2 = "coreasm";
	public static final String[] COREASM_FILE_EXTS = {COREASM_FILE_EXT_1, COREASM_FILE_EXT_2};

	// private static final String PROPERTIES_FILE_NAME = "coreasmEclipsePlugin.plist"; 
	
	private Shell shell = null;
	
	//The shared instance.
	private static CoreASMPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public CoreASMPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static CoreASMPlugin getDefault() {
		return plugin;
	}

	public Shell getShell() {
		IWorkbenchWindow window = this.getWorkbench().getActiveWorkbenchWindow();
		if (window != null && window.getShell() != null) {
			shell = window.getShell();
		} else 
			if (shell == null)
				shell = new Shell();
		return shell;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns the root folder of the plugin.
	 */
	public String getRootFolder() {
		return getDefault().getPreferenceStore().getString(PreferenceConstants.ROOT_FOLDER);
	}
	
	/*
	public void saveProperties() {
		File file = new File(getRootFolder() + PROPERTIES_FILE_NAME);
		try {
			properties.store(new FileOutputStream(file), "CoreASM Eclipse plugin properties");
		} catch (Exception e) {
			System.err.println("WARNING: CoreASM Eclipse plugin cannot save its properties file.\n  - cause: " + e.getMessage());
		}
	}
	
	private void loadProperties() {
		File file = new File(getRootFolder() + PROPERTIES_FILE_NAME);
		if (file.exists()) {
			try {
				properties.load(new FileInputStream(file));
				return;
			} catch (Exception e) {
				System.err.println("WARNING: CoreASM Eclipse plugin cannot load its properties file.\n  - cause: " + e.getMessage());
			}
		}
		loadDefaultProperties();
		saveProperties();
	}
	
	private void loadDefaultProperties() {
		properties.clear();
		properties.put(ADDITIONAL_PLUGINS_FOLDERS_PROPERTY, "");
	}
	*/
	
}
