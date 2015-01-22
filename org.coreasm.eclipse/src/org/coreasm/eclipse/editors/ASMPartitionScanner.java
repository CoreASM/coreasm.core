package org.coreasm.eclipse.editors;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * This class defines the rules for partitions of CoreASM specifications.
 * @author Markus Müller
 */
public class ASMPartitionScanner
extends RuleBasedPartitionScanner
{
	public final static String ASM_DEFAULT = ASMEditor.PARTITION_CODE;
	public final static String ASM_COMMENT = ASMEditor.PARTITION_COMMENT;
	
	public ASMPartitionScanner()
	{
		IToken asmComment = new Token(ASM_COMMENT);
		IToken asmDefault = new Token(ASM_DEFAULT);
		
		setDefaultReturnToken(asmDefault);
		
		IPredicateRule[] rules = new IPredicateRule[3];
		rules[0] = new MultiLineRule("/*", "*/", asmComment);
		rules[1] = new EndOfLineRule("//", asmComment);
		rules[2] = new ASMCodeRule(asmDefault);
		
		setPredicateRules(rules);
	}
	
	
}
