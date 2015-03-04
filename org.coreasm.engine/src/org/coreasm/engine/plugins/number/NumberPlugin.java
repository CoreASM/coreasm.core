/*	
 * NumberPlugin.java 		1.0 	$Revision: 253 $
 * 
 * Copyright (C) 2006 Mashaal Memon
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.number;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.Tokens;
import org.codehaus.jparsec.Tokens.Fragment;
import org.codehaus.jparsec.Tokens.Tag;
import org.codehaus.jparsec.pattern.Pattern;
import org.codehaus.jparsec.pattern.Patterns;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.ConstantFunction;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;

/**
 * Plugin for number related literals, operations, and functions.
 * 
 * @author Mashaal Memon, Roozbeh Farahbod
 * 
 */
public class NumberPlugin extends Plugin implements ParserPlugin,
		InterpreterPlugin, VocabularyExtender, OperatorProvider {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 5, 4, "");

	public static final String PLUGIN_NAME = NumberPlugin.class.getSimpleName();

	static final String NUMBER_TOKEN = "number";

	public static final String NUMBERADD_OP = "+";

	public static final String NUMBERSUBT_OP = "-";

	public static final String NUMBERMULT_OP = "*";

	public static final String NUMBERDIV_OP = "/";

	public static final String NUMBER_INT_DIV_OP = "div";

	public static final String NUMBERMOD_OP = "%";

	public static final String NUMBERUNMIN_OP = "-";

	public static final String NUMBEREXP_OP = "^";

	public static final String NUMBERGT_OP = ">";

	public static final String NUMBERGTE_OP = ">=";

	public static final String NUMBERLT_OP = "<";

	public static final String NUMBERLTE_OP = "<=";

	public static final String SIZE_OF_SYMBOL = "|";
	
	// TODO Why do we have these as static fileds?
	// public static NumberBackgroundElement NUMBER_BACKGROUND_ELEMENT;
	// public static NumberRangeBackgroundElement
	// NUMBER_RANGE_BACKGROUND_ELEMENT;
	private NumberBackgroundElement numberBackgroundElement;

	private NumberRangeBackgroundElement numberRangeBackgroundElement;

	private Map<String, BackgroundElement> backgroundElements = null;

	private Map<String, FunctionElement> functionElements = null;

	private Map<String, GrammarRule> parsers = null;
	private Set<Parser<? extends Object>> lexers = null;
	private final Map<String, Parser<Node>> exposedParsers;
	
	private final Parser.Reference<Node> refNumberRangeParser = Parser.newReference();
	private final Parser.Reference<Node> refNumberTermParser = Parser.newReference();
	
	// TOKENIZER
	Parser<Fragment> tokenizer_nr = null;

	private final String[] keywords = {"step", NUMBER_INT_DIV_OP};
	private final String[] operators = {"+", "-", "/", "*", "%", 
										".", "^", ">", "<", ">=", 
										"<=", "|", "[", "]", "..", ":"};

	private SizeFunctionElement sizeFunction = null;

	public NumberPlugin() {
		exposedParsers = new HashMap<String, Parser<Node>>();
	}

	@Override
	public void setControlAPI(ControlAPI capi) {
		super.setControlAPI(capi);
		//ParserTools npTools = ParserTools.getInstance(capi);
		exposedParsers.put("Number", refNumberTermParser.lazy());
		exposedParsers.put("NumberRangeTerm", refNumberRangeParser.lazy());
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.Plugin#interpret(org.coreasm.engine.interpreter.Node)
	 */
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) {

		ASTNode nextPos = pos;
		String x = pos.getToken();
		String gClass = pos.getGrammarClass();

		if (pos instanceof NumberRangeNode) {
			NumberRangeNode nrNode = (NumberRangeNode) pos;

			if (!nrNode.getStart().isEvaluated()) {
				return nrNode.getStart();
			} else if (!nrNode.getEnd().isEvaluated()) {
				return nrNode.getEnd();
			} else if (nrNode.getStep() != null
					&& !nrNode.getStep().isEvaluated()) {
				return nrNode.getStep();
			} else {
				Element fromElement = nrNode.getStart().getValue();
				double from = 0;

				if (fromElement instanceof NumberElement) {
					from = ((NumberElement) fromElement).value;
				} else {
					capi.error("Number range start value must be a number.",
							pos, interpreter);
					return pos;
				}

				Element toElement = nrNode.getEnd().getValue();
				double to = 0;
				if (toElement instanceof NumberElement) {
					to = ((NumberElement) toElement).value;
				} else {
					capi.error("Number range end value must be a number.", pos, interpreter);
					return pos;
				}

				double step = 1.0;

				if (nrNode.getStep() != null) {
					Element stepElement = nrNode.getStep().getValue();
					if (stepElement instanceof NumberElement) {
						step = ((NumberElement) stepElement).value;
					} else {
						capi.error("Number range step value must be a number.",
								pos, interpreter);
						return pos;
					}
				}

				NumberRangeElement rangeElement = null;
				try {
					rangeElement = numberRangeBackgroundElement.getNewValue(
							from, to, step);
					pos.setNode(null, null, rangeElement);
				} catch (IllegalArgumentException e) {
					capi.error(e.getMessage(), pos, interpreter);
				}

				return pos;
			}
		}

		else if (pos instanceof SizeOfEnumNode) {
			SizeOfEnumNode node = (SizeOfEnumNode) pos;
			ASTNode enumerable = node.getEnumerableNode();

			if (!enumerable.isEvaluated()) {
				return enumerable;
			} else {
				Element value = enumerable.getValue();
				if (value instanceof Enumerable) {
					if (sizeFunction == null)
						sizeFunction = new SizeFunctionElement();
					pos.setNode(null, null, sizeFunction.getValue((Enumerable)value));
				} else
					pos.setNode(null, null, Element.UNDEF);
			}
		}

		// if number related expression
		else if (gClass.equals(ASTNode.EXPRESSION_CLASS)) {
			// it is a number constant
			if (x != null) {

				// convert string representationg to numeric
				double number = Double.parseDouble(x);

				NumberElement ne = null;
				try {
					// get new number element from the number background
					ne = numberBackgroundElement.getNewValue(number);
				} catch (Throwable e) {
					System.out.println(x);
					System.out.println(numberBackgroundElement);
					System.out.println(number);
					throw new Error(e);
				}

				// result of this node is the number element produced
				pos.setNode(null, null, ne);
			}
		}

		return nextPos;
	}

	public Set<Parser<? extends Object>> getLexers() {
		if (lexers == null) {
			lexers = new HashSet<Parser<? extends Object>>();
			
			// Define pattern for the numbers tokenizer manually to aviod the
			// recognition of strings like '1.' as 1.0 or '.1' as 0.1 
			// This clashes with the NumberRangeTerm rule (for example '[1..10]'). 
			// In other words: This tokenizer only recognizes numbers without
			// a decimal point and numbers with a decimal point preceded AND followed
			// by at least one digit.
			
			Pattern pDigits = Patterns.range('0', '9').many1();
			Pattern pFloat = pDigits.next(Patterns.isChar('.').next(pDigits).optional());
			Parser<String> sFloat = Scanners.pattern(pFloat, "NUMBER").source();
			tokenizer_nr = sFloat.map(
				new org.codehaus.jparsec.functors.Map<String,Fragment>() {
					@Override
					public Fragment map(String from) {
						return Tokens.fragment(from, Tag.DECIMAL);
					}				
				}
			);
			lexers.add(tokenizer_nr);
		}
		return lexers;
	}
	
	/*
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		return exposedParsers.get(nonterminal);
	}
	
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();

			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);
			
			// NumberTerm
			createNumberParser();
			parsers.put("ConstantTerm",
					new GrammarRule("Number", "NUMBER", getParser("Number"), PLUGIN_NAME));
			
			// NumberRangeTerm: '[' Term '..' Term ((':'|'step') Term)? ']'
			Parser<Node> numberRangeTermParser = Parsers.array(
					new Parser[] {
						pTools.getOprParser("["),
						termParser,
						pTools.getOprParser(".."),
						termParser,
						pTools.seq(
								Parsers.or(pTools.getOprParser(":"), pTools.getKeywParser("step", PLUGIN_NAME)),
								termParser).optional(),
						pTools.getOprParser("]")
					}).map(new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object[] vals) {
							Node node = new NumberRangeNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			});
			parsers.put("NumberRangeTerm", 
					new GrammarRule("NumberRangeTerm",
							"'[' Term '..' Term ((':'|'step') Term)? ']'", 
							getParser("NumberRangeTerm"), PLUGIN_NAME));
			refNumberRangeParser.set(numberRangeTermParser);

			// TODO this has to go to EnumerablePlugin
			Parser<Node> sizeOfEnumTermParser = Parsers.array(
					new Parser[] {
						pTools.getOprParser("|"),
						termParser,
						pTools.getOprParser("|")
					}).map(new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object[] vals) {
							Node node = new SizeOfEnumNode(((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
			} );
			parsers.put("SizeOfEnumTerm", 
					new GrammarRule("SizeOfEnumTerm",
							"'|' Term '|'", sizeOfEnumTermParser, PLUGIN_NAME));

			parsers.put("BasicTerm", 
					new GrammarRule("NumberBasicTerm",
							"NumberRangeTerm | SizeOfEnumTerm", Parsers.or(
									getParser("NumberRangeTerm"),
									sizeOfEnumTermParser), PLUGIN_NAME));
		}
		return parsers;
	}

	/**
	 * Provides a number parser that would parse a real number into a properly
	 * created {@link Node} object.
	 * 
	 */
	private Parser<Node> createNumberParser() {
	
		if (refNumberTermParser.get() == null) {
			Parser<Node> nrParser = Terminals.fragment(Tag.DECIMAL).token().map(
				new org.codehaus.jparsec.functors.Map<Token,Node> () {
					@Override
					public Node map(Token from) {
						return new NumberTermNode(new ScannerInfo(from), from.toString());
					}
				}
			);
			refNumberTermParser.set(nrParser);
		}
		return refNumberTermParser.lazy();

	}

	// --------------------------------
	// Vocabulary Extender Interface
	// --------------------------------

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getFunctions()
	 */
	public Map<String, FunctionElement> getFunctions() {
		if (functionElements == null) {
			functionElements = new HashMap<String, FunctionElement>();

			sizeFunction  = new SizeFunctionElement();
			functionElements.put(
					SizeFunctionElement.NAME, 
					sizeFunction);

			functionElements.put(
					NumberNaturalFunction.NUMBER_NATURAL_FUNCTION_NAME,
					new NumberNaturalFunction());
			functionElements.put(
					NumberIntegerFunction.NUMBER_INTEGER_FUNCTION_NAME,
					new NumberIntegerFunction());
			functionElements.put(NumberRealFunction.NUMBER_REAL_FUNCTION_NAME,
					new NumberRealFunction());
			/* 
			 * I don't think we really need these
			functionElements.put(
					NumberPositiveFunction.NUMBER_POSITIVE_FUNCTION_NAME,
					new NumberPositiveFunction());
			functionElements.put(
					NumberNegativeFunction.NUMBER_NEGATIVE_FUNCTION_NAME,
					new NumberNegativeFunction());
			*/
			functionElements.put(NumberEvenFunction.NUMBER_EVEN_FUNCTION_NAME,
					new NumberEvenFunction());
			functionElements.put(NumberOddFunction.NUMBER_ODD_FUNCTION_NAME,
					new NumberOddFunction());
			functionElements.put(ToNumberFunctionElement.TONUMBER_FUNC_NAME,
					new ToNumberFunctionElement());
			functionElements.put("infinity", 
					new ConstantFunction(NumberElement.POSITIVE_INFINITY));
		}
		return functionElements;
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getUniverses()
	 */
	public Map<String, UniverseElement> getUniverses() {
		// no universe
		return Collections.emptyMap();
	}

	/**
	 * @see org.coreasm.engine.plugin.VocabularyExtender#getBackgrounds()
	 */
	public Map<String, BackgroundElement> getBackgrounds() {
		if (backgroundElements == null) {
			backgroundElements = new HashMap<String, BackgroundElement>();

			// initialize number background element
			numberBackgroundElement = new NumberBackgroundElement();

			// put it in collection
			backgroundElements.put(
					NumberBackgroundElement.NUMBER_BACKGROUND_NAME,
					numberBackgroundElement);

			// initialize integer range background element
			numberRangeBackgroundElement = new NumberRangeBackgroundElement();

			// put it in collection
			backgroundElements.put(
					NumberRangeBackgroundElement.NUMBER_RANGE_BACKGROUND_NAME,
					numberRangeBackgroundElement);
		}
		return backgroundElements;
	}

	public Set<String> getRuleNames() {
		return Collections.emptySet();
	}

	public Map<String, RuleElement> getRules() {
		return null;
	}

	// --------------------------------
	// Operator Implementor Interface
	// --------------------------------

	public Collection<OperatorRule> getOperatorRules() {

		ArrayList<OperatorRule> opRules = new ArrayList<OperatorRule>();

		opRules.add(new OperatorRule(NUMBERADD_OP, OpType.INFIX_LEFT, 750, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBERSUBT_OP, OpType.INFIX_LEFT, 750, PLUGIN_NAME));

		// bind tighter than others
		opRules.add(new OperatorRule(NUMBERMULT_OP, OpType.INFIX_LEFT, 800, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBERDIV_OP, OpType.INFIX_LEFT, 800, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBER_INT_DIV_OP, OpType.INFIX_LEFT, 800, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBERMOD_OP, OpType.INFIX_LEFT, 800, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBEREXP_OP, OpType.INFIX_LEFT, 820, PLUGIN_NAME));

		// unary minus, higher precedence than all the rest
		opRules.add(new OperatorRule(NUMBERUNMIN_OP, OpType.PREFIX, 850, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBERGT_OP, OpType.INFIX_LEFT, 650, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBERGTE_OP, OpType.INFIX_LEFT, 650, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBERLT_OP, OpType.INFIX_LEFT, 650, PLUGIN_NAME));

		opRules.add(new OperatorRule(NUMBERLTE_OP, OpType.INFIX_LEFT, 650, PLUGIN_NAME));

		return opRules;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.OperatorImplementor#interpretOperatorNode(org.coreasm.engine.interpreter.Node)
	 */
	public Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode)
			throws InterpreterException {
		Element result = null;
		String x = opNode.getToken();
		String gClass = opNode.getGrammarClass();

		// if class of operator is binary
		if (gClass.equals(ASTNode.BINARY_OPERATOR_CLASS)) {

			// get operand nodes
			ASTNode alpha = opNode.getFirst();
			ASTNode beta = alpha.getNext();

			// get operand values
			Element l = alpha.getValue();
			Element r = beta.getValue();

			// confirm that operands are numeric elements, or undef 
			if ((l instanceof NumberElement || l.equals(Element.UNDEF)) 
					&& (r instanceof NumberElement || r.equals(Element.UNDEF))) {
				if (l.equals(Element.UNDEF) || r.equals(Element.UNDEF))
					result = Element.UNDEF;
				else {
					// convert operands to number elements
					NumberElement eL = (NumberElement) l;
					NumberElement eR = (NumberElement) r;

					// numeric add
					if (x.equals(NUMBERADD_OP)) {
						result = numberBackgroundElement.getNewValue(eL.value
								+ eR.value);
					}
					// numeric subtract
					else if (x.equals(NUMBERSUBT_OP)) {
						result = numberBackgroundElement.getNewValue(eL.value
								- eR.value);

					}
					// numeric multiply
					else if (x.equals(NUMBERMULT_OP)) {
						result = numberBackgroundElement.getNewValue(eL.value
								* eR.value);
					}
					// numeric divide
					else if (x.equals(NUMBERDIV_OP)) {
						result = numberBackgroundElement.getNewValue(eL.value
								/ eR.value);
					}
					// numeric integer divide
					else if (x.equals(NUMBER_INT_DIV_OP)) {
						result = numberBackgroundElement.getNewValue(Math
								.floor(eL.value / eR.value));
					}
					// numeric modulous
					else if (x.equals(NUMBERMOD_OP)) {
						result = numberBackgroundElement.getNewValue(eL.value
								% eR.value);
					} else if (x.equals(NUMBEREXP_OP)) {
						result = numberBackgroundElement.getNewValue(Math.pow(
								eL.value, eR.value));
					} else if (x.equals(NUMBERGT_OP)) {
						result = BooleanElement.valueOf(eL.value > eR.value);
					} else if (x.equals(NUMBERGTE_OP)) {
						result = BooleanElement.valueOf(eL.value >= eR.value);
					} else if (x.equals(NUMBERLT_OP)) {
						result = BooleanElement.valueOf(eL.value < eR.value);
					} else if (x.equals(NUMBERLTE_OP)) {
						result = BooleanElement.valueOf(eL.value <= eR.value);
					}
				}
			} else {
				if (l.equals(Element.UNDEF) && r.equals(Element.UNDEF))
					capi.warning(PLUGIN_NAME, "Both operands of the '" + x + "' operator were undef.", opNode, interpreter);
				else
					if (l.equals(Element.UNDEF))
						capi.warning(PLUGIN_NAME, "The left operand of the '" + x + "' operator was undef.", opNode, interpreter);
					else
						if (r.equals(Element.UNDEF))
							capi.warning(PLUGIN_NAME, "The right operand of the '" + x + "' operator was undef.", opNode, interpreter);
			}
		}
		
		// if class of operator is unary
		if (gClass.equals(ASTNode.UNARY_OPERATOR_CLASS)) {
			// get operand nodes
			ASTNode alpha = opNode.getFirst();

			// get operand values
			Element o = alpha.getValue();

			// confirm that operand is numeric element or undef
			if (o.equals(Element.UNDEF)) {
				result = Element.UNDEF;
				capi.warning(PLUGIN_NAME, "The operand of the unariy '" + x + "' operator was undef.", opNode, interpreter);
			} else
				if (o instanceof NumberElement) {
					// convert operand to number element
					NumberElement eO = (NumberElement) o;

					// numeric add
					if (x.equals(NUMBERUNMIN_OP)) {
						result = numberBackgroundElement.getNewValue(-eO.value);
					}
				}
		}

		return result;
	}

	@Override
	public void initialize() {
		getBackgrounds();
		getFunctions();
		getUniverses();
	}

	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	}

	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	}

	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	}

	public NumberBackgroundElement getNumberBackgroundElement() {
		return numberBackgroundElement;
	}

	public NumberRangeBackgroundElement getNumberRangeBackgroundElement() {
		return numberRangeBackgroundElement;
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getOperators() {
		return operators;
	}
	
	/** 
	 * Type of number tokens.
	 */
	public static enum NumberTokenType {
		Number
	}

}
