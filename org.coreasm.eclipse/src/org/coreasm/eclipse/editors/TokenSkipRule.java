package org.coreasm.eclipse.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

public class TokenSkipRule implements IRule {

	IWordDetector wd;
	
	public TokenSkipRule(IWordDetector wd) {
		super();
		this.wd=wd;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int c;
		do {
			c=scanner.read();
			if (c==ICharacterScanner.EOF)
				return Token.EOF;
		} while (wd.isWordPart((char)c));
		scanner.unread();
		return Token.UNDEFINED;
	}

}
