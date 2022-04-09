/*	
 * StringPlugin.java 	1.0 	$Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $ by $Author: rfarahbod $
 *
 * Copyright (C) 2006 Mashaal Memon
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Terminals;
import org.jparsec.Token;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.string.CompilerStringPlugin;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;

/** 
 * Plugin for string related literals, operations, and functions.
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public class StringPlugin extends Plugin 
		implements ParserPlugin, InterpreterPlugin, OperatorProvider, VocabularyExtender {
	
	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 4, 1, "");
	
	public static final String STRING_TOKEN = "STRING";
	
	public static final String STRINGCONCAT_OP = "+";

	public static final String PLUGIN_NAME = StringPlugin.class.getSimpleName();

	// TODO why do we have this as a static field?
	// public static StringBackgroundElement STRING_BACKGROUND_ELEMENT;
	
	private StringBackgroundElement stringBackgroundElement;

	private Map<String,FunctionElement> funcs = null;
	private Map<String,BackgroundElement> backgroundElements = null;
	
	private Map<String, GrammarRule> parsers = null;
	private Set<Parser<? extends Object>> lexers = null;
	
	//private final Parser<Node>[] stringTermParserArray = new Parser[1];
	//private final Parser<Node> stringTermParser = ParserTools.lazy("StringTerm", stringTermParserArray);
	Parser.Reference<Node> refStringTermParser = Parser.newReference();
	
	Parser<String> tokenizer_str = null;
	
	private final String[] keywords = {};
	private final String[] operators = {"+"};
	
	private CompilerPlugin compilerPlugin = new CompilerStringPlugin(this);
	
	@Override
	public CompilerPlugin getCompilerPlugin(){
		return compilerPlugin;
	}
	
	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}

	/* (non-Javadoc)
	 * @see org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.Node)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) {
		
		ASTNode nextPos = pos;
		String x = pos.getToken();
		String gClass = pos.getGrammarClass();
        
		// if number related expression
		if (gClass.equals(ASTNode.EXPRESSION_CLASS))
		{
			// it is a number constant
			if (x != null) {
				
				// get new string element representing lexeme from string background
				StringElement se = stringBackgroundElement.getNewValue(x);
					
				// result of this node is the string element produced
				pos.setNode(null,null,se);
	        	}
		}
		
		return nextPos;
	}

	public Set<Parser<? extends Object>> getLexers() {
		if (lexers == null) {
			lexers = new HashSet<Parser<? extends Object>>();

			tokenizer_str = Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER;
			lexers.add(tokenizer_str);
		}
		return lexers;
	}

	/*
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		if (nonterminal.equals("StringTerm")) 
			return refStringTermParser.lazy();
		else
			return null;
	}


	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			//org.coreasm.engine.parser.Parser parser = capi.getParser();
			parsers = new HashMap<String, GrammarRule>();
			
			Parser<Node> stringParser = Terminals.StringLiteral.PARSER.token().map(
					from -> new StringNode(StringElement.processEscapeCharacters(from.toString()), new ScannerInfo(from))
			);
			
			refStringTermParser.set(stringParser);
			parsers.put("ConstantTerm", 
					new GrammarRule("StringConstantTerm", "STRING", refStringTermParser.lazy(), PLUGIN_NAME));

		}
		
		return parsers;
	}
	
	/* (non-Javadoc)
	 * @see org.coreasm.engine.Plugin#initialize()
	 */
	@Override
	public void initialize() {
	}

	//--------------------------------
	// Vocabulary Extender Interface
	//--------------------------------
	
	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
	 */
	public Map<String,FunctionElement> getFunctions() {
		if (funcs == null) {
			funcs = new HashMap<String,FunctionElement>();
			funcs.put(
					ToStringFunctionElement.TOSTRING_FUNC_NAME,
					new ToStringFunctionElement());
			funcs.put(
					StringLengthFunctionElement.STRLENGTH_FUNC_NAME,
					new StringLengthFunctionElement());
			funcs.put(StringMatchingFunction.STRING_MATCHES_FUNCTION_NAME, 
					new StringMatchingFunction(capi));
			funcs.put(StringSubstringFunction.STRING_SUBSTRING_FUNCTION_NAME,
					new StringSubstringFunction());
		}
		return funcs;
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return null;
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
	 */
	public Map<String,UniverseElement> getUniverses() {
		// no universes
		return Collections.emptyMap();
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
	 */
	public Map<String,BackgroundElement> getBackgrounds() {
		if (backgroundElements == null) {
			backgroundElements = new HashMap<String,BackgroundElement>();
			stringBackgroundElement = new StringBackgroundElement();
			backgroundElements.put(
					StringBackgroundElement.STRING_BACKGROUND_NAME,
					stringBackgroundElement);
		}
		return backgroundElements;
	}
	
	//--------------------------------
	// Operator Implementor Interface
	//--------------------------------

	public Collection<OperatorRule> getOperatorRules() {
		
		ArrayList<OperatorRule> opRules = new ArrayList<OperatorRule>();
		
		opRules.add(new OperatorRule(STRINGCONCAT_OP,
				OpType.INFIX_LEFT,
				750,
				PLUGIN_NAME));
		
		
		return opRules;
	}

	public Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode) throws InterpreterException {
		Element result = null;
		String x = opNode.getToken();
		String gClass = opNode.getGrammarClass();
		
		// if class of operator is binary
		if (gClass.equals(ASTNode.BINARY_OPERATOR_CLASS))
		{
			
			// get operand nodes
			ASTNode alpha = opNode.getFirst();
			ASTNode beta = alpha.getNext();
			
			// get operand values
			Element l = alpha.getValue();
			Element r = beta.getValue();
			
			// if both operands are undef, the result is undef
			if (l.equals(Element.UNDEF) && r.equals(Element.UNDEF)) { 
				capi.warning(PLUGIN_NAME, "Both operands of the '" + x + "' operator were undef.", opNode, interpreter);
				result = Element.UNDEF;
			}
			else
				// confirm that at least one of the operands is a string elements, otherwise throw an error
				if ((l instanceof StringElement || r instanceof StringElement)) {
					if (x.equals(STRINGCONCAT_OP))
						result = stringBackgroundElement.getNewValue(l.toString() + r);
				}
			// otherwise 
			//    throw new InterpreterException("At least one operand must be strings for '"+x+"' operation.");
		}

		
		return result;
	}

	/**
	 * This plugin requires "NumberPlugin".
	 */
	public Set<String> getDependencyNames() {
		Set<String> names = new HashSet<String>(super.getDependencyNames());
		names.add("NumberPlugin");
		return names;
	}


	public Set<String> getBackgroundNames() {
		return backgroundElements.keySet();
	}

	public Set<String> getFunctionNames() {
		return funcs.keySet();
	}

	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	/**
	 * A pattern to match string litterals.
	 */
//	public static final class StringPattern extends Pattern {
//		
//		private static final long serialVersionUID = 1L;
//
//		public int match(final CharSequence src, final int len, final int from) {
//	        if(from >= len) 
//	        	return Pattern.MISMATCH;
//	        if (src.charAt(from) != '"')
//	        	return Pattern.MISMATCH;
//	        
//	        for (int i=from+1; i < len; i++) {
//	        	char c = src.charAt(i);
//	        	if (c == '"' && src.charAt(i-1) != '\\')
//	        		return i+1 - from;
//	        }
//	        
//	        return Pattern.MISMATCH;
//		}
//
//		public String toString(){
//			return "string constant";
//		}
//
//	}

	/**
	 * Creates string tokens.
	 */
//	@SuppressWarnings("serial")
//	public static class StringTokenizer implements Tokenizer {
//
//		public Object toToken(CharSequence cs, int from, int len) {
//			TypedToken<StringTokenType> tToken = 
//				new TypedToken<StringTokenType>(cs.subSequence(from, from + len).toString(), 
//						StringTokenType.String);
//			return tToken;
//		}
//		
//	}
	

	/**
	 * Type of string tokens.
	 * @author trident
	 *
	 */
	public static enum StringTokenType {
		String
	}
}
