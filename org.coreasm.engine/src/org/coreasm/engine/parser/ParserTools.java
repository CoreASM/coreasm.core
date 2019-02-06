package org.coreasm.engine.parser;

import java.util.*;
import java.util.function.Function;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Token;
import org.jparsec.Tokens.Fragment;
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
	
	public static void removeInstance(ControlAPI capi) {
		if (instances != null)
			instances.remove(capi);
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
	
	public void init(String [] keywords, String[] ops, Set<Parser<?>> lexers)
	{
		if (initialized == true)
			throw new EngineError("Cannot re-initialize ParserTools.");
		
		keywParsers = new HashMap<String, Parser<Node>>();
		oprParsers = new HashMap<String, Parser<Node>>();
		
		terminals_keyw = Terminals.operators(ops).words(Scanners.IDENTIFIER).keywords(Arrays.asList(keywords)).build();
		tokenizer_keyw = terminals_keyw.tokenizer();
		tokenizer_id = Terminals.Identifier.TOKENIZER;
		
		// Convert set with lexers into a list, because the keyword tokenizer must be first
		// and the identifier tokenizer must be last.
		List<Parser<?>> _lexers = new LinkedList<>(lexers);
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
			Parser<Node> parser = terminals_keyw.token(keyword).map(from -> {
                int index = from.index();
                return new Node(
                    pluginName,
                    from.toString(),
                    new ScannerInfo(index),
                    Node.KEYWORD_NODE
                );
            });
			keywParsers.put(keyword, parser);
		}
		return keywParsers.get(keyword);
	}
	
	public Parser<Node> getOprParser(String operator) {
		if ( ! oprParsers.containsKey(operator) ) {
			Parser<Node> parser = terminals_keyw.token(operator).map(from -> {
                int index = from.index();
                return new Node(
                        "Kernel",
                        from.toString(),
                        new ScannerInfo(index),
                        Node.OPERATOR_NODE
                        );
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
	implements Function<Object[], Node>
	{
		String pluginname;
		
		public ArrayParseMap(String pluginname) {
			this.pluginname = pluginname;
		}
		
		public ArrayParseMap(Plugin plugin) {
			this(plugin.getName());
		}

		@Override
		public abstract Node apply(Object[] from);

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

		public RuleSignatureParseMap() {
			super(Kernel.PLUGIN_NAME);
		}
		
		@Override
		public Node apply(Object[] from) {
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

		public RuleDeclarationParseMap() {
			super(Kernel.PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] vals) {
			ScannerInfo info = ((Node)vals[0]).getScannerInfo();
			
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

		public CoreASMParseMap() {
			super(Kernel.PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] vals) {
			ScannerInfo info;
			
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

		public FunctionRuleTermParseMap() {
			super(Kernel.PLUGIN_NAME);
		}

		@Override
		public Node apply(Object[] v) {
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

		@Override
		public Node apply(Token v)
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
	public Parser<Object[]> seq(Parser<?>...parsers) {
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
	public Parser<Object[]> seq(String name, Parser<?>...parsers) {
		return Parsers.array(parsers);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser*
	 *
	 * @param parser parser to be repeated
	 */
	public Parser<Object[]> many(Parser<?> parser) {
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
	public Parser<Object[]> many(String name, Parser<?> parser) {
		return parser.many().map(List::toArray);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )*
	 *
	 * @param parser parser to be repeated
	 */
	public Parser<Object[]> star(Parser<?> parser) {
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
	public Parser<Object[]> star(String name, Parser<?> parser) {
		return this.many(name, parser);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: ( parser delimiter )+
	 *
	 * @param parser parser to be repeated at least once
	 */
	public Parser<Object[]> plus(Parser<?> parser) {
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
	public Parser<Object[]> plus(String name, Parser<?> parser) {
		return parser.many1().map(List::toArray);
	}
	
	/**
	 * Returns a parser P that is:
	 * <p>
	 * P: parser delimiter (',' delimiter parser delimiter)*
     * 
	 * @param parser parser to be repeated at least once
	 */
	public Parser<Object[]> csplus(Parser<?> parser) {
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
	public Parser<Object[]> csplus(Parser<?> commaParser, Parser<?> parser) {
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
	public Parser<Object[]> csplus(String name, Parser<?> parser) {
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
	public Parser<Object[]> csplus(String name, Parser<?> commaParser, Parser<?> parser) {
		Parser<Object[]> repeated = 
			star(
				seq(
					commaParser,
					//getOptionalDelimiterParser(),
					parser
					//getOptionalDelimiterParser()
					)
				);

		return seq(name,
				parser,
				//getOptionalDelimiterParser(),
				repeated
				);
	}

	
	
}
