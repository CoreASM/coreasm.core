package org.coreasm.engine.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.Tokens;
import org.codehaus.jparsec.Tokens.Fragment;
import org.codehaus.jparsec.functors.Map;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineError;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugin.Plugin;

public class ParserTools 
{
	static private java.util.Map<ControlAPI, ParserTools> instances = null;

	static public ParserTools getInstance(ControlAPI capi) {
		if (instances == null)
			instances = new HashMap<ControlAPI, ParserTools>();
		if ( ! instances.containsKey(capi))
			instances.put(capi, new ParserTools());
		return instances.get(capi);
	}
	
	// TERMINALS
	private Terminals terminals_keyw = null;
	
	// TOKENIZER
	private Parser<?> tokenizer_keyw = null;
	private Parser<Fragment> tokenizer_id = null;
	private Parser<Object> tokenizer = null;
	private Parser<Void> ignored = null;
	
	// PARSER
	private Parser<Node> idParser = null;
	private java.util.Map<String, Parser<Node>> keywParsers = null;
	private java.util.Map<String, Parser<Node>> oprParsers = null;
	
	// MISC
	private boolean initialized;
	
	private ParserTools() {
		initialized = false;
	}
	
	public void init(String [] keywords, String[] operators, Set<Parser<? extends Object>> lexers)
	{
		if (initialized == true)
			throw new EngineError("Cannot re-initialize ParserTools.");
		
		keywParsers = new HashMap<String, Parser<Node>>();
		oprParsers = new HashMap<String, Parser<Node>>();
		
		terminals_keyw = Terminals.caseSensitive(operators, keywords);
		tokenizer_keyw = terminals_keyw.tokenizer();
		tokenizer_id = Terminals.Identifier.TOKENIZER;
		
		// Convert set with lexers into a list, because the keyword tokenizer must be first
		// and the identifier tokenizer must be last.
		List<Parser<? extends Object>> _lexers = new LinkedList<Parser<? extends Object>>(lexers);
		_lexers.add(0, tokenizer_keyw);	
		_lexers.add(tokenizer_id);
		tokenizer = Parsers.or(_lexers);
		
		ignored = Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

		initialized = true;
	}
	
	public Terminals getKeywTerminals() {
		return terminals_keyw;
	}
	
	public Parser<Node> getIdParser() {
		if (idParser == null)
			idParser = Terminals.Identifier.PARSER.token().map(new IdentifierMap());
		return idParser;
	}
	
	public Parser<Node> getKeywParser(String keyword, final String pluginName) {
		if ( ! keywParsers.containsKey(keyword) ) {
			Parser<Node> parser = terminals_keyw.token(keyword).map(new Map() {
				@Override
				public Object map(Object from) {
					int index = -1;
					if (from instanceof Token)
						index = ((Token)from).index();
					return new Node(
						pluginName,
						from.toString(),
						new ScannerInfo(index),
						Node.KEYWORD_NODE
					);
				}
			});
			keywParsers.put(keyword, parser);
		}
		return keywParsers.get(keyword);
	}
	
	public Parser<Node> getOprParser(String operator) {
		if ( ! oprParsers.containsKey(operator) ) {
			Parser<Node> parser = terminals_keyw.token(operator).map(new Map() {
				@Override
				public Object map(Object from) {
					int index = -1;
					if (from instanceof Token)
						index = ((Token)from).index();
					return new Node(
							"Kernel",
							from.toString(),
							new ScannerInfo(index),
							Node.OPERATOR_NODE
							);
				}
			});
			oprParsers.put(operator, parser);
		}
		return oprParsers.get(operator);
	}
	
	public Parser<Object> getTokenizer() {
		return tokenizer;
	}
	
	public Parser<Void> getIgnored() {
		return ignored;
	}
	
	
	
	
	
	public static abstract class ArrayParseMap
	implements Map<Object[], Node>
	{
		String pluginname;
		
		public ArrayParseMap(String pluginname) {
			this.pluginname = pluginname;
		}
		
		public ArrayParseMap(Plugin plugin) {
			this(plugin.getName());
		}
		
		public abstract Node map(Object[] from);
		
		/**
		 * Assumes all the children are instances of {@link Node} and
		 * adds all of them as children of parent.
		 *  
		 * @param parent parent node
		 * @param children array of child nodes
		 */
		public void addChildren(Node parent, Object[] children) {
			
			for (Object child: children) {
				if (child != null) {
					if (child instanceof Object[])
						addChildren(parent, (Object[])child);
					else
					// otherwise child should be a Node!
						addChild(parent, (Node)child);
				}
			}
		}
		
		/**
		 * Simply adds <code>child</code> to the children of
		 * <code>parent</code>. This method is used by 
		 * {@link #addChildren(Node, Object[])} and can 
		 * be overriden to customize the tree construction.
		 * 
		 *  @param parent
		 *  @param child 
		 */
		public void addChild(Node parent, Node child) {
			parent.addChild(child);
		}
		
	}
	
	
	public static class RuleSignatureParseMap
	extends ArrayParseMap
	{
		private static final long serialVersionUID = 1L;

		public RuleSignatureParseMap() {
			super(Kernel.PLUGIN_NAME);
		}
		
		@Override
		public Node map(Object[] from) {
			Node node = new ASTNode(
					"Kernel",
					ASTNode.DECLARATION_CLASS,
					"RuleSignature",
					null,
					((Node)from[0]).getScannerInfo()
				);
			addChildren(node,from);
			return node;
		}
	}
	

