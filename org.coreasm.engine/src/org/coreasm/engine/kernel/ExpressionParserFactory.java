/**
 * ExpressionParserFactory.java 		$Revision: 243 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $
 *
 */

package org.coreasm.engine.kernel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.OperatorRegistry;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParseMap;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class can be used to build an expression parser
 * given a basic expression parser and a list of plugins 
 * contributing operator rules.
 *   
 * @author Roozbeh Farahbod
 * 
 */

public class ExpressionParserFactory {

	private static final Logger logger = LoggerFactory.getLogger(ExpressionParserFactory.class);

	private final Parser<Node> termParser;
	private final Parser<Node> basicExprParser;
	private final Set<Plugin> plugins;
	private final ParserTools ParserTools;
	
	// Maps of the form: Operator -> (PluginName -> OperatorRule) 
	private Map<String, Map<String, OperatorRule>> binOps = new HashMap<String, Map<String, OperatorRule>>();
	private Map<String, Map<String, OperatorRule>> unOps = new HashMap<String, Map<String, OperatorRule>>();
	private Map<String, Map<String, OperatorRule>> indexOps = new HashMap<String, Map<String, OperatorRule>>();
	
	// precedence levels
	private Map<String, Integer> infixLeftOprs = new HashMap<String, Integer>();
	private Map<String, Integer> infixRightOprs = new HashMap<String, Integer>();
	private Map<String, Integer> infixNonOprs = new HashMap<String, Integer>();
	private Map<String, Integer> prefixOprs = new HashMap<String, Integer>();
	private Map<String, Integer> postfixOprs = new HashMap<String, Integer>();
	private Map<String, Integer> indexOprs = new HashMap<String, Integer>();

	// operator -> plugins (for convenient)
	private Map<String, String> infixLeftPlugins = new HashMap<String, String>();
	private Map<String, String> infixRightPlugins = new HashMap<String, String>();
	private Map<String, String> infixNonPlugins = new HashMap<String, String>();
	private Map<String, String> prefixPlugins = new HashMap<String, String>();
	private Map<String, String> postfixPlugins = new HashMap<String, String>();
	private Map<String, String> indexPlugins = new HashMap<String, String>();

	Parser<IndexMap> indexParser = null;
	
	/**
	 * Creates a new expression parser factory.  
	 * @param termParser basic expression parser
	 * @param plugins a set of plugins
	 */
	public ExpressionParserFactory(ControlAPI capi, ParserTools ParserTools, Parser<Node> basicExprParser, Parser<Node> termParser, Set<Plugin> plugins) {
		this.plugins = plugins;
		this.ParserTools = ParserTools;
		this.basicExprParser = basicExprParser;
		this.termParser = termParser;
		
		loadOperatorRules(capi);
	}
	
	/*
	 * Loads all the operator rules from plugins.
	 */
	private void loadOperatorRules(ControlAPI capi) {
    	for (Plugin p: plugins) {
    		if (p instanceof OperatorProvider) {
    			Collection<OperatorRule> oprRules = ((OperatorProvider)p).getOperatorRules();
    			for (OperatorRule oprRule: oprRules) {
    				
    				switch (oprRule.getType()) {
    				
    				case INFIX_LEFT:
    					addOperator(infixLeftOprs, oprRule);
    					addOperatorPlugin(infixLeftPlugins, oprRule);
    					break;
    					
    				case INFIX_NON:
    					addOperator(infixNonOprs, oprRule);
    					addOperatorPlugin(infixNonPlugins, oprRule);
    					break;
    					
    				case INFIX_RIGHT:
    					addOperator(infixRightOprs, oprRule);
    					addOperatorPlugin(infixRightPlugins, oprRule);
    					break;
    					
    				case POSTFIX:
    					addOperator(postfixOprs, oprRule);
    					addOperatorPlugin(postfixPlugins, oprRule);
    					break;
    					
    				case PREFIX:
    					addOperator(prefixOprs, oprRule);
    					addOperatorPlugin(prefixPlugins, oprRule);
    					break;

    				case INDEX:
    					addOperator(indexOprs, oprRule);
    					addOperatorPlugin(indexPlugins, oprRule);
    					
    				}
    			}
    		}
    	}

    	// Loading the results into the operator registry
    	OperatorRegistry oprReg = OperatorRegistry.getInstance(capi);
    	oprReg.binOps.clear();
    	oprReg.binOps.putAll(binOps);
    	oprReg.unOps.clear();
    	oprReg.unOps.putAll(unOps);
    	oprReg.indexOps.clear();
    	oprReg.indexOps.putAll(indexOps);
	}

