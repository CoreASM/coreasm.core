package org.coreasm.eclipse.editors;


import org.coreasm.eclipse.editors.hovering.ASMInformationControl;
import org.coreasm.eclipse.editors.hovering.ASMTextHover;
import org.coreasm.eclipse.tools.ColorManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;

/**
 * This class contains the configuration for the SourceViewer of the ASMEditor.
 * @author	Markus Müller
 * @see		org.eclipse.jface.text.source.SourceViewer
 * @see		org.eclipse.jface.text.source.SourceViewerConfiguration
 */
public class ASMConfiguration
extends SourceViewerConfiguration
{
	//private ASMDocumentProvider documentProvider;
	private ColorManager colorManager;

	// scanner objects for syntax highlighting
	private CommentScanner commentScanner;
	private KeywordScanner keywordScanner;
	
	// factory object for hover windows
	private IInformationControlCreator fInformationControlCreator = null;
	
	public ASMConfiguration(ColorManager colorManager)
	{
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
		return new ASMTextHover();
	}
	
	@Override
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer)
	{
		if (fInformationControlCreator == null) {
			fInformationControlCreator = new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new ASMInformationControl(parent);
				}
			};
		}
		return fInformationControlCreator;
	}
	
	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		IQuickAssistAssistant assistant = new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new ASMQuickAssistProcessor());
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return assistant;
	}
}
