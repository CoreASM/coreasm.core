package org.coreasm.eclipse.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.engine.CoreASMEngineFactory;
import org.coreasm.eclipse.preferences.PreferenceConstants;
import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.util.CoreASMGlobal;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Version;

/**
 * Shows a brief About window.
 * 
 */
public class CoreASMAboutAction implements IWorkbenchWindowActionDelegate {

	public static final Version version = ((BundleReference) org.coreasm.eclipse.CoreASMPlugin.class.getClassLoader())
			.getBundle().getVersion();

	public static final String ABOUT_TEXT = "Version " + version.toString().split(".qualifier")[0]
			+ "\nCopyright (c) 2005-2014\n\n" + "www.github.com/CoreASM\n" + "www.coreasm.org";

	public static final String DEVELOPERS = "Marcel Dausend\n" + "Roozbeh Farahbod\n" + "Vincenzo Gervasi\n"
			+ "George Ma\n" + "Mashaal Memon\n" + "Markus Müller\n" + "Michael Stegmaier";

	public static final String FOOTER = "visit us at <A>www.github.com/CoreASM/coreasm.core</A>";

	public static final String ACKNOWLEDGEMENTS = "Thanks to the ASM community, especially to Alexander\n"
			+ "Raschke, Helmuth Partsch, Uwe Glässer, and Egon Börger\n"
			+ "for supporting our work and encouraging others to use\n" + "CoreASM.";

	public static Map<String, String> plugins;

	private IWorkbenchWindow window;
	private boolean windowOpen;
	private Shell shell;
	private Display display;

	private void setFont(Label label, int size, int fontStyle) {
		FontData fontData = label.getFont().getFontData()[0];
		Font font = new Font(display, new FontData(fontData.getName(), size, fontStyle));
		label.setFont(font);
	}

	/**
	 * The constructor.
	 */
	public CoreASMAboutAction() {
		windowOpen = false;
	}

	public static final int SHELL_TRIM = SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE;

