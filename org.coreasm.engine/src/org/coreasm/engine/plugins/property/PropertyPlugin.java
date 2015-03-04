/*	
 * PropertyPlugin.java 	$Revision: 243 $
 * 
 * Copyright (C) 2007 George Ma
 * Copyright (C) 2007 Roozbeh Farahbod
 *
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.plugins.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.parser.OperatorRule.OpType;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;

/** 
 * Supports LTL properties in the header. 
 *   
 * @author  George Ma, Roozbeh Farahbod
 * 
 */
public class PropertyPlugin extends Plugin implements ParserPlugin, OperatorProvider {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 2, 1, "beta");
	
	public static final String PLUGIN_NAME = PropertyPlugin.class.getSimpleName();
    public static final String ALWAYS_OP = "G";
    public static final String EVENTUALLY_OP = "F";
    public static final String UNTIL_OP = "U";
    public static final String NEXT_OP = "X";
    public static final String DUAL_OF_UNTIL_OP = "V";
    
    private ArrayList<OperatorRule> opRules = null;
    private Map<String, GrammarRule> parsers = null;
    
	private final String[] keywords = {"G", "F", "U", "X", "V", "check", "property"};
	private final String[] operators = {};
	
    @Override
    public void initialize() {
        
    }

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	/**
	 * @return <code>null</code>
	 */
	public Parser<Node> getParser(String nonterminal) {
		return null;
	}

    public Map<String, GrammarRule> getParsers() {
    	if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
					.getPluginInterface();
			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);

			// 'property' Term
			Parser<Object[]> propertyExpr = pTools.seq(
					pTools.getKeywParser("check", PLUGIN_NAME).atomic().optional(),
					pTools.getKeywParser("property", PLUGIN_NAME),
					termParser
					);
			
 			// PropertyList : ('property' Expression)* ('check' 'property' Expression)? ('property' Expression)*
			// PropertyList : ('property' Term)* ('check' 'property' Term)? ('property' Term)*
			Parser<Node> propertyParser = Parsers.array(
					new Parser[] {
						pTools.plus(propertyExpr),
					}).map(
					new PropertyParseMap());
			
			parsers.put("Header", 
					new GrammarRule("PropertyList", 
							"('property' Term)* ('check' 'property' Term)? ('property' Term)*", 
							propertyParser, PLUGIN_NAME));

    	}
    	
    	return parsers;
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

	public Collection<OperatorRule> getOperatorRules() {
        if (opRules == null) {
            opRules = new ArrayList<OperatorRule>();
            
            opRules.add(new OperatorRule(UNTIL_OP,
                        OpType.INFIX_LEFT,
                        400,
                        getName()));
            
            opRules.add(new OperatorRule(DUAL_OF_UNTIL_OP,
                        OpType.INFIX_LEFT,
                        400,
                        getName()));
            
            opRules.add(new OperatorRule(ALWAYS_OP,
                        OpType.PREFIX,
                        500,
//                        OpAssoc.RIGHT,
                        getName()));
            
            opRules.add(new OperatorRule(EVENTUALLY_OP,
                        OpType.PREFIX,
                        500,
//                        OpAssoc.RIGHT,
                        getName()));
            
            opRules.add(new OperatorRule(NEXT_OP,
                        OpType.PREFIX,
                        500,
//                        OpAssoc.LEFT,
                        getName()));                   
        }
            
        return opRules;
    }

 	public Element interpretOperatorNode(Interpreter interpreter, ASTNode opNode) throws InterpreterException {
		// TODO Auto-generated method stub
		return null;
	}

 	public static class PropertyParseMap extends ParserTools.ArrayParseMap {

 		public PropertyParseMap() {
 			super(PLUGIN_NAME);
 		}
 		
 		public Node map(Object[] vals) {
	        PropertyListNode node = new PropertyListNode(null);
	        addChildren(node, vals);
	        node.setScannerInfo(node.getFirstCSTNode());
	        node.incrementPropertyCount();
			return node;
		}
		
 		@Override
		public void addChild(Node parent, Node child) {
	        if (child.getToken() != null && child.getToken().equals("check")) {
	            ((PropertyListNode)parent).setHasCheck(true);
	        }
	        parent.addChild(child);
		}

 	}
}
