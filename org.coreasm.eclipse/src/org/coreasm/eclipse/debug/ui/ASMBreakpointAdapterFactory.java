package org.coreasm.eclipse.debug.ui;

import org.coreasm.eclipse.editors.ASMEditor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

/**
 * Creates a toggle breakpoint adapter
 * @author Michael Stegmaier
 *
 */
@SuppressWarnings("rawtypes")
public class ASMBreakpointAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ASMEditor) {
			ASMEditor editorPart = (ASMEditor) adaptableObject;
			IResource resource = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);
			if (resource != null && ("coreasm".equalsIgnoreCase(resource.getFileExtension()) || "casm".equalsIgnoreCase(resource.getFileExtension())))
				return new ASMBreakpointAdapter();
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IToggleBreakpointsTarget.class };
	}
}
