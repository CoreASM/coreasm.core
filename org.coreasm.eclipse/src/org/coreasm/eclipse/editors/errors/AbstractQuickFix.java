package org.coreasm.eclipse.editors.errors;

import java.util.LinkedList;
import java.util.List;

import org.coreasm.eclipse.editors.IconManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * This class is an abstract implementation of QuickFixes for an error.
 * @author Markus Müller
 */
public abstract class AbstractQuickFix
{
	/** The prompt string is shown in the hover window */
	public final String prompt;	
	
	/** A QuickFix can offer a list of choices, which are presented to the user
	 * as a list of alternatives. The QuickFix uses the chosen alternative as
	 * a parameter for its fixing. */
	public final List<String> choices;

	/**
	 * Generates a new QuickFix
	 * @param prompt	The prompt which is shown to the user in the hover window
	 * @param choices	The list of choices which is presented to the user in the
	 * 					hover window, or null if there are no choices for this QuickFix.
	 */
	public AbstractQuickFix(String prompt, List<String> choices) 
	{
		super();
		this.prompt = prompt;
		this.choices = new LinkedList<String>();
		if (choices != null)
			this.choices.addAll(choices);
	}

	/**
	 * Subclasses have to implement this method with the actual fixing code.
	 * @param error		The error which should be fixed with this QuickFix
	 * 					(containing error parameters like the offset and length etc)
	 * @param choice	The choice the user has chosen in the hover window.
	 */
	public abstract void fix(AbstractError error, String choice);

	/**
	 * Checks if the QuickFix can be applied for the given error.
	 * The default implementation of AbstractQuickFix always returns true.
	 * Subclasses must override this method if it depends on the error object if
	 * a QuickFix can be applied to the error.
	 */
	public boolean checkValidility(AbstractError error)
	{
		return true;
	}
	
	/**
	 * This method initializes the choices which are offered to the hover window.
	 * Subclasses which offer choices must override this method. The implementation
	 * of the method in this class does nothing, so the list of choices remains
	 * empty.
	 */
	public void initChoices(AbstractError error)
	{
		;
	}

	public abstract void collectProposals(AbstractError error, List<ICompletionProposal> proposals);

	/**
	 * General QuickFix for replacing the whole hover region with a given string
	 * or inserting the string at the offset of the hover region.
	 * 
	 * @author Markus Müller
	 */
	public static class QF_Replace
	extends AbstractQuickFix
	{
		private String insert;		// the string to be inserted, use "" for deletions
		private boolean replace;
		
		/**
		 * Generates a new instance of this class.
		 * @param prompt	The prompt which is shown in the hover window
		 * @param insert	The text which is inserted or which replaces the hovered text
		 * @param replace	Should the hovered text be replaced? If false, the insert
		 * 					string will be inserted right in front of the hover region.
		 */
		public QF_Replace(String prompt, String insert, boolean replace)
		{
			super(prompt, null);
			this.insert = insert;
			this.replace = replace;
		}

		/**
		 * Executes the QuickFix.
		 */
		@Override
		public void fix(AbstractError error, String choice)
		{
			int begin = error.getPosition();
			int len = 0;
			if (replace == true)
				len = error.getLength();
			try {
				error.getDocument().replace(begin, len, insert);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			int length = 0;
			if (replace)
				length = error.getLength();
			proposals.add(new CompletionProposal(insert, error.getPosition(), length, 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt, null, null));
		}
		
		/**
		 * Sets the text which is inserted or which should replace the present text.
		 */
		public void setInsert(String insert)
		{
			this.insert = insert;
		}
		
		/**
		 * Returns the text which is inserted or which should replace the present text.
		 */
		public String getInsert()
		{
			return insert;
		}
	}

	public static class QuickFixProposal implements ICompletionProposal {
		private AbstractQuickFix quickFix;
		private AbstractError error;
		private String choice;
		
		public QuickFixProposal(AbstractQuickFix quickFix, AbstractError error, String choice) {
			this.quickFix = quickFix;
			this.error = error;
			this.choice = choice;
		}

		@Override
		public void apply(IDocument document) {
			quickFix.fix(error, choice);
		}

		@Override
		public String getAdditionalProposalInfo() {
			return null;
		}

		@Override
		public IContextInformation getContextInformation() {
			return null;
		}

		@Override
		public String getDisplayString() {
			return quickFix.prompt.replaceFirst("@", "'" + choice + "'");
		}

		@Override
		public Image getImage() {
			return IconManager.getIcon("/icons/editor/bullet.gif");
		}

		@Override
		public Point getSelection(IDocument document) {
			return null;
		}
	}
}
