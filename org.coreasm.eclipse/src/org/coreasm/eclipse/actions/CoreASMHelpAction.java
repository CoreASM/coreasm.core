package org.coreasm.eclipse.actions;

import java.io.BufferedReader;
import java.io.FileReader;

import org.coreasm.eclipse.CoreASMPlugin;
import org.coreasm.eclipse.preferences.PreferenceConstants;
import org.coreasm.util.Tools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


/**
 * Shows a brief help window.
 * 
 */
public class CoreASMHelpAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
    private boolean windowOpen;
    private Shell shell;
    private Display display;
	/**
	 * The constructor.
	 */
	public CoreASMHelpAction() {
        windowOpen = false;
    }

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
        if (!windowOpen) {
            display = Display.getCurrent();
            shell = new Shell (window.getShell(),SWT.SHELL_TRIM);
            shell.setLayout(new FillLayout());
            shell.setText("CoreASM Help");
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

//            Browser browser = new Browser(shell,SWT.SHELL_TRIM);
//            browser.setText("<HTML><HEAD><TITLE>CoreASM Help</TITLE></HEAD><BODY>KLF</BODY></HTML>");
            
            ScrolledComposite c = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);            
            Text t = new Text(c, SWT.MULTI|SWT.WRAP|SWT.READ_ONLY);
            StringBuffer text = new StringBuffer();
            
            try {
                BufferedReader reader = new BufferedReader(new FileReader(root+"data/CoreASMHelp.dat"));
                String line = reader.readLine();
                while (line!=null) {
                    text.append(line + Tools.getEOL());
                    line = reader.readLine();
                }
                reader.close();
            }
            catch (Throwable e) {
                MessageDialog.openError(
                        window.getShell(),
                        "CoreASM Plug-in",
                        e.getMessage());
            }
            t.setText(text.toString());
           
            t.setBackground(new Color(display,255,255,255));
            c.setContent(t);
            c.setExpandHorizontal(true);
            c.setExpandVertical(true);
            c.setMinWidth(400);
            c.setMinHeight(1800);
            shell.setSize(600, 400);
            shell.setLocation(200,200);

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