	/**
	 * Creates a new expression parser for the given set of plugins.
	 */
	public Parser<Node> createExpressionParser() {
		OperatorTable<Node> table = new OperatorTable<Node>();

		String pluginNames;
		Integer precedence;

		// Infix Left-associative
		for (String opr: infixLeftOprs.keySet()) {
			pluginNames = infixLeftPlugins.get(opr);
			precedence = infixLeftOprs.get(opr);
			table.infixl(createBinaryParser(opr, pluginNames, OpType.INFIX_LEFT), precedence.intValue());
		}

		// Infix Non-associative
		for (String opr: infixNonOprs.keySet()) {
			pluginNames = infixNonPlugins.get(opr);
			precedence = infixNonOprs.get(opr);
			table.infixn(createBinaryParser(opr, pluginNames, OpType.INFIX_NON), precedence.intValue());
		}

		// Infix Right-associative
		for (String opr: infixRightOprs.keySet()) {
			pluginNames = infixRightPlugins.get(opr);
			precedence = infixRightOprs.get(opr);
			table.infixr(createBinaryParser(opr, pluginNames, OpType.INFIX_RIGHT), precedence.intValue());
		}
		
		// Prefix
		for (String opr: prefixOprs.keySet()) {
			pluginNames = prefixPlugins.get(opr);
			precedence = prefixOprs.get(opr);
			table.prefix(createUnaryParser(opr, pluginNames, OpType.PREFIX), precedence.intValue());
		}

		// Postfix
		for (String opr: postfixOprs.keySet()) {
			pluginNames = postfixPlugins.get(opr);
			precedence = postfixOprs.get(opr);
			table.postfix(createUnaryParser(opr, pluginNames, OpType.POSTFIX), precedence.intValue());
		}

		// Index
		for (String opr: indexOprs.keySet()) {
			pluginNames = indexPlugins.get(opr);
			precedence = indexOprs.get(opr);
			int i = opr.indexOf(OperatorRule.OPERATOR_DELIMITER);
			String opr1 = opr.substring(0, i);
			String opr2 = opr.substring(i + 1);
			table.postfix(createIndexParser(opr1, opr2, pluginNames), precedence.intValue());
		}

		Parser<Node> p = table.build(basicExprParser);
		return p;
	}
	
	/* 
	 * Creates a new binary parser.
	 */
	private Parser<BinaryMap> createBinaryParser(String opr, String pluginNames, OpType type) {
		final Parser<Node> tempParser = ParserTools.getOprParser(opr);//, termParser.peek());
		return ParserTools.seq(tempParser.peek(), tempParser).map(
				new BinaryParseMap(opr, pluginNames, type));
		// without lookahead:
		// return parserTools.seq(parserTools.getOprParser(opr), optionalDelimiter).map(
		//		new BinaryParseMap(opr, pluginNames, type));
	}
	
	/* 
	 * Creates a new unary parser.
	 */
	private Parser<UnaryMap> createUnaryParser(String opr, String pluginNames, OpType type) {
		if (type == OpType.PREFIX) {
			final Parser<Node> tempParser = ParserTools.getOprParser(opr);//, termParser.peek());
			return ParserTools.seq(tempParser.peek(), tempParser).map(
	    			new UnaryParseMap(opr, pluginNames, type));
		}
		else //if (type == OpType.POSTFIX)
	    	return ParserTools.seq(ParserTools.getOprParser(opr)).map(
	    			new UnaryParseMap(opr, pluginNames, type));
	}

	/*
	 * Creates a new index parser.
	 */
	private Parser<IndexMap> createIndexParser(String opr1, String opr2, String pluginNames) {
		return ParserTools.seq(
				ParserTools.getOprParser(opr1), 
				termParser.optional(), 
				ParserTools.getOprParser(opr2)
				).map(new IndexParseMap(opr1, opr2, pluginNames, OpType.INDEX));
	}
	
	/*
	 * Adds the name of the plugin contributer of this operator to the database.
	 */
	private void addOperatorPlugin(Map<String, String> oprPlugins, OperatorRule oprRule) {
		String pluginNames = oprPlugins.get(oprRule.getOprToken());
		if (pluginNames == null) {
			pluginNames = oprRule.contributor;
		} else
			if (pluginNames.indexOf(oprRule.contributor) < 0)
				pluginNames = pluginNames + ", " + oprRule.contributor;
			else
				return;
		oprPlugins.put(oprRule.getOprToken(), pluginNames);
	}
	
	/*
	 * Adds one single operator rule to the database.
	 */
	private void addOperator(Map<String, Integer> oprs, OperatorRule oprRule) {
		
		Map<String, Map<String, OperatorRule>> oprDB = null;
		String oprToken = oprRule.getOprToken();
		switch (oprRule.type) {
		case PREFIX:
		case POSTFIX:
			oprDB = unOps;
			break;
			
		case INFIX_LEFT:
		case INFIX_NON:
		case INFIX_RIGHT:
			oprDB = binOps;
			break;
			
		case INDEX:
			oprDB = indexOps;
		}

		Integer tempPrec = oprs.get(oprToken);
		Map<String, OperatorRule> pMap = oprDB.get(oprToken);

		//	if this operator is a new one in its type
		if (tempPrec == null) { 
			oprs.put(oprToken, oprRule.precedence);
			if (pMap == null) { 
				pMap = new HashMap<String, OperatorRule>();
				oprDB.put(oprToken, pMap);
			}
			pMap.put(oprRule.contributor, oprRule);
		} else
			if (tempPrec.intValue() == oprRule.precedence) { 
				pMap.put(oprRule.contributor, oprRule);
			} else {
				// there should be another rule in pMap
				OperatorRule anotherRule = pMap.values().iterator().next();
				String errorMsg = "Kernel Plugin: Operator \"" 
					+ oprToken + "\" from " + oprRule.contributor + " with precedence level " 
					+ oprRule.precedence + " conflicts with the same operator from "
					+ anotherRule.contributor + " with precedence level " + anotherRule.precedence + ".";
				logger.error(errorMsg);
				throw new EngineError(errorMsg);
			}
	}
	
	/* Special unary map class */
	public static class UnaryMap implements UnaryOperator<Node> {
		
		//private String pluginNames;
		private String opr;
		private Object[] cnodes;
		private OpType type;
		
		/**
		 * Creates a new UnaryMap.
		 * 
		 * @param opr operator token
		 * @param pluginNames contributing plugin names
		 * @param type operator type
		 * @param nodes an array of operator and delimiter nodes (without the operands) 
		 */
		public UnaryMap(String opr, String pluginNames, OpType type, Object[] nodes) {
			this.opr = opr;
			//this.pluginNames = pluginNames;
			//this.cnodes = (Object[])nodes[1];
			this.cnodes = nodes;
			this.type = type;
		}

		/**
		 * Creates a tree for this operator with an {@link ASTNode} as its root.
		 */
		@Override
		public Node apply(Node child) {
			Node node = null;
			if (type == OpType.POSTFIX) {
				node = new ASTNode(
						null, ASTNode.UNARY_OPERATOR_CLASS, "", ((Node)cnodes[1]).getToken(), child.getScannerInfo());
				node.addChild(child);
//				if (cnodes[0] != null)
//					node.addChild((Node)cnodes[0]);
				node.addChild(new Node(null, opr, ((Node)cnodes[1]).getScannerInfo()));
			}
			if (type == OpType.PREFIX) {
				node = new ASTNode(
						null, ASTNode.UNARY_OPERATOR_CLASS, "", ((Node)cnodes[0]).getToken(), child.getScannerInfo());
				node.addChild(new Node(null, opr, ((Node)cnodes[0]).getScannerInfo()));
//				if (cnodes[1] != null)
//					node.addChild((Node)cnodes[1]);
				node.addChild(child);
			}

			return node;
		}
	}
	
