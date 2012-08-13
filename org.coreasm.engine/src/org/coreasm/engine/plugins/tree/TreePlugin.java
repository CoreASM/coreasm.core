/*	
 * TreePlugin.java
 * 
 * Copyright (C) 2010 Dipartimento di Informatica, Universita` di Pisa, Italy.
 *
 * Author: Franco Alberto Cardillo 		(facardillo@gmail.com)
 *
 * Licensed under the Academic Free License version 3.0
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.plugins.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractStorage;
import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginCompositionAPI;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.ParseMapN;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.plugins.list.ListElement;
import org.coreasm.engine.plugins.number.NumberElement;



/** 
 * CoreASM Plugin for the 'TREE' data structure 
 *   
 * @author  Franco Alberto Cardillo (facardillo@gmail.com)
 * 
 */
public class TreePlugin extends Plugin 
implements ParserPlugin, InterpreterPlugin,	VocabularyExtender, TreeOptionsReader {

	// prefix for all the functions offered by the plugin
	public static final String TREE_PREFIX = "tree";
	
	
	public static TreeOptionsReader optionsReader;
	public static TreePlugin instance;

	// Class constants required by the CoreASM framework
	public static final VersionInfo VERSION_INFO = new VersionInfo(1, 0, 1, "alpha");
	public static final String PLUGIN_NAME = TreePlugin.class.getSimpleName();


	// NEW_TREE_STR: String used for creating new Tree elements
	// Example (CoreASM specs):   T := tree [root, [children]]
	// public static final String NEW_TREE_STR = "tree";

	// Keywords used in the grammar rule add child NODE to NODE
	public static final String ADD_STR = "add";
	public static final String CHILD_STR = "child";
	public static final String TO_STR = "to";
	public static final String AT_STR = "at";
	public static final String REMOVE_STR = "remove";
	public static final String FROM_STR = "from";
	public static final String MAKE_STR = "make";
	public static final String INTO_STR = "into";
	public static final String TREE_STR = "tree";



	// Options of the TreePlugin:	
	// Name of the option for setting the Traversal mode 
	// The legal values for the option are specified in the class TreeNodeElement
	public static final String TREE_TRAVERSAL_OPT = "TREE_TRAVERSAL";


	// Format of the string built by the toString() method of TreeNodeElement instances
	// The legal values for the option are specified in the class TreeNodeElement
	public static final String TREE_OUTPUT_STRING_OPT = "TREE_OUTPUT_STRING";

	// Format of the list used for creating new TreeNodeElement instances
	public static final String LIST_FOR_TREES_OPT = "LIST_FOR_TREES";

	// Values for LIST_FOR_TREES_OPT option
	protected static String LIST_FOR_TREES_OPT_SHORT = "short";
	protected static String LIST_FOR_TREES_OPT_LONG = "long";
	protected static String LIST_FOR_TREES_OPT_DEFAULT = LIST_FOR_TREES_OPT_SHORT;


	// Background offered by this plugin
	private TreeBackgroundElement treeBackground; 

	protected String inputListFormatStr = null;
	protected String treeTraversalModeStr = null;
	protected String treeOutputStringFormatStr = null;



	// Interface VocabularyExtender
	private Map<String, BackgroundElement> backgrounds = null;
	private Map<String, FunctionElement> functions = null;

	// Parsers (ParserPlugin)
	private Map<String, GrammarRule> parsers = null;

	// Keywords (Used by CoreASM in the construction of the lexer
	private final String[] keywords = {ADD_STR, CHILD_STR, TO_STR, AT_STR, REMOVE_STR, FROM_STR, MAKE_STR, INTO_STR, TREE_STR};
	private final String[] operators = {};

	// XXX REMOVE
	// private Parser<Node>[] treeTermParserArray = new Parser[1];
	// private Parser<Node> treeTermParser = ParserTools.lazy("TreeTerm", treeTermParserArray);

	private HashSet<String> dependencyList = new HashSet<String>();


	public static AbstractStorage getAbstractStorage() {
		return instance.capi.getStorage();
	} // getAbstractStorage

	public static ControlAPI getCAPI() {
		return instance.capi;
	} // getCAPI



	public TreePlugin() {
		dependencyList.add("ListPlugin");
		treeBackground = new TreeBackgroundElement();
		optionsReader = this;
		instance = this;
	} // TreePlugin


	@Override
	public void initialize() throws InitializationFailedException {
		// empty initialize method
	} // void initialize

	@Override
	public Set<String> getDependencyNames() {
		return dependencyList;
	} // getDependencyNames

	@Override
	public VersionInfo getVersionInfo() {
		return TreePlugin.VERSION_INFO;
	} // VersionInfo getVersionInfo


	public Set<Parser<? extends Object>> getLexers() {
		// No particular lexers.
		return Collections.emptySet();
	} // getLexers


	@SuppressWarnings({ "unchecked", "serial" })
	public Map<String, GrammarRule> getParsers() {		
		if(parsers == null) {
			parsers = new HashMap<String, GrammarRule>();

			// The reference to the KernelServices will be used to access the
			// parsers that have already been defined.
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel")
			.getPluginInterface();

			// Reference to the parser for TERMS
			Parser<Node> termParser = kernel.getTermParser();

			// pTools used to communicate with the jParsec framework
			ParserTools pTools = ParserTools.getInstance(capi);


			/*
			 * REMOVE FROM HERE ...

			Parser<Object[]> createTreeParser_step1 = pTools.seq(
					pTools.getKeywordParser(TreePlugin.NEW_TREE_STR, PLUGIN_NAME),
					optionalDelim,
					termParser,
					optionalDelim);


			//Parser<Node> createTreeParser
			treeTermParserArray[0] = Parsers.mapn("TreeTerm", 
					new Parser[] {createTreeParser_step1},
					new ParseMapN<Node>(PLUGIN_NAME) {
						public Node map(Object... vals) {
							Node node = new TreeTermNode();
							addChildren(node, vals);
							node.setScannerInfo(node.getFirstCSTNode());
							return node;
						}} // new ParseMapN<Node>
			); // Parsers.mapn

			parsers.put(treeTermParserArray[0].toString(),
					new GrammarRule(treeTermParserArray[0].toString(), "'"+ NEW_TREE_STR +"' Term ",
							treeTermParserArray[0], PLUGIN_NAME));

			parsers.put("BasicTerm", new GrammarRule("TreeBasicTerm", 
					"TreeTerm", treeTermParserArray[0], PLUGIN_NAME));

			 * ... TO HERE
			 */


			// RULES
			// make LIST into tree T
			Parser<Node> makeTreeParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser(MAKE_STR, PLUGIN_NAME),
					termParser,
					pTools.getKeywParser(INTO_STR, PLUGIN_NAME),
					pTools.getKeywParser(TREE_STR,PLUGIN_NAME),
					termParser
			}).map(
			new ParserTools.ArrayParseMap(PLUGIN_NAME) {
				public Node map(Object... vals) {
					Node node = new MakeTreeRuleNode();
					addChildren(node, vals);
					return node;
				}}
			);

			parsers.put("MakeTreeRule", 
					new GrammarRule("MakeTreeRule",
							"'" + MAKE_STR +"' Term  '" + INTO_STR + "' '"+ TREE_STR +"' Term", makeTreeParser, PLUGIN_NAME));

			parsers.put("Rule",	
					new GrammarRule("Rule", makeTreeParser.toString(), makeTreeParser, PLUGIN_NAME));


			// RULES
			// add child NODE to NODE (
			Parser<Node> addChildToParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser(ADD_STR, PLUGIN_NAME),
					pTools.getKeywParser(CHILD_STR, PLUGIN_NAME),
					termParser,
					pTools.getKeywParser(TO_STR, PLUGIN_NAME),
					termParser,
					pTools.seq(
							pTools.getKeywParser(AT_STR, PLUGIN_NAME),
							termParser
					).optional()
			}).map(
			new ParserTools.ArrayParseMap(PLUGIN_NAME) {

				public Node map(Object... vals) {
					Node node = new AddChildToRuleNode();
					addChildren(node, vals);
					return node;
				}} //ParseMapN
			); // Parsers.mapn 

			parsers.put("AddChildToRule", 
					new GrammarRule("AddChildToRule",
							"'" + ADD_STR +"' '"+ CHILD_STR + "' Term '" + TO_STR + "' Term ('" + AT_STR + "' Term)?", addChildToParser, PLUGIN_NAME));

			parsers.put("Rule",	
					new GrammarRule("Rule", "AddChildToRule", addChildToParser, PLUGIN_NAME));


			// RULES
			// remove child NODE from NODE 
			Parser<Node> removeChildFromParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser(REMOVE_STR, PLUGIN_NAME),
					pTools.getKeywParser(CHILD_STR, PLUGIN_NAME),
					termParser,
					pTools.getKeywParser(FROM_STR, PLUGIN_NAME),
					termParser
			}).map(
			new ParserTools.ArrayParseMap(PLUGIN_NAME) {

				public Node map(Object... vals) {
					Node node = new RemoveChildFromRuleNode();
					addChildren(node, vals);
					return node;
				}} //ParseMapN
			); // Parsers.mapn 


			parsers.put("RemoveChildFromRule", 
					new GrammarRule("RemoveChildFromRule",
							"'"+REMOVE_STR + "' '"+ CHILD_STR + "' Term '"+FROM_STR +"' Term", removeChildFromParser, PLUGIN_NAME));



			// RULES
			// remove child at Term from NODE 
			Parser<Node> removeChildAtParser = Parsers.array(
					new Parser[] {
					pTools.getKeywParser(REMOVE_STR, PLUGIN_NAME),
					pTools.getKeywParser(CHILD_STR, PLUGIN_NAME),
					pTools.getKeywParser(AT_STR, PLUGIN_NAME),
					termParser,
					pTools.getKeywParser(FROM_STR, PLUGIN_NAME),
					termParser
			}).map(
			new ParserTools.ArrayParseMap(PLUGIN_NAME) {

				public Node map(Object... vals) {
					Node node = new RemoveChildAtRuleNode();
					addChildren(node, vals);
					return node;
				}} //ParseMapN
			); // Parsers.mapn 


			parsers.put("RemoveChildAtRule", 
					new GrammarRule("RemoveChildAtRule",
							"'"+REMOVE_STR + "' '"+ CHILD_STR + "' '"+AT_STR+"' Term '"+FROM_STR +"' Term", removeChildFromParser, PLUGIN_NAME));


			parsers.put("Rule",	
					new GrammarRule("TreeRules", 
							"MakeTreeRule" + "|" + "AddChildToRule" + "|" + "RemoveChildFromRule" + "|"+ "RemoveChildAtRule", 
							Parsers.or(makeTreeParser, addChildToParser, removeChildFromParser, removeChildAtParser), PLUGIN_NAME));


		} // if parsers==null

		return parsers;
	} // getParsers

	@Override
	public String[] getKeywords() {
		return keywords;
	} // getKeywords

	@Override
	public String[] getOperators() {
		return operators;
	} // getOperators

	@Override
	public Parser<Node> getParser(String nonterminal) {
		return null;
	} // getParser