	private Shell createShell(Display display) {
		final Shell shell = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE));
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		String root = CoreASMPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.ROOT_FOLDER);
		try {
			shell.setImage(new Image(display, root + CoreASMPlugin.MAIN_ICON_PATH));
		} catch (Throwable e) {
			MessageDialog.openError(window.getShell(), "CoreASM Plug-in", e.getMessage());
		}

		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBackground(new Color(display, new RGB(255, 255, 255)));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setBackground(new Color(display, new RGB(255, 255, 255)));
		composite.setLayout(new GridLayout(1, true));

		Composite comp_north = new Composite(composite, SWT.NONE);
		comp_north.setBackground(new Color(display, new RGB(255, 255, 255)));
		// comp_north.setLayoutData(BorderLayout.NORTH);
		GridLayout gl_comp_north = new GridLayout(3, false);
		gl_comp_north.horizontalSpacing = 25;
		comp_north.setLayout(gl_comp_north);

		Label lblCoreasmAbout = new Label(comp_north, SWT.NONE);
		setFont(lblCoreasmAbout, 9, SWT.BOLD);
		lblCoreasmAbout.setBackground(new Color(display, new RGB(255, 255, 255)));
		lblCoreasmAbout.setText("The CoreASM Eclipe Plugin");

		new Label(comp_north, SWT.NONE);

		Label lblCoreasmDevelopers = new Label(comp_north, SWT.NONE);
		setFont(lblCoreasmDevelopers, 9, SWT.BOLD);
		lblCoreasmDevelopers.setBackground(new Color(display, new RGB(255, 255, 255)));
		lblCoreasmDevelopers.setText("CoreASM Developers");

		Label aboutLabel = new Label(comp_north, SWT.NONE);
		aboutLabel.setBackground(new Color(display, new RGB(255, 255, 255)));
		aboutLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		aboutLabel.setText(ABOUT_TEXT);
		new Label(comp_north, SWT.NONE);

		Label developerLabel = new Label(comp_north, SWT.NONE);
		developerLabel.setBackground(new Color(display, new RGB(255, 255, 255)));
		developerLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		developerLabel.setText(DEVELOPERS);

		Label label = new Label(comp_north, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.FILL);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

		Composite pluginTable = new Composite(composite, SWT.NONE);
		pluginTable.setBackground(new Color(display, new RGB(255, 255, 255)));
		GridLayout gl_comp_center = new GridLayout(3, false);
		gl_comp_center.verticalSpacing = 3;
		gl_comp_center.horizontalSpacing = 25;
		pluginTable.setLayout(gl_comp_center);

		Label lblAvailPlugins = new Label(pluginTable, SWT.NONE);
		lblAvailPlugins.setBackground(new Color(display, new RGB(255, 255, 255)));
		lblAvailPlugins.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblAvailPlugins.setText("available plugins");

		Label lblPluginName = new Label(pluginTable, SWT.NONE);
		setFont(lblPluginName, 9, SWT.BOLD);
		lblPluginName.setBackground(new Color(display, new RGB(255, 255, 255)));
		lblPluginName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPluginName.setText("plugin name");
		new Label(pluginTable, SWT.NONE);

		Label lblVersion = new Label(pluginTable, SWT.NONE);
		setFont(lblVersion, 9, SWT.BOLD);
		lblVersion.setBackground(new Color(display, new RGB(255, 255, 255)));
		lblVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("version");

		for (java.util.Map.Entry<String, String> entry : plugins.entrySet()) {
			Label pluginName = new Label(pluginTable, SWT.NONE);
			pluginName.setText(entry.getKey());
			if (entry.getKey().equals("CoreASM Engine"))
				setFont(pluginName, 9, SWT.ITALIC);
			pluginName.setBackground(new Color(display, new RGB(255, 255, 255)));
			new Label(pluginTable, SWT.NONE);
			Label pluginVersion = new Label(pluginTable, SWT.NONE);
			pluginVersion.setText(entry.getValue());
			if (entry.getKey().equals("CoreASM Engine"))
				setFont(pluginVersion, 9, SWT.ITALIC);
			pluginVersion.setBackground(new Color(display, new RGB(255, 255, 255)));
		}
		Label spacer = new Label(composite, SWT.NONE);
		spacer.setBackground(new Color(display, new RGB(255, 255, 255)));

		Label footerHeader = new Label(composite, SWT.NONE);
		footerHeader.setText("Acknowledgements");
		footerHeader.setBackground(new Color(display, new RGB(255, 255, 255)));
		setFont(footerHeader, 9, SWT.BOLD);

		Label footerLabel = new Label(composite, SWT.NONE);
		footerLabel.setText(ACKNOWLEDGEMENTS);
		footerLabel.setSize(350, 50);
		footerLabel.setBackground(new Color(display, new RGB(255, 255, 255)));

		Label spacer2 = new Label(composite, SWT.NONE);
		spacer2.setBackground(new Color(display, new RGB(255, 255, 255)));
		spacer2.setLayoutData(new GridData(SWT.NONE, SWT.NONE, true, true, 3, 1));

		Link link = new Link(composite, SWT.NONE);
		link.setBackground(new Color(display, new RGB(255, 255, 255)));
		link.setText(FOOTER);

		shell.setText("About CoreASM");
		shell.setSize(375, 550);

		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		return shell;

	}

	public void run(IAction action) {
		if (!windowOpen) {
			display = Display.getCurrent();

			// collect content for plugin list
			if (plugins == null) {
				CoreASMGlobal.setRootFolder(CoreASMPlugin.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.ROOT_FOLDER));
				CoreASMEngine engine = CoreASMEngineFactory.createCoreASMEngine();
				engine.setClassLoader(this.getClass().getClassLoader());
				engine.initialize();
				engine.waitWhileBusy();

				plugins = new HashMap<String, String>();
				if (engine.getEngineMode() != EngineMode.emError) {

					Map<String, String> pluginList = new HashMap<String, String>();
					Map<String, VersionInfo> list = engine.getPluginsVersionInfo();
					list.put("CoreASM Engine", engine.getVersionInfo());
					for (Entry<String, VersionInfo> plugin : list.entrySet()) {
						pluginList.put(plugin.getKey(), plugin.getValue().toString());
					}
					plugins = new TreeMap<String, String>(pluginList);
				}
				engine.terminate();
			}
			shell = createShell(display);
			shell.open();
			shell.layout();
			windowOpen = true;
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			windowOpen = false;
		} else {
			shell.setActive();
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}