	/* Unary parse map class for binary operators */
	public static class UnaryParseMap extends ParseMap<Object[], UnaryMap> {

		private String opr;
		private OpType type;
		
		public UnaryParseMap(String opr, String pluginName, OpType type) {
			super(pluginName);
			this.opr = opr;
			this.type = type;
		}

		@Override
		public UnaryMap apply(Object[] v) {
			return new UnaryMap(opr, pluginName, type, v);
		}
		
	}

	/* Special binary map class */
	public static class BinaryMap implements BinaryOperator<Node> {
		
		//private String pluginNames;
		private String opr;
		private Object[] cnodes;
		//private OpType type;
		
		/**
		 * Creates a new BinaryMap.
		 * 
		 * @param opr operator token
		 * @param pluginNames contributing plugin names
		 * @param type operator type
		 * @param cnodes an array of operator and delimiter nodes (without the operands) 
		 */
		public BinaryMap(String opr, String pluginNames, OpType type, Object[] cnodes) {
			this.opr = opr;
			//this.pluginNames = pluginNames;
			//this.type = type;
			//this.cnodes = (Object[])cnodes[1];
			this.cnodes = cnodes;
		}

		/**
		 * Creates a tree for this operator with an {@link ASTNode} as its root.
		 */
		@Override
		public Node apply(Node o1, Node o2) {
			Node node = new ASTNode(
					null, ASTNode.BINARY_OPERATOR_CLASS, "", ((Node)cnodes[1]).getToken(), o1.getScannerInfo());
			node.addChild(o1);
			node.addChild(new Node(null, opr, o1.getScannerInfo()));
//			if (cnodes[1] != null)
//				node.addChild((Node)cnodes[1]);
			node.addChild(o2);
			//FIXME INFIX_RIGHT and INFIX_NON are skipped for now
			return node;
		}

	}
	
	/* Binary parse map class for binary operators */
	public static class BinaryParseMap extends ParseMap<Object[], BinaryMap> {

		private String opr;
		private OpType type;
		
		public BinaryParseMap(String opr, String pluginName, OpType type) {
			super(pluginName);
			this.opr = opr;
			this.type = type;
		}

		@Override
		public BinaryMap apply(Object[] v) {
			return new BinaryMap(opr, pluginName, type, v);
		}
		
	}

	/* Special index map class */
	public static class IndexMap implements UnaryOperator<Node> {
		
		//private String pluginNames;
		private String opr1;
		private String opr2;
		private Object[] cnodes;
		//private OpType type;
		
		/**
		 * Creates a new IndexMap.
		 * 
		 * @param opr1 first operator token
		 * @param opr2 second operator token
		 * @param pluginNames contributing plugin names
		 * @param type operator type
		 * @param nodes an array of operator and delimiter nodes (without the operands) 
		 */
		public IndexMap(String opr1, String opr2, String pluginNames, OpType type, Object[] nodes) {
			this.opr1 = opr1;
			this.opr2 = opr2;
			//this.pluginNames = pluginNames;
			this.cnodes = nodes;
			//this.type = type;
		}

		/**
		 * Creates a tree for this operator with an {@link ASTNode} as its root.
		 */
		@Override
		public Node apply(Node child) {
			Node node = null;
			node = new ASTNode(
					null, ASTNode.INDEX_OPERATOR_CLASS, "", opr1 + OperatorRule.OPERATOR_DELIMITER + opr2, child.getScannerInfo());
			node.addChild(child);
			for (Object obj: cnodes)
				if (obj != null)
					node.addChild((Node)obj);
			return node;
		}
	}
	
	/* Index parse map class for index operators */
	public static class IndexParseMap extends ParseMap<Object[], IndexMap> {

		private String opr1;
		private String opr2;
		private OpType type;
		
		public IndexParseMap(String opr1, String opr2, String pluginName, OpType type) {
			super(pluginName);
			this.opr1 = opr1;
			this.opr2 = opr2;
			this.type = type;
		}

		@Override
		public IndexMap apply(Object[] v) {
			return new IndexMap(opr1, opr2, pluginName, type, v);
		}
		
	}


}
