/*	
 * OperatorRule.java 	1.0 	$Revision: 243 $
 * 
 * Copyright (C) 2005 Mashaal Memon
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.parser;

/** 
 * This class is used to define operator rules provided by plugins to extend the language.
 * <p>&nbsp;<p>
 * A guideling on precedence level of different classes of operators:
 * <p>
 * Precedence, Operator Class<br>
 * --------------------------<br>
 * 900,	postfix operators<br>
 * 850,	unary operators<br>
 * 800,	multiplicative<br>
 * 750, additive<br>
 * 650,	relational<br>
 * 600,	equality<br>
 * 550,	bitwise AND<br>
 * 500,	bitwise XOR<br>
 * 450,	bitwise OR<br>
 * 400,	logical AND<br>
 * 350,	logical OR<br>
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public class OperatorRule {

	/** Operator types. */
	public enum OpType {INFIX_LEFT, INFIX_NON, INFIX_RIGHT, PREFIX, POSTFIX, INDEX, TERNARY, CLOSED
  }
    
    public static final String OPERATOR_DELIMITER = " ";
	
    /** every operator has at least one (group of) operator symbol */
	public final String opr;
	
	/** some operators have two (groups of) operator symbols */
	public final String opr2;
	
	/** the type of the operator (unary, binary, etc.) */
	public final OpType type;
	
	/** the precedence level of the operator (0 to 1000). 1000 is the highest priority. */
	public final int precedence; 

	/** the name of the plugin that contributes this operator */
	public final String contributor; 
	
	//private final OpAssoc assoc; // the associativity of the operator
	
	/**
	 * Creates a new <code>OperatorRule</code> given 
	 * the operator symbol(s), operator precedence (0..1000) with 1000 being the highest,
	 * the type(unary, bunary, etc.), the precedence, associativity, and the
	 * contributor of the operator
	 * 
	 * @param op a <code>String</code> representing the (group of) symbol(s) for this operator.
	 * @param op2 a <code>String</code> representing the second (group of) symbol(s) for this operator.
	 * @param type an <code>OpType</code> representing the type of operator
	 * @param prec an <code>int</code> value representing the precedence of this operator (from 0 to 1000, 1000 being the highest priority)
	 * @param contributor the name of the contributor of this operator rule (i.e. "kernel" or a plugin);
	 */
	public OperatorRule(String op, String op2, OpType type, int prec, String contributor) {
		super();

		this.opr = op;
		this.opr2 = op2;
		this.type = type;
		if (prec < 0) prec = 0;
		if (prec > 1000) prec = 1000;
		this.precedence = prec;
		this.contributor = contributor;
	}
	
	/**
	 * Creates a new <code>OperatorRule</code> given 
	 * the operator symbol(s), operator precedence (0..1000) with 1000 being the highest,
	 * the type(unary, bunary, etc.), the precedence, associativity, and the
	 * contributor of the operator
	 * 
	 * @param op a <code>String</code> representing the (group of) symbol(s) for this operator.
	 * @param type an <code>OpType</code> representing the type of operator
	 * @param prec an <code>int</code> value representing the precedence of this operator (from 0 to 1000, 1000 being the highest priority)
	 * @param contributor the name of the contributor of this operator rule (i.e. "kernel" or a plugin);
	 * 
	 * @see org.coreasm.engine.parser.OperatorRule#OperatorRule(String, org.coreasm.engine.parser.OperatorRule.OpType, int, String)
	 */
	public OperatorRule(String op, OpType type, int prec, String contributor) {
		this(op, null, type, prec, contributor);
	}
	
	/**
	 * Get first (group of) operator symbol(s).
	 *
	 * @return operator symbol(s) as a <code>String</code>
	 */
	public String getOp()
	{
			return opr;
	}
	
  /**
   * Get second (group of) operator symbol(s).
   *
   * @return operator symbol(s) as a <code>String</code>
   */
  public String getOp2()
  {
    return opr2;
  }

	/**
	 * Get precedence of operator.
	 *
	 * @return precedence as a <code>double</code>
	 */
	public double getPrec()
	{
			return precedence;
	}
	
	/**
	 * Get type of operator.
	 *
	 * @return type as a <code>OpType</code>
	 */
	public OpType getType()
	{
			return type;
	}
	
	/**
	 * Get contributor of grammar rule.
	 *
	 * @return contributor value as <code>String</code>
	 */
	public String getContributor()
	{
			return contributor;
	}

	/**
	 * @return combined operator token for this opartor (to be used as a key)
	 */
	public String getOprToken() {
		if (opr2 == null)
			return opr;
		else
			return opr + OPERATOR_DELIMITER + opr2;
	}
}
