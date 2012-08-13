/*
 * LaTeXFormatStringMapper.java 		$Revision: 80 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.latex;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.NodeToFormatStringMapper;
import org.coreasm.engine.plugins.string.StringNode;
import org.coreasm.util.Tools;

/**
 * Maps CoreASM abstract syntax tree nodes to a format string producing LaTeX output. 
 *   
 * @author Roozbeh Farahbod
 *
 */

public class LaTeXFormatStringMapper implements NodeToFormatStringMapper<Node> {

	public static final String SINGLE_LINE_COMMENT_REGEX = "^[/][/]"; 
	protected static final Pattern singleLineCommentPattern = Pattern.compile(SINGLE_LINE_COMMENT_REGEX, Pattern.MULTILINE);
	
	public static final String MULTILINE_COMMENT_REGEX = "[/][*]"; 
	protected static final Pattern multiLineCommentPattern = Pattern.compile(MULTILINE_COMMENT_REGEX, Pattern.MULTILINE);

	protected Set<String> knownIdentifiers = new HashSet<String>();
	
	public LaTeXFormatStringMapper() {}

	public LaTeXFormatStringMapper(Set<String> knownIds) {
		if (knownIds != null)
			knownIdentifiers = new HashSet<String>(knownIds);
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.interpreter.NodeToFormatStringMapper#getFormatString(org.coreasm.engine.interpreter.Node)
	 */
	public String getFormatString(Node node) {
		if (node.getConcreteNodeType().equals(Node.KEYWORD_NODE))
			return " \\clkeyword{%s} ";
		else 
		if (node.getConcreteNodeType().equals(Node.DELIMITER_NODE)) 
			return delimiterFilter(node.unparse());
		else
		if (isKnownId(node))
			return " \\clfn{%s}";
		else
		if (node.getConcreteNodeType().equals(Node.GENERAL_ID_NODE)) 
			return "%s";
		else
		if (node instanceof StringNode)
			return " \\clstr{" + removeBackslashes(node.unparse()) + "} ";
		else
		if (isBinaryOperator(node) || isUnaryOperator(node)) {
			if (isAlphabeticOperator(node))
				return " \\clkeyword{%s} ";
			else
				return " " + removeBackslashes(node.unparse()) + " ";
		}
		else
		if (isKnownId(node))
			return " \\clfn{%s} ";
		else
		if (node.getConcreteNodeType().equals(Node.OPERATOR_NODE)) 
			return node.unparse().replace("{", "\\{").replace("}", "\\}");
		else {
			String result = "%s";
			// concatenate all the string representation of 
			// the node itself and all its children 
			for (int i=0; i < node.getNumberOfChildren(); i++)
				result = result + "%s";
			return result;
		}
	}

	protected String removeBackslashes(String str) {
		return str.replace("\\", "\\backslash");
	}
	
	protected String delimiterFilter(String delim) {
		String result = removeBackslashes(delim).replace(Tools.getEOL(), "\\\\"+Tools.getEOL()	).replace("  ", "\\cldblsp ").replace("\t", "\\cltb ");
		Matcher matcher = singleLineCommentPattern.matcher(result);
		if (matcher.find())
			result = "\\clcomment{" + result + "} "; 
		else {
			matcher = multiLineCommentPattern.matcher(result);
			if (matcher.find())
				result = "\\clcomment{" + result + "} ";
		}
		return result;
	}
	
	protected boolean isBinaryOperator(Node node) {
		if (node.getFirstCSTNode() == null && node.getParent() != null)
			if (((ASTNode)node.getParent()).getGrammarClass().equals(ASTNode.BINARY_OPERATOR_CLASS))
				if (node.getToken().equals(node.getParent().getToken()))
					return true;
		return false;
	}

	protected boolean isUnaryOperator(Node node) {
		if (node.getFirstCSTNode() == null && node.getParent() != null)
			if (((ASTNode)node.getParent()).getGrammarClass().equals(ASTNode.UNARY_OPERATOR_CLASS))
				if (node.getToken().equals(node.getParent().getToken()))
					return true;
		return false;
	}
	
	protected boolean isAlphabeticOperator(Node node) {
		if (node.getToken() != null)
			return Character.isLetter(node.getToken().charAt(0));
		else
			return false;
	}	
	
	protected boolean isKnownId(Node node) {
		if (node.getConcreteNodeType().equals(Node.GENERAL_ID_NODE) && node.getToken() != null)
			return knownIdentifiers.contains(node.getToken());
		else
			return false;
	}
	
}