	public static class RuleDeclarationParseMap
	extends ArrayParseMap
	{
		private static final long serialVersionUID = 1L;

		public RuleDeclarationParseMap() {
			super(Kernel.PLUGIN_NAME);
		}
		
		public Node map(Object... vals) {
			ScannerInfo info = null;
			info = ((Node)vals[0]).getScannerInfo();
			
			Node node = new ASTNode(
					null,
					ASTNode.DECLARATION_CLASS,
					Kernel.GR_RULEDECLARATION,
					null,
					info
					);

			for (int i=0; i < vals.length; i++) {
				Node child = (Node)vals[i];
				if (child != null)
					// to give proper names to ASTNode children:
					if (child instanceof ASTNode) {
						if (((ASTNode)child).getGrammarClass().equals("RuleSignature"))
							node.addChild("alpha", child);
						else
							node.addChild("beta", child);
					} else
						node.addChild(child);
			}
			
			return node;
		}

	}
	
	
	
	public static class CoreASMParseMap
	extends ArrayParseMap
	{
		private static final long serialVersionUID = 1L;

		public CoreASMParseMap() {
			super(Kernel.PLUGIN_NAME);
		}
		
		public Node map(Object... vals) {
			ScannerInfo info = null;
			
			// consider the possiblity of starting with a 
			// comment or whitespace
			if (vals[0] != null && ((Node)vals[0]).getToken().equals("CoreASM"))
				info = ((Node)vals[0]).getScannerInfo();
			else
				info = ((Node)vals[1]).getScannerInfo();
			
			ASTNode rootNode = new ASTNode(
					"Kernel", 
					"CoreASM", 
					Kernel.GR_COREASM, 
					null, 
					info
					);
			rootNode.setParent(null);

			addChildren(rootNode, vals);
			
			return rootNode;
		}
	}
	
	
	
	public static class FunctionRuleTermParseMap
	extends ArrayParseMap
	{
		private static final long serialVersionUID = 1L;

		public FunctionRuleTermParseMap() {
			super(Kernel.PLUGIN_NAME);
		}
		
		public Node map(Object... v) {
			Node node = new FunctionRuleTermNode(((Node)v[0]).getScannerInfo());
			node.addChild("alpha", (Node)v[0]); // ID
			
			for (int i=1; i < v.length; i++) {
				if (v[i] != null && v[i] instanceof ASTNode) {
					// Then it should be a TupleTerm
					for (Node n: ((Node)v[i]).getChildNodes())
						if (n instanceof ASTNode) 
							node.addChild("lambda", n);
						else 
							node.addChild(n);
				}
			}
			return node;
		}

	}
	
	
	public static final class IdentifierMap
	extends ParseMap<Token, Node> {

		public IdentifierMap() {
			super("Kernel");
		}

		public Node map(Token v)
		{
			return new ASTNode(
							pluginName, 
							ASTNode.ID_CLASS, 
							"ID", 
							v.toString(),
							new ScannerInfo(v),
							Node.GENERAL_ID_NODE
							);
		}
		
	}


	
	//=======================================================================
	// KOPIERT VON ParserTools
	//=======================================================================
	
	/**
	 * Returns a parser that is a sequence of the given parsers. 
	 * The resulting parser will return an array of Objects.
	 * <p>
	 * P: P1 ... Pn
	 *  
	 * @param parsers parsers to be sequenced
	 */
	public Parser<Object[]> seq(Parser<? extends Object>...parsers) {
		return seq("parser", parsers);
	}
	
