/**
 * ExpressionParserFactory.java 		$Revision: 243 $
 * 
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $
 *
 */

package org.coreasm.engine.kernel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Unary;
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
  private Map<String, Map<String, OperatorRule>> closedOps = new HashMap<String, Map<String, OperatorRule>>();
	
	// precedence levels
	private Map<String, Integer> infixLeftOprs = new HashMap<String, Integer>();
	private Map<String, Integer> infixRightOprs = new HashMap<String, Integer>();
	private Map<String, Integer> infixNonOprs = new HashMap<String, Integer>();
	private Map<String, Integer> prefixOprs = new HashMap<String, Integer>();
	private Map<String, Integer> postfixOprs = new HashMap<String, Integer>();
  private Map<String, Integer> closedOprs = new HashMap<String, Integer>();

	// operator -> plugins (for convenience)
	private Map<String, String> infixLeftPlugins = new HashMap<String, String>();
	private Map<String, String> infixRightPlugins = new HashMap<String, String>();
	private Map<String, String> infixNonPlugins = new HashMap<String, String>();
	private Map<String, String> prefixPlugins = new HashMap<String, String>();
	private Map<String, String> postfixPlugins = new HashMap<String, String>();
  private Map<String, String> closedPlugins = new HashMap<String, String>();

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

    				case INDEX:
    				  oprRule = new OperatorRule(oprRule.opr + OperatorRule.OPERATOR_DELIMITER +
                  oprRule.opr2, OpType.PREFIX, oprRule.precedence, oprRule.contributor);
    				case PREFIX:
    					addOperator(prefixOprs, oprRule);
    					addOperatorPlugin(prefixPlugins, oprRule);
    					break;

            case CLOSED:
              addOperator(closedOprs, oprRule);
              addOperatorPlugin(closedPlugins, oprRule);
              break;
    				}
    			}
    		}
    	}

    	// Loading the results into the operator registry
    	OperatorRegistry oprReg = OperatorRegistry.getInstance(capi);
    	oprReg.infixOps.clear();
    	oprReg.infixOps.putAll(binOps);
    	oprReg.unOps.clear();
    	oprReg.unOps.putAll(unOps);
    	oprReg.closedOps.clear();
      oprReg.closedOps.putAll(closedOps);
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
      table.infixl(createInfixParser(opr, pluginNames, OpType.INFIX_LEFT), precedence);
			//table.infixl(createBinaryParser(opr, pluginNames, OpType.INFIX_LEFT), precedence);
		}

		// Infix Non-associative
		for (String opr: infixNonOprs.keySet()) {
			pluginNames = infixNonPlugins.get(opr);
			precedence = infixNonOprs.get(opr);
      table.infixn(createInfixParser(opr, pluginNames, OpType.INFIX_NON), precedence);
			//table.infixn(createBinaryParser(opr, pluginNames, OpType.INFIX_NON), precedence);
		}

		// Infix Right-associative
		for (String opr: infixRightOprs.keySet()) {
			pluginNames = infixRightPlugins.get(opr);
			precedence = infixRightOprs.get(opr);
      table.infixr(createInfixParser(opr, pluginNames, OpType.INFIX_RIGHT), precedence);
			//table.infixr(createBinaryParser(opr, pluginNames, OpType.INFIX_RIGHT), precedence);
		}
		
		// Prefix
		for (String opr: prefixOprs.keySet()) {
			pluginNames = prefixPlugins.get(opr);
			precedence = prefixOprs.get(opr);
			table.prefix(createPrefixParser(opr, pluginNames), precedence);
		}

		// Postfix
		for (String opr: postfixOprs.keySet()) {
			pluginNames = postfixPlugins.get(opr);
			precedence = postfixOprs.get(opr);
			table.postfix(createPostfixParser(opr, pluginNames), precedence);
		}
		
    Parser<Node> p = basicExprParser;
    
    // Closed
    for (String opr: closedOprs.keySet()) {
      pluginNames = closedPlugins.get(opr);
      p = p.or(createClosedParser(opr, pluginNames));
    }

    return table.build(p);
	}

	private Parser<Stream<Node>> internalHoleParser(String opr) {
    List<Parser<List<Node>>> o = Arrays.stream(opr.split(OperatorRule.OPERATOR_DELIMITER))
        .map(s -> ParserTools.seqList(ParserTools.getOprParser(s), termParser))
        .collect(Collectors.toList());
    o.set(o.size() - 1, ParserTools.getOprParser(opr.split(OperatorRule.OPERATOR_DELIMITER)[o.size() - 1]).map(Collections::singletonList));
    return ParserTools.seqList(o).map(ls -> ls.stream().flatMap(Collection::stream));

  }

  private Parser<BinaryMap> createInfixParser(String opr, String pluginNames, OpType type) {
	  final Parser<Stream<Node>> tempParser = internalHoleParser(opr);
    return tempParser.peek().followedBy(tempParser).map(new BinaryParseMap(opr, pluginNames, type));
  }

	private Parser<UnaryMap> createPrefixParser(String opr, String pluginNames) {
    final Parser<Stream<Node>> tempParser = internalHoleParser(opr);
			return tempParser.peek().followedBy(tempParser).map(
	    			new UnaryParseMap(opr, pluginNames, OpType.PREFIX));
	}

  private Parser<UnaryMap> createPostfixParser(String opr, String pluginNames) {
    final Parser<Stream<Node>> tempParser = internalHoleParser(opr);
      return tempParser.map(
          new UnaryParseMap(opr, pluginNames, OpType.POSTFIX));
  }

  private Parser<ASTNode> createClosedParser(String opr, String pluginNames) {
    return internalHoleParser(opr).map(ns -> {
      List<Node> nsl = ns.collect(Collectors.toList());
      Node n = nsl.get(0);
      ASTNode node = new ASTNode(null, ASTNode.CLOSED_OPERATOR_CLASS, null,
          opr, n.getScannerInfo());
      nsl.forEach(node::addChild);
      return node;
    });
  }

	/*
	 * Adds the name of the plugin contributer of this operator to the database.
	 */
	private void addOperatorPlugin(Map<String, String> oprPlugins, OperatorRule oprRule) {
		String pluginNames = oprPlugins.get(oprRule.getOprToken());
		if (pluginNames == null) {
			pluginNames = oprRule.contributor;
		} else
			if (!pluginNames.contains(oprRule.contributor))
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
    case INDEX:
			oprDB = unOps;
			break;
			
		case INFIX_LEFT:
		case INFIX_NON:
		case INFIX_RIGHT:
			oprDB = binOps;
			break;

    case CLOSED:
      oprDB = closedOps;
      break;
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
	public static class UnaryMap implements Unary<Node> {
		
		//private String pluginNames;
		private String opr;
		private Stream<Node> cnodes;
		private OpType type;
		
		/**
		 * Creates a new UnaryMap.
		 * 
		 * @param opr operator token
		 * @param pluginNames contributing plugin names
		 * @param type operator type
		 * @param nodes an array of operator and delimiter nodes (without the operands) 
		 */
		public UnaryMap(String opr, String pluginNames, OpType type, Stream<Node> nodes) {
			this.opr = opr;
			//this.pluginNames = pluginNames;
			//this.cnodes = (Object[])nodes[1];
			this.cnodes = nodes;
			this.type = type;
		}

		/**
		 * Creates a tree for this operator with an {@link ASTNode} as its root.
		 */
		public Node map(Node child) {
			Node node = null;
			if (type == OpType.POSTFIX) {
				node = new ASTNode(
						null, ASTNode.UNARY_OPERATOR_CLASS, "", opr, child.getScannerInfo());
				node.addChild(child);
        cnodes.forEach(node::addChild);
			}
			if (type == OpType.PREFIX) {
				node = new ASTNode(
						null, ASTNode.UNARY_OPERATOR_CLASS, "", opr, child.getScannerInfo());
        cnodes.forEach(node::addChild);
				node.addChild(child);
			}

			return node;
		}
	}
	
	/* Unary parse map class for binary operators */
	public static class UnaryParseMap extends ParseMap<Stream<Node>, UnaryMap> {

		private String opr;
		private OpType type;
		
		public UnaryParseMap(String opr, String pluginName, OpType type) {
			super(pluginName);
			this.opr = opr;
			this.type = type;
		}

		public UnaryMap map(Stream<Node> v) {
			return new UnaryMap(opr, pluginName, type, v);
		}
		
	}

	/* Special binary map class */
	public static class BinaryMap implements Binary<Node> {
		
		//private String pluginNames;
		private String opr;
		private Stream<Node> cnodes;
		//private OpType type;
		
		/**
		 * Creates a new BinaryMap.
		 * 
		 * @param opr operator token
		 * @param pluginNames contributing plugin names
		 * @param type operator type
		 * @param cnodes an array of operator and delimiter nodes (without the operands) 
		 */
		public BinaryMap(String opr, String pluginNames, OpType type, Stream<Node> cnodes) {
			this.opr = opr;
			//this.pluginNames = pluginNames;
			//this.type = type;
			this.cnodes = cnodes;
		}

		/**
		 * Creates a tree for this operator with an {@link ASTNode} as its root.
		 */
		public Node map(Node o1, Node o2) {
			Node node = new ASTNode(
					null, ASTNode.BINARY_OPERATOR_CLASS, "", opr, o1.getScannerInfo());
			node.addChild(o1);
			cnodes.forEach(node::addChild);
      node.addChild(o2);
			return node;
		}

	}
	
	/* Binary parse map class for binary operators */
	public static class BinaryParseMap extends ParseMap<Stream<Node>, BinaryMap> {

		private String opr;
		private OpType type;
		
		public BinaryParseMap(String opr, String pluginName, OpType type) {
			super(pluginName);
			this.opr = opr;
			this.type = type;
		}

		public BinaryMap map(Stream<Node> v) {
			return new BinaryMap(opr, pluginName, type, v);
		}
		
	}
}
