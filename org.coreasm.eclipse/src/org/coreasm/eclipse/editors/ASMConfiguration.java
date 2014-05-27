package org.coreasm.eclipse.editors;


import org.coreasm.eclipse.editors.contentassist.TemplateAssistProcessor;
import org.coreasm.eclipse.editors.hover.ASMTextHover;
import org.coreasm.eclipse.editors.quickfix.ASMQuickAssistProcessor;
import org.coreasm.eclipse.tools.ColorManager;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * This class contains the configuration for the SourceViewer of the ASMEditor.
 * @author	Markus Müller
 * @see		org.eclipse.jface.text.source.SourceViewer
 * @see		org.eclipse.jface.text.source.SourceViewerConfiguration
 */
public class ASMConfiguration
extends TextSourceViewerConfiguration
{
	private ASMEditor editor;
	private ColorManager colorManager;

	// scanner objects for syntax highlighting
	private CommentScanner commentScanner;
	private KeywordScanner keywordScanner;
	
	public ASMConfiguration(ASMEditor editor, ColorManager colorManager)
	{
		this.editor = editor;
		this.colorManager = colorManager;
	}
	
	/**
	 * Returns the content types a CoreASM specification can consist of. There are
	 * two content types: code and comments
	 * 
	 * @return	a String array containing the tags of the content types.
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
	{
		return new String[]
		{
			ASMPartitionScanner.ASM_DEFAULT,
			ASMPartitionScanner.ASM_COMMENT
		};
	}
	
	/**
	 * @return	Returns the content assist which is used for the templates
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();

		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new TemplateAssistProcessor(), ASMEditor.PARTITION_CODE);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);

		return assistant;
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
	{
		PresentationReconciler reconciler = new PresentationReconciler();
		
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getASMCommentScanner());
		reconciler.setDamager(dr, ASMPartitionScanner.ASM_COMMENT);
		reconciler.setRepairer(dr, ASMPartitionScanner.ASM_COMMENT);
		
		dr = new DefaultDamagerRepairer(getASMKeywordScanner());
		reconciler.setDamager(dr, ASMPartitionScanner.ASM_DEFAULT);
		reconciler.setRepairer(dr, ASMPartitionScanner.ASM_DEFAULT);
		
		return reconciler;
	}
	
	/**
	 * Creates and returns the CommentScanner, which recognizes comments for the
	 * syntax highlighting and configures their formatting.
	 */
	protected CommentScanner getASMCommentScanner()
	{
		if (commentScanner == null)
		{
			commentScanner = new CommentScanner(colorManager);
			commentScanner.setDefaultReturnToken(new Token(new TextAttribute(ColorManager.getColor(IEditorColorConstants.ASM_COMMENT))));
		}
		return commentScanner;
	}
	

	/**
	 * Creates and returns the ASMKeywordScanner, which recognizes keywords for the
	 * syntax highlighting and configures their formatting.
	 */
	protected KeywordScanner getASMKeywordScanner()
	{
		if (keywordScanner == null)
		{
			keywordScanner = new KeywordScanner(colorManager);
			keywordScanner.setDefaultReturnToken(new Token(new TextAttribute(ColorManager.getColor(IEditorColorConstants.DEFAULT))));
		}
		return keywordScanner;
	}
	
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
	{
		return new DefaultAnnotationHover();
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
	{
		return new ASMTextHover(editor);
	}
	
	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		IQuickAssistAssistant assistant = new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new ASMQuickAssistProcessor());
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return assistant;
	}
}