	/**
	 * Returns a parser that is a sequence of the given parsers. 
	 * The resulting parser will return an array of Objects.
	 * <p>
	 * P: P1 ... Pn
	 *
	 * @param name name of the new parser
	 * @param parsers parsers to be sequenced
	 */
	public Parser<Object[]> seq(String name, Parser<? extends Object>...parsers) {
		Parser<Object[]> seqParser = Parsers.array(parsers);
		/*.new ParseMapN<Object[]>("") {

			public Object[] map(Object... vals) {
				Object[] nodes = new Object[vals.length];
				
				for (int i=0; i < vals.length; i++) 
					nodes[i] = vals[i];
				
				return nodes;
			}
			
		});*/
			
		return seqParser;
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser*
	 *
	 * @param parser parser to be repeated
	 */
	public Parser<Object[]> many(Parser<? extends Object> parser) {
		return many("parser", parser);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser*
	 * 
	 * @param name name of the new parser
	 * @param parser parser to be repeated
	 */
	public Parser<Object[]> many(String name, Parser<? extends Object> parser) {
		//Parser<Object[]> result = Parsers.many(name, Object.class, parser);
		Parser<Object[]> result = parser.many().map( new Map<List<? extends Object>, Object[]>() {
			@Override
			public Object[] map(List<? extends Object> from) {
				return from.toArray();
			}
		});
		return result;
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )*
	 *
	 * @param parser parser to be repeated
	 */
	public Parser<Object[]> star(Parser<? extends Object> parser) {
		return star("parser", parser);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )*
	 * 
	 * @param name name of the new parser
	 * @param parser parser to be repeated
	 */
	public Parser<Object[]> star(String name, Parser<? extends Object> parser) {
//		Parser<Object[]> result = seq(name, parser, getOptionalDelimiterParser()).many(Object[].class).map(
//				new Map<Object[][], Object[]>() {
//
//					public Object[] map(Object[][] v) {
//						ArrayList list = new ArrayList();
//						for (Object[] arr: v) 
//							for (Object obj: arr)
//								list.add(obj);
//						return list.toArray();
//					}
//				}
//		);
		Parser<Object[]> result = this.many(name, parser);
		
		return result;
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )+
	 *
	 * @param parser parser to be repeated at least once
	 */
	public Parser<Object[]> plus(Parser<? extends Object> parser) {
		return plus("parser", parser);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )+
	 * 
	 * @param name name of the new parser
	 * @param parser parser to be repeated at least once
	 */
	//public Parser<Object[]> plus(String name, Parser<? extends Object> parser) {
	//	return plus(name, parser, getOptionalDelimiterParser());
	//}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )+
	 * 
	 * @param parser parser to be repeated at least once
	 * @param delimiter the delimiter parser
	 */
	//public Parser<Object[]> plus(Parser<? extends Object> parser, Parser<Node> delimiter) {
	//	return plus("parser", parser, delimiter);
	//}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )+
	 * 
	 * @param name name of the new parser
	 * @param parser parser to be repeated at least once
	 * @param delimiter the delimiter parser [REMOVED]
	 */
	public Parser<Object[]> plus(String name, Parser<? extends Object> parser) {
		/*Parser<Object[]> result = seq(name, parser, delimiter).many1(Object[].class).map(
				new Map<Object[][], Object[]>() {

					public Object[] map(Object[][] v) {
						ArrayList list = new ArrayList();
						for (Object[] arr: v) 
							for (Object obj: arr)
								list.add(obj);
						return list.toArray();
					}
				}
		);*/
		Parser<Object[]> result = parser.many1().map(new Map<List<? extends Object>, Object[]> () {
			@Override
			public Object[] map(List<? extends Object> from) {
				return from.toArray();
			}
		});
		
		return result;
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser delimiter (',' delimiter parser delimiter)*
     * 
	 * @param parser parser to be repeated at least once
	 */
	public Parser<Object[]> csplus(Parser<? extends Object> parser) {
		return csplus("parser", parser);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser delimiter (commaParser delimiter parser delimiter)*
     * 
     * @param commaParser the parser that parses the comma or any other symbol 
	 * @param parser parser to be repeated at least once
	 */
	public Parser<Object[]> csplus(Parser<? extends Object> commaParser, Parser<? extends Object> parser) {
		return csplus("parser", commaParser, parser);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser delimiter (',' delimiter parser delimiter)*
	 * 
	 * @param name name of the new parser
	 * @param parser parsers to be repeated at least once
	 */
	public Parser<Object[]> csplus(String name, Parser<? extends Object> parser) {
		return csplus(name, getOprParser(","), parser);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser delimiter (commaParser delimiter parser delimiter)*
	 * 
	 * @param name name of the new parser
	 * @param commaParser the parser that parses the comma or any other symbol
	 * @param parser parser to be repeated at least once
	 */
	@SuppressWarnings("unchecked")
	public Parser<Object[]> csplus(String name, Parser<? extends Object> commaParser, Parser<? extends Object> parser) {
		Parser<Object[]> repeated = 
			star(
				seq(
					commaParser,
					//getOptionalDelimiterParser(),
					parser
					//getOptionalDelimiterParser()
					)
				);
		
		Parser<Object[]> result = seq(name, 
				parser, 
				//getOptionalDelimiterParser(),
				repeated
				);
		
		return result;
	}

	
	
}
