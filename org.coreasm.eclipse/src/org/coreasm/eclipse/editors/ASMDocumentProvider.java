package org.coreasm.eclipse.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 * This class is a DocumentProvider for ASMDocuments stored as files.
 */
public class ASMDocumentProvider
extends FileDocumentProvider
{
	ASMEditor parentEditor;
	
	public ASMDocumentProvider(ASMEditor parentEditor) 
	{
		super();
		this.parentEditor = parentEditor;
	}

	@Override
	protected IDocument createDocument(Object element)
	throws CoreException
	{
		// The super constructor calls createEmptyDocument() which is overridden
		// below to create a new ASMDocument.
		IDocument document = super.createDocument(element);
		
		if (document != null)
		{
			// Set up the document partitioning
			IDocumentPartitioner partitioner = new FastPartitioner(
					new ASMPartitionScanner(),
					new String [] {
						ASMPartitionScanner.ASM_DEFAULT,
						ASMPartitionScanner.ASM_COMMENT
					});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			
			document.addDocumentListener(parentEditor);
		}

		return document;
	}

	/**
	 * This method is overridden to create documents of class ASMDocument instead
	 * of the default class Document.
	 */
	@Override
	protected IDocument createEmptyDocument()
	{
		return new ASMDocument();
	}
	
	


}
