package org.coreasm.eclipse.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class ASMCodeRule implements IPredicateRule {

	IToken fToken;
	
	public ASMCodeRule(IToken t) {
		this.fToken=t;
	}

	public IToken getSuccessToken() {
		return fToken;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		IToken t=detectEndSequence(scanner);
//		int c=scanner.read();
//		System.out.println("ASMCodeRule ended on <"+(c==scanner.EOF?'$':(char)c)+">, result "+t);
//		scanner.unread();
		return t;
	}

	private IToken detectEndSequence(ICharacterScanner scanner) {
//		System.out.println("ASMCodeRule called from <"+(char)scanner.read()+">");
//		scanner.unread();
		int c,len=0;
//		char [][] eol=scanner.getLegalLineDelimiters();
		do {
			c=scanner.read(); len++;
			if (c==ICharacterScanner.EOF) {
				scanner.unread(); len--;
				return len>0?fToken:Token.EOF;
			}
			// TODO: check eol? Reduces the partition size, but may break multi-line statements processing
			if (c=='#') {
				scanner.unread(); len--;
				return len>0?fToken:Token.UNDEFINED;
			}
			if (c=='/') {
				int c2=scanner.read(); len++;
				if (c2==ICharacterScanner.EOF)
					return len>0?fToken:Token.UNDEFINED;
				else if ((c2=='/') || (c2=='*')) {
					scanner.unread(); len--;
					scanner.unread(); len--;
					return len>0?fToken:Token.UNDEFINED;
				} else {
					scanner.unread(); len--;
				}
			}
		} while (true);
	}

	public IToken evaluate(ICharacterScanner scanner) {
		return evaluate(scanner,false);
	}

}
