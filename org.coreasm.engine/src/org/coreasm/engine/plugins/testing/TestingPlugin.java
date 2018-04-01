package org.coreasm.engine.plugins.testing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.ParserTools.ArrayParseMap;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;

public class TestingPlugin extends Plugin implements ParserPlugin {

	private static final VersionInfo version = new VersionInfo(0, 1, 1, "");
	public static final String PLUGIN_NAME = TestingPlugin.class.getSimpleName();
	private final String[] keywords = {"PARAM", "test"};
	private final String[] operators = {};

	private Map<String, GrammarRule> parsers = null;
	
	@Override
	public VersionInfo getVersionInfo() {
		return version;
	}

	@Override
	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, GrammarRule> getParsers() {

		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel").getPluginInterface();

			Parser<Node> ruleParser = kernel.getRuleParser();
			Parser<Node> termParser = kernel.getTermParser();

			ParserTools pTools = ParserTools.getInstance(capi);

			Parser<Node> testParser = Parsers.array(
					new Parser[]{
						pTools.getKeywParser("test", PLUGIN_NAME),
						pTools.getOprParser("="),
						Parsers.array(ruleParser).or(Parsers.array(termParser))
			}).map(new TestingParseMap());
			
			Parser<Node> paramkeyw = pTools.getKeywParser("PARAM", PLUGIN_NAME);
			
			Parser<Node> testParamExpr = Parsers.array(
					new Parser[]{
						paramkeyw,
						pTools.getIdParser()
					}).map(
						new ParserTools.ArrayParseMap(PLUGIN_NAME) {
							@Override
							public Node map(Object[] from) {
								Node node = new ASTNode(PLUGIN_NAME, ASTNode.EXPRESSION_CLASS, "PARAM", null, ((Node) from[0]).getScannerInfo());
								addChildren(node, from);
								return node;
							}});
					
			parsers.put("PARAM", new GrammarRule("PARAM", "'PARAM' ID", testParamExpr, PLUGIN_NAME));
			parsers.put("BasicTerm",  new GrammarRule("TestingBasicTerm", "PARAM", testParamExpr, PLUGIN_NAME));
			
			Parser<Node> testParamRule = Parsers.array(
					new Parser[]{
							paramkeyw,
							pTools.getIdParser()
					}).map(new ArrayParseMap(PLUGIN_NAME){
						public Node map(Object[] vals){
							Node node = new ASTNode(PLUGIN_NAME, ASTNode.RULE_CLASS, "PARAM", null, ((Node)vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
					});
			
			parsers.put("Header", new GrammarRule("Header", "TestHeader", testParser, PLUGIN_NAME));
			parsers.put("Rule", new GrammarRule("Rule", "ParamRule", testParamRule, PLUGIN_NAME));

		}
		return parsers;
	}

	@Override
	public Parser<Node> getParser(String nonterminal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getKeywords() {
		return keywords;
	}

	@Override
	public String[] getOperators() {
		return operators;
	}

	@Override
	public void initialize() throws InitializationFailedException {
		// TODO Auto-generated method stub

	}
	
	public static class TestingParseMap extends ArrayParseMap{
		public TestingParseMap(){
			super(PLUGIN_NAME);
		}
		
		@Override
		public Node map(Object[] vals){
			Node node = new ASTNode(PLUGIN_NAME, ASTNode.RULE_CLASS, "TestRule", null, ((Node) vals[0]).getScannerInfo());
			addChildren(node, vals);
			return node;
		}
		
		@Override
		public void addChild(Node parent, Node child){
			parent.addChild(child);
		}
	}

}
