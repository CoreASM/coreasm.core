package org.coreasm.eclipse.editors.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.eclipse.CoreASMPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.Image;

/**
 * @author Tobias
 *
 * Main class which searches for templates that fit to the current context
 * e.g: typing "ru" returns templates like "rule"
 */
public class TemplateAssistProcessor extends TemplateCompletionProcessor {

	private static final String DEFAULT_IMAGE= "icons/editor/templateprop_co.gif"; 
	
	/**
	 * Helper function for computeCompletionProposals
	 */
	@Override
	protected String extractPrefix(ITextViewer viewer, int offset) {
		int i = offset;
		IDocument document = viewer.getDocument();
		if (i > document.getLength())
			return "";
		try {
			while (i > 0) {
				char ch = document.getChar(i - 1);
				if (!Character.isJavaIdentifierPart(ch))
					break;
				i--;
			}
			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return "";
		}
	}

	protected int getRelevance(Template template, String prefix) {
		if (template.getName().startsWith(prefix))
			return 90;
		return 0;
	}
	
	/**
	 * Returns all templates from the given context
	 * 
	 * @param contextTypeId the context type
	 * @return all templates
	 */
	protected Template[] getTemplates(String contextTypeId) {
		TemplateManager manager = TemplateManager.getInstance();
		return manager.getTemplateStore().getTemplates(contextTypeId);
	}

	/**
	 * Return the context type that is supported by this plug-in.
	 * 
	 * @param viewer the viewer, ignored in this implementation
	 * @param region the region, ignored in this implementation
	 * @return the supported Scenario context type
	 */
	protected TemplateContextType getContextType(ITextViewer viewer,
			IRegion region) {
		TemplateManager manager = TemplateManager.getInstance();
		return manager.getContextTypeRegistry().getContextType(
				ASMTemplateContextType.CONTEXT_TYPE);
	}

	/**
	 * Always return the default image.
	 * 
	 * @param template the template, ignored in this implementation
	 * @return the default template image
	 */
	protected Image getImage(Template template) {
		ImageRegistry registry= TemplateManager.getInstance().getImageRegistry();
		Image image= registry.get(DEFAULT_IMAGE);
		
		if (image == null) {
			ImageDescriptor desc = TemplateManager.imageDescriptorFromPlugin(CoreASMPlugin.PLUGIN_ID, DEFAULT_IMAGE); //$NON-NLS-1$
			registry.put(DEFAULT_IMAGE, desc);
			image= registry.get(DEFAULT_IMAGE);
		}
		
		return image;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		ITextSelection selection = (ITextSelection) viewer
				.getSelectionProvider().getSelection();
		
		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset = selection.getOffset() + selection.getLength();
		String prefix = extractPrefix(viewer, offset);
		Region region = new Region(offset - prefix.length(), prefix.length());
		TemplateContext context = createContext(viewer, region);
		
		if (context == null)
			return new ICompletionProposal[0];
		context.setVariable("selection", selection.getText()); // name of the selection variables {line, word_selection //$NON-NLS-1$
		
		Template[] templates = getTemplates(context.getContextType().getId());
		
		List<ICompletionProposal> matches = new ArrayList<ICompletionProposal>();
		for (int i = 0; i < templates.length; i++) {
			Template template = templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			if (!prefix.equals("") && prefix.charAt(0) == '<')
				prefix = prefix.substring(1);
			if (!prefix.equals("")
					&& (template.getName().startsWith(prefix) && template
							.matches(prefix, context.getContextType().getId())))
				matches.add(createProposal(template, context, (IRegion) region,
						getRelevance(template, prefix)));
		}
		
		return matches.toArray(new ICompletionProposal[matches.size()]);
	}
}
