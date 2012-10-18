package org.coreasm.eclipse.actions;


import java.util.Map;
import java.util.TreeSet;

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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * Shows a brief About window.
 * 
 */
public class CoreASMAboutAction implements IWorkbenchWindowActionDelegate {
	
	private static final String HTML_TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
	
	public static final String ABOUT_TEXT = 
		"The CoreASM Eclipse Plugin<p>" +
		"<font size='-1'>Copyright (c) 2005-2011<br>" +
		"Roozbeh Farahbod<br>" +
		"Vincenzo Gervasi<br>" +
		"George Ma<br>" +
		"Mashaal Memon<br>" +
		"<p>";
		

	private IWorkbenchWindow window;
    private boolean windowOpen;
    private Shell shell;
    private Display display;
	
    /**
	 * The constructor.
	 */
	public CoreASMAboutAction() {
        windowOpen = false;
    }

	public void run(IAction action) {
        if (!windowOpen) {
            display = Display.getCurrent();
            shell = new Shell (window.getShell(), SWT.SHELL_TRIM);
            shell.setLayout(new FillLayout());
            shell.setText("About CoreASM");
            String root = CoreASMPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.ROOT_FOLDER);
            try {
                shell.setImage(new Image(display,root+CoreASMPlugin.MAIN_ICON_PATH));
            }
            catch (Throwable e) {
                MessageDialog.openError(
                        window.getShell(),
                        "CoreASM Plug-in",
                        e.getMessage());
            }

            
            //ScrolledComposite c = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);            
            Browser browser = new Browser(shell, SWT.MULTI|SWT.WRAP|SWT.READ_ONLY);
            StringBuffer text = new StringBuffer("<HTML><HEAD><TITLE>About CoreASM</TITLE></HEAD><BODY>");

            text.append("<TABLE WIDTH=100% BORDER=0 CELLPADDING=4 CELLSPACING=0>");
            text.append("<TR><TD WIDTH=100 valign=top >");
            text.append("<img src=\"" + root + "icons/logo-96x.png\"/>");
            text.append("</TD><TD WIDTH='*' valign=top >");
            text.append("<p>&nbsp;<br>" + ABOUT_TEXT);
            
//            text.append("<p>&nbsp;</p>");
            
    		CoreASMGlobal.setRootFolder(CoreASMPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.ROOT_FOLDER));
    		CoreASMEngine engine = CoreASMEngineFactory.createCoreASMEngine();
    		engine.setClassLoader(this.getClass().getClassLoader());
    		engine.initialize();
    		engine.waitWhileBusy();
    		if (engine.getEngineMode() != EngineMode.emError) {
    			text.append("CoreASM Engine " + engine.getVersionInfo() + "<br>");
    			text.append("Available Plugins: <br>");
				Map<String,VersionInfo> list = engine.getPluginsVersionInfo();
				TreeSet<String> sortedSet = new TreeSet<String>();
				
				for (String name: list.keySet()) {
					VersionInfo vinfo = list.get(name);
					sortedSet.add(HTML_TAB + name + "&nbsp;" + (vinfo==null?"":vinfo.toString()));
				}
				for (String pinfo: sortedSet)
					text.append(pinfo + "<br>");
    		}
            engine.terminate();
    		
    		text.append("</font></TD></TR></TABLE>");
            browser.setText(text.toString() + "</BODY></HTML>");
            //.setText(text.toString());
           
            browser.setBackground(new Color(display,215,215,215));
            //c.setContent(browser);
            //c.setExpandHorizontal(true);
            //c.setExpandVertical(true);
            //c.setMinWidth(350);
            //c.setMinHeight(500);
            shell.setSize(380, 420);
            shell.setLocation(100,100);

            shell.open();
            windowOpen = true;
            while (!shell.isDisposed ()) {
                if (!display.readAndDispatch ()) display.sleep ();
            }
            windowOpen = false;
        }
        else {
            shell.setActive();
        }
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}