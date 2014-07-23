package org.coreasm.eclipse.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
	    defineLayout(layout);
	}

	private void defineLayout(IPageLayout layout) {
		// Add "new wizards".
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
        layout.addNewWizardShortcut("org.coreasm.eclipse.newFileWizard");

        layout.createFolder("left", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
        layout.createFolder("right", IPageLayout.RIGHT, 0.8f, IPageLayout.ID_EDITOR_AREA);
        layout.createFolder("bottom", IPageLayout.BOTTOM, 0.8f, IPageLayout.ID_EDITOR_AREA);
        layout.createFolder("top", IPageLayout.TOP, 0.2f, IPageLayout.ID_EDITOR_AREA);
        layout.createFolder("left_bottom", IPageLayout.BOTTOM, 0.7f, "left");
        layout.createFolder("right_bottom", IPageLayout.BOTTOM, 0.7f, "right");
	}
}
