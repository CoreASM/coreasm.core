package org.coreasm.eclipse.editors;

import org.coreasm.eclipse.tools.ColorManager;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

/**
 * This class defines the rules and formatting of comments 
 * for the syntax highlighting. 
 * @author Markus Müller
 */
public class CommentScanner
extends RuleBasedScanner 
{
	public CommentScanner(ColorManager manager)
	{
		// Define a token with the formatting information
		Color color = ColorManager.getColor(IEditorColorConstants.ASM_COMMENT);
		TextAttribute textAttribute = new TextAttribute(color);
		IToken asmComment = new Token(textAttribute);
		
		// For comments, there are no rules, since every character which is
		// within a comment partition will be highlighted as comment.
		setDefaultReturnToken(asmComment);
		setRules(null);
	}
	
}