//	protected static List<Update> processInternalUpdates(List<InternalUpdate> listOfUpdates, 
//			Interpreter interpreter, ScannerInfo info) {
//		List<Update> result = new LinkedList<Update>();
//
////		System.err.println("------------------\n processInternalUpdates");
////		int i=1;
//		for (InternalUpdate iu : listOfUpdates) {
////			System.err.println("** UPDATE - " + (i++));
////			System.err.println("loc: " + iu.loc);
////			System.err.println("value: " + iu.value);
////			System.err.println("interpreter: " + interpreter.getSelf());
////			System.err.println("scannerinfo: " + info);
//			Element value = iu.value == null? Element.UNDEF : iu.value;
//			Update update = new Update(iu.loc, value, Update.UPDATE_ACTION, interpreter.getSelf(), info);
//			result.add(update);
//		}
////		System.err.println("-------------------------------");
//		return result;
//	} // processInternalUpdates

	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos)
	throws InterpreterException {
		try{
			UpdateMultiset updates = new UpdateMultiset();

			if (pos instanceof MakeTreeRuleNode) {
				// Create a new TreeNodeElement
				MakeTreeRuleNode node = (MakeTreeRuleNode) pos;

				// If the value has not bee evaluated, evaluate it
				ASTNode listNode = node.getFirst();
				if( ! listNode.isEvaluated())
					return listNode;

				ASTNode termNode = node.getSecond();
				if(! termNode.isEvaluated())
					return termNode;
				
				
				Location loc = termNode.getLocation();

				if(loc != null) {

					// We need a ListElement in order to initialize the tree
					if (listNode.getValue() instanceof ListElement ) {
						ListElement list = (ListElement) listNode.getValue();
						TreeNodeElement treeNode = createTreeFromList(list);
						
						
						List<InternalUpdate> internalUpdates = treeNode.getTreeUpdates();
						List<Update> l = InternalUpdate.processInternalUpdates(internalUpdates, interpreter, pos.getScannerInfo());

						Update u = new Update(loc, treeNode, Update.UPDATE_ACTION, interpreter.getSelf(), pos.getScannerInfo());

						updates.addAll(l);
						updates.add(u);

						pos.setNode(null, updates, null); // treeNode);


						// make list into tree t
						// alla fine in loc(t) albero creato dal list processing.	
					} else {
						throw new InterpreterException(PLUGIN_NAME + ": ListElement expected, found a " + listNode.getValue().getClass().getSimpleName());
					} // if pos instance of ... else ...
				} else {
					capi.error("Cannot store the tree in a non-location");
				} // if loc!= null
			} else if (pos instanceof AddChildToRuleNode) {
				AddChildToRuleNode node = (AddChildToRuleNode)  pos;
				ASTNode childNode = node.getFirst();
				
				TreeNodeElement child;  
				if(! childNode.isEvaluated())
					return childNode;
				else {
					if(! (childNode.getValue() instanceof TreeNodeElement) ) {
						child = new TreeNodeElement(childNode.getValue());
					} else {
						child = (TreeNodeElement) childNode.getValue();
					}
				} // else
					


				ASTNode parentNode = node.getSecond();
				if(! parentNode.isEvaluated())
					return parentNode;
				else if(! (parentNode.getValue() instanceof TreeNodeElement) ) {
					throw new InterpreterException(PLUGIN_NAME + ": TreeNodeElement expected, found a " + parentNode.getValue().getClass().getSimpleName());
				}
				
				TreeNodeElement parent = (TreeNodeElement) parentNode.getValue();
				

				ASTNode posNode = parentNode.getNext();
				if(posNode == null) {
					parent.add(child);
				} else {
					if (! posNode.isEvaluated())
						return posNode;
					else if (! (posNode.getValue() instanceof NumberElement ) )
						throw new InterpreterException(PLUGIN_NAME + ": NumberElement expected, found a " + posNode.getValue().getClass().getSimpleName());

					NumberElement ne = (NumberElement) posNode.getValue();
					parent.insert(child, ne);
				}
				List<InternalUpdate> internalUpdates = parent.getTreeUpdates();
				List<Update> l = InternalUpdate.processInternalUpdates(internalUpdates, interpreter, pos.getScannerInfo());
				// System.err.println("N UPDATES: " + l.size());
				updates.addAll(l);
				// Pass the information back
				// pos.setNode(null, updates, parent);
				pos.setNode(null, updates, null);
			} else if (pos instanceof RemoveChildFromRuleNode) {
				RemoveChildFromRuleNode node = (RemoveChildFromRuleNode) pos;


				ASTNode firstNode = node.getFirst();

				if(! firstNode.isEvaluated())
					return firstNode;
				else if (! (firstNode.getValue() instanceof TreeNodeElement))
					throw new InterpreterException(PLUGIN_NAME + ": TreeNodeElement expected, found a " + firstNode.getValue().getClass().getSimpleName());


				ASTNode secondNode = firstNode.getNext();
				if(!secondNode.isEvaluated())
					return secondNode;
				else if (! (secondNode.getValue() instanceof TreeNodeElement)) {
					throw new InterpreterException(PLUGIN_NAME + ": TreeNodeElement expected, found a " + secondNode.getValue().getClass().getSimpleName());
				}

				TreeNodeElement pa = (TreeNodeElement) secondNode.getValue();
				TreeNodeElement ch = (TreeNodeElement) firstNode.getValue();
				
				pa.removeChild(ch);

				List<InternalUpdate> internalUpdates = pa.getTreeUpdates();
				List<Update> l = InternalUpdate.processInternalUpdates(internalUpdates, interpreter, pos.getScannerInfo());
				updates.addAll(l);


				// pos.setNode(null, null, pa);
				pos.setNode(null, updates, null);
			} else if (pos instanceof RemoveChildAtRuleNode) {
				RemoveChildAtRuleNode node = (RemoveChildAtRuleNode) pos;
				ASTNode firstNode = node.getFirst();

				if(! firstNode.isEvaluated())
					return firstNode;
				else if (! (firstNode.getValue() instanceof NumberElement))
					throw new InterpreterException(PLUGIN_NAME + ": NumberElement expected, found a " + firstNode.getValue().getClass().getSimpleName());

				ASTNode secondNode = firstNode.getNext();
				if(!secondNode.isEvaluated())
					return secondNode;
				else if (! (secondNode.getValue() instanceof TreeNodeElement)) {
					throw new InterpreterException(PLUGIN_NAME + ": TreeNodeElement expected, found a " + secondNode.getValue().getClass().getSimpleName());
				}

				TreeNodeElement pa = (TreeNodeElement) secondNode.getValue();
				NumberElement idx = (NumberElement) firstNode.getValue();

				pa.removeChildAtIndex(idx);

				List<InternalUpdate> internalUpdates = pa.getTreeUpdates();
				List<Update> l = InternalUpdate.processInternalUpdates(internalUpdates, interpreter, pos.getScannerInfo());
				updates.addAll(l);

				pos.setNode(null, updates, null);
			} // if

			return pos;
		} catch (IllegalArgumentException ex) {
			throw new InterpreterException(ex.getMessage());
		} // catch		
	} // interpret

	/*
	 * When trees are created using 'short lists', 
	 * the method checks whether the list(s) passed to the tree constructor
	 * 'tree' represent(s) nodes with children (trees) or list(s) of children whose
	 * parent is a node with an 'undef' value. The parameter list corresponds
	 * to the argument of the tree constructor 'tree'.
	 * 
	 */
	protected boolean isNodeWithChildren(ListElement list) {

		int nElements = list.intSize();

		boolean withChildren = false;

		if(nElements == 2) {

			// If the first node is a ListElement or a TreeNodeElement, then
			// the parameter list represents children of a parent node with
			// an undef value.
			// For example tree [ [1, 2], ...]
			boolean firstElementIsNotAList = !(list.get(1) instanceof ListElement 
					|| list.get(1) instanceof TreeNodeElement );


			boolean secondElementIsAList = (list.get(2) instanceof ListElement);

			// For example, withChildren is true in the following cases:
			//    tree [1, [2, 3]] [2, []]
			withChildren = firstElementIsNotAList && secondElementIsAList;
		} // if nElements == 2;

		return withChildren;
	} // isNodeWithChildren


	protected void addChildrenToNodeShort(TreeNodeElement node, ListElement childrenList) {
		if(childrenList == null || childrenList.intSize() == 0)
			return;

		for(int idx = 1; idx <= childrenList.intSize(); idx++) {
			Element child = childrenList.get(idx);
			if (child instanceof ListElement) {
				node.add(createTreeFromShortList((ListElement) child)); 
			} else if (child instanceof TreeNodeElement){
				node.add((TreeNodeElement) child);
			} else {
				node.add(new TreeNodeElement(child));
			} // if ... else
		} // for
	} // addChildrenToNode

	protected TreeNodeElement createTreeFromShortList(ListElement list) {
		if(list == null)
			return null;


		if(isNodeWithChildren(list)) {
			TreeNodeElement tree = new TreeNodeElement(list.get(1));
			ListElement childrenList = (ListElement) list.get(2);
			addChildrenToNodeShort(tree, childrenList);
			return tree;
		} else {
			TreeNodeElement tree = new TreeNodeElement();
			addChildrenToNodeShort(tree, list);
			return tree;
		} // if ... else

	} // createTreeFromShortList

	/*
	 * Creates a new TreeNodeElement from a list prepared according to the
	 * long list format, i.e. each node is represented by a list
	 * (value, (list of children))
	 */
	protected TreeNodeElement createTreeFromLongList(ListElement list) throws IllegalArgumentException {
		if(list == null)
			return null;

		if(list.isEmpty())
			return new TreeNodeElement();

		// First check: list must have two elements AND the second element must be a list
		if(list.intSize() != 2 || ! (list.get(2) instanceof ListElement))
			throw new IllegalArgumentException("TreePlugin. Error in the list argument");


		Element firstValue = list.get(1);
		Element secondValue = list.get(2);

		// Second check: if the first element is a tree then the list must be empty
		if(firstValue instanceof TreeNodeElement) {
			if(!(secondValue instanceof ListElement) || ! ( (ListElement) secondValue).isEmpty())
				throw new IllegalArgumentException("TreePlugin. Error in the list" + 
				" argument: trees can only have empty lists of children.");
			return (TreeNodeElement) firstValue;
		} 



		TreeNodeElement tree = new TreeNodeElement(firstValue);
		addChildrenToNodeLong(tree, ((ListElement) secondValue));

		return tree;
	} // createTreeFromLongList


	/*
	 * Used for creating new trees when the input string list is in LONG format.
	 * In the long format each child is represented by a list [value, [list of children]].
	 * If the value is undef, then the list is [undef, [list of children]].
	 * If the list of children is empty, then the list is [value, []].
	 */
	protected void addChildrenToNodeLong(TreeNodeElement tree, ListElement childrenList) 
	throws IllegalArgumentException {
		if(childrenList == null || childrenList.intSize() == 0)
			return;

		for(int idx = 1; idx <= childrenList.intSize(); idx++) {
			Element child = childrenList.get(idx);
			if(! (child instanceof ListElement))
				throw new IllegalArgumentException("TreePlugin. Malformed list for creating trees.");

			tree.add(createTreeFromLongList((ListElement) child));
		} // for
	} // addChildrenToNodeLong



	public String getInputListFormatOption() {
		// PRE: capi is not null	
		if ( inputListFormatStr == null ) {
			inputListFormatStr = capi.getProperty(LIST_FOR_TREES_OPT);
			if (inputListFormatStr == null) {
				inputListFormatStr = LIST_FOR_TREES_OPT_DEFAULT;
			} // 2nd if
		} // if listFormat == null
		return inputListFormatStr;
	} // getListFormat()


	public String getTreeTraversalOption() {
		if (treeTraversalModeStr == null) {
			treeTraversalModeStr = capi.getProperty(TREE_TRAVERSAL_OPT);
		} // if

		return treeTraversalModeStr;
	} // getTreeTraversalOption


	public String getOutputStringFormatOption() {
		if(treeOutputStringFormatStr == null) {
			treeOutputStringFormatStr = capi.getProperty(TREE_OUTPUT_STRING_OPT);
		} // if

		return treeOutputStringFormatStr;

	} // getOutputStringFormatOption


	protected TreeNodeElement createTreeFromList(ListElement list) {			
		if(list == null)
			return null;

		TreeNodeElement tree = new TreeNodeElement(); 

		String listFormat = getInputListFormatOption();		

		if(listFormat.equals(LIST_FOR_TREES_OPT_LONG)) {
			tree = createTreeFromLongList(list);
		} else if (listFormat.equals(LIST_FOR_TREES_OPT_SHORT)) { // currently there's only another option: short
			tree = createTreeFromShortList(list);
		} else {
			tree = createTreeFromShortList(list);
		} // if listFormat...
		return tree;
	} // createTreeFromList

	@Override
	public Set<String> getBackgroundNames() {
		return getBackgrounds().keySet();
	} // getBackgroundNames


	@Override
	public Map<String, BackgroundElement> getBackgrounds() {
		if (backgrounds == null) {
			backgrounds = new HashMap<String, BackgroundElement>();

			backgrounds.put(TreeBackgroundElement.TREE_BACKGROUND_NAME, treeBackground);
		} // if
		return backgrounds;
	} // getBackgrounds


	@Override
	public Set<String> getFunctionNames() {
		return getFunctions().keySet();
	} // getFunctionNames

	@Override
	public Map<String, FunctionElement> getFunctions() {
		if (functions == null) {
			functions = new HashMap<String, FunctionElement>();

			functions.put(TreeRootFunctionElement.TREE_ROOT_FUNC_NAME, new TreeRootFunctionElement());

			functions.put(TreeLeavesFunctionElement.TREE_LEAVES_FUNC_NAME, new TreeLeavesFunctionElement());

			functions.put(EnumerateTreeFunctionElement.ENUM_NODES_FUNC_NAME, new EnumerateTreeFunctionElement(false));			
			functions.put(EnumerateTreeFunctionElement.ENUM_VALUES_FUNC_NAME, new EnumerateTreeFunctionElement(true));


			functions.put(BFTFunctionElement.BFT_FUNC_NAME, new BFTFunctionElement(true));
			functions.put(BFTFunctionElement.BFT_NODES_FUNC_NAME, new BFTFunctionElement(false));

			functions.put(DFTFunctionElement.DFT_FUNC_NAME, new DFTFunctionElement(true));
			functions.put(DFTFunctionElement.DFT_NODES_FUNC_NAME, new DFTFunctionElement(false));

//			functions.put(TreeNodeValueFunctionElement.TREE_NODE_VALUE_FUNC_NAME, new TreeNodeValueFunctionElement());
//
//			functions.put(GetFirstFunctionElement.GET_FIRST_FUNC_NAME, new GetFirstFunctionElement());
//			functions.put(GetParentFunctionElement.GET_PARENT_FUNC_NAME, new GetParentFunctionElement());
//			functions.put(GetNextFunctionElement.GET_NEXT_FUNC_NAME, new GetNextFunctionElement());

		} // if functions == null
		return functions;
	} // getFunctions


	@Override
	public Set<String> getRuleNames() {
		return Collections.emptySet();
	} // getRuleNames


	@Override
	public Map<String, RuleElement> getRules() {
		return null;
	} // getRules


	@Override
	public Set<String> getUniverseNames() {
		return Collections.emptySet();
	} // getUniverseNames


	@Override
	public Map<String, UniverseElement> getUniverses() {
		return null;
	} // getUniverses

	
//	public static List<InternalUpdate> getNodeUpdates(TreeNodeElement aNode) {
//		return aNode.getNodeUpdates();
//	} // getNodeUpdates
//	
//	public static List<InternalUpdate> getTreeUpdates(TreeNodeElement aNode) {
//		return aNode.getTreeUpdates();
//	} // getTreeUpdates
		
	public static List<Update> getUpdatesFromNode(TreeNodeElement aNode, Interpreter interpreter, ScannerInfo info) {
		List<InternalUpdate> internalUpdates = aNode.getTreeUpdates();
		List<Update> l = InternalUpdate.processInternalUpdates(internalUpdates, interpreter, info);
		return l;
	} // getUpdatesFromNode
	
	
	public static Element getTempValueOfNode(TreeNodeElement aNode) {
		return aNode.getTempValue();
	} // getTemValueOfNode
	
	
	
} // TreePlugin.java
