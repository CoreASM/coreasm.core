package org.coreasm.eclipse.editors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class KeywordScanner
extends RuleBasedScanner 
{
	// formatting tokens
	IToken asmKeyword;
	IToken asmID;
	IToken asmString;
	
	// Sets containing keywords and IDs
	Set<String> currentKeywords;
	Set<String> currentIDs;
	
	IRule[] rules;
	WordDetector wordDetector;
	
	/**
	 * This class defines the rules and formatting of keywords, IDs and strings 
	 * for the syntax highlighting. 
	 * @author Markus M�ller
	 */
	public KeywordScanner()
	{
		super();
		
		// create formatting token for keywords
		Color colorKeyword = new Color(Display.getCurrent(), IEditorColorConstants.KEYWORD);
		TextAttribute attrKeyword = new TextAttribute(colorKeyword, null, SWT.BOLD);
		asmKeyword = new Token(attrKeyword);
		
		// create formatting token for IDs
		Color colorID = new Color(Display.getCurrent(), IEditorColorConstants.PLUGIN_DEFINED_IDS);
		TextAttribute attrID = new TextAttribute(colorID, null, SWT.ITALIC);
		asmID = new Token(attrID);
		
		// create formatting token for strings
		Color colorString = new Color(Display.getCurrent(), IEditorColorConstants.STRING);
		TextAttribute attrString = new TextAttribute(colorString);
		asmString = new Token(attrString);
		
		// set up rules for keywords, IDs and strings
		rules = new IRule[4];
		wordDetector = new WordDetector();
		
		// initialize the keyword & ID sets with empty sets
		init(new HashSet<String>(), new HashSet<String>());
	}
	
	/**
	 * Initializes this instance with new sets of keywords and IDs
	 */
	public void init(Set<String> keywords, Set<String> ids)
	{
		setRules(new IRule[0]);
		
		currentKeywords = keywords;
		currentIDs = ids;
		
		WordRule wordRule = new WordRule(wordDetector);
		for (String keyword: keywords)
			wordRule.addWord(keyword, asmKeyword);
		for (String id: ids)
			wordRule.addWord(id, asmID);

		// Add rule for double quotes
		rules[0] = new SingleLineRule("\"", "\"", asmString, '\\');
		// Add a rule for single quotes
		rules[1] = new SingleLineRule("'", "'", asmString, '\\');

		rules[2] = wordRule;
		rules[3] = new TokenSkipRule(wordDetector);
		setRules(rules);
	}
	
	
	
	private class WordDetector
	implements IWordDetector
	{
		@Override
		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}
	}
	
}
