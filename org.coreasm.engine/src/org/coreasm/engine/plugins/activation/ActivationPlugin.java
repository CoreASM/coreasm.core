package org.coreasm.engine.plugins.activation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.plugins.options.CompilerOptionsPlugin;
import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.BooleanElement;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.PluginAggregationAPI;
import org.coreasm.engine.absstorage.PluginAggregationAPI.Flag;
import org.coreasm.engine.absstorage.PluginCompositionAPI;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.KernelServices;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.InterpreterPlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plug-in provides two new keywords "activate" and "deactivate" each with
 * a location as parameter. The semantics is that if at least one activate
 * statement is called in a step, the value of the parameter gets true. If only
 * deactivate is called, the parameter gets false.
 * <p>
 * This is realized by introducing a new update instruction "activationAction".
 * <p>
 * The plugin provides the following syntax:
 * <p>
 * <code><b>activate</b></code><code> <controlled function> </code><br>
 * <code><b>deactivate</b></code><code> <controlled function> </code>
 * <p>
 * This plugin was created in order to be able to run the specification of
 * TerminationDetection as presented in "The Modeling Companion", Springer in
 * CoreASM.
 * 
 * @author Alexander Raschke
 * 
 */
public class ActivationPlugin extends Plugin implements ParserPlugin, InterpreterPlugin, Aggregator {

	protected static final Logger logger = LoggerFactory.getLogger(ActivationPlugin.class);

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 0, 1, "");

	public static final String PLUGIN_NAME = ActivationPlugin.class.getSimpleName();

	private Map<String, GrammarRule> parsers = null;

	private final String[] keywords = { "activate", "deactivate" };
	private final String[] operators = {};

	public static final String ACTIVATION_ACTION = "activationAction";
	public static final String[] UPDATE_ACTIONS = { ACTIVATION_ACTION };

	private final CompilerPlugin compilerPlugin = new CompilerOptionsPlugin(this);

	@Override
	public CompilerPlugin getCompilerPlugin() {
		return compilerPlugin;
	}

	public ActivationPlugin() {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.plugin.Plugin#initialize()
	 */
	@Override
	public void initialize() {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.plugin.ParserPlugin#getKeywords()
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.plugin.ParserPlugin#getOperators()
	 */
	public String[] getOperators() {
		return operators;
	}

	public Set<Parser<? extends Object>> getLexers() {
		return Collections.emptySet();
	}

	/**
	 * Always returns <code>null</code>.
	 * 
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParser(java.lang.String)
	 */
	public Parser<Node> getParser(String nonterminal) {
		if (parsers != null)
			return parsers.get(nonterminal).parser;
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.plugin.ParserPlugin#getParsers()
	 */
	public Map<String, GrammarRule> getParsers() {
		if (parsers == null) {
			parsers = new HashMap<String, GrammarRule>();
			KernelServices kernel = (KernelServices) capi.getPlugin("Kernel").getPluginInterface();

			ParserTools pTools = ParserTools.getInstance(capi);
			Parser<Node> funcRuleTermParser = kernel.getFunctionRuleTermParser();

			// "activate" ID
			Parser<Node> activateParser = Parsers
					.array(new Parser[] { pTools.getKeywParser("activate", PLUGIN_NAME), funcRuleTermParser })
					.map(new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object[] vals) {
							Node node = new ActivationNode(((Node) vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
					});
			Parser<Node> deactivateParser = Parsers
					.array(new Parser[] { pTools.getKeywParser("deactivate", PLUGIN_NAME), funcRuleTermParser })
					.map(new ParserTools.ArrayParseMap(PLUGIN_NAME) {
						public Node map(Object[] vals) {
							Node node = new ActivationNode(((Node) vals[0]).getScannerInfo());
							addChildren(node, vals);
							return node;
						}
					});
			Parser<Node> activationParser = Parsers.or(activateParser, deactivateParser);
			parsers.put("Rule", new GrammarRule("ActivationRule", "'(activate|deactivate)' FunctionRuleTerm",
					activationParser, PLUGIN_NAME));
		}

		return parsers;
	}

	@Override
	public ASTNode interpret(Interpreter interpreter, ASTNode pos) throws InterpreterException {
		ASTNode nextPos = pos;
		String gClass = pos.getGrammarClass();

		// if activation related rule
		if (gClass.equals(ASTNode.RULE_CLASS)) {
			// activation rule
			if (pos instanceof ActivationNode) {
				// Activation rule wrapper
				ActivationNode actNode = (ActivationNode) pos;

				nextPos = actNode.getUnevaluatedTerm();

				// no unevaluated terms
				if (nextPos == null) {
					// set next pos to current position
					nextPos = pos;

					Location loc = actNode.getLocation();
					if (loc != null && (loc.isModifiable == null || loc.isModifiable == true)) {
						// prepare (multi) update set
						Update u = new Update(loc, BooleanElement.valueOf(actNode.isActivate()),
								ActivationPlugin.ACTIVATION_ACTION, interpreter.getSelf(), pos.getScannerInfo());
						UpdateMultiset us = new UpdateMultiset(u);
						// set vul for node
						pos.setNode(null, us, null);
					} else
						throw new CoreASMError(
								"activate/deactivate expect a modifiable function term." + Tools.getEOL()
										+ "Failed to set " + actNode.getLocation() + " to " + actNode.isActivate()
										+ " because " + actNode.getLocation() + " was " + actNode.getValue() + ".",
								actNode);
				} 
			}

		}
		return nextPos;
	}

	/**
	 * This function returns a list of the update actions provided by this
	 * plugin. In this particular case, it is only ACTIVATION_ACTION.
	 */
	@Override
	public String[] getUpdateActions() {
		return UPDATE_ACTIONS;
	}

	/**
	 * This aggregator looks if for a location at least one update to true
	 * exists. If this is the case, one update to true is generated, else the
	 * corresponding location is set to false.
	 * 
	 * @param pluginAgg
	 *            plugin aggregation API object.
	 */
	@Override
	public void aggregateUpdates(PluginAggregationAPI pluginAgg) {
		// all locations on which contain activation actions
		Set<Location> locsToAggregate = pluginAgg.getLocsWithAnyAction(ACTIVATION_ACTION);

		// for all locations to aggregate
		for (Location l : locsToAggregate) {
			UpdateMultiset uis = pluginAgg.getLocUpdates(l);

			// search for the first activate Update that sets the trigger to
			// true
			Update activateUpdate = null;
			for (Update u : uis) {
				if (u.value.equals(BooleanElement.TRUE)) {
					activateUpdate = u;
					break;
				}
			}
			// if at least one activate exists, we take this one as the
			// resultantUpdate
			if (activateUpdate != null) {
				pluginAgg.addResultantUpdate(activateUpdate, this);
			}
			// if no activate update exists, we take the first update (which
			// must be a deactivate update!)
			else {
				pluginAgg.addResultantUpdate(uis.iterator().next(), this);
			}
			// in any case, we mark all update instructions as successful
			for (Update u : uis) {
				pluginAgg.flagUpdate(u, Flag.SUCCESSFUL, this);
			}
		}
	}

	/**
	 * no special composition is necessary for this plugin.
	 * 
	 * @see org.coreasm.engine.plugin.Aggregator#compose(PluginCompositionAPI)
	 */
	public void compose(PluginCompositionAPI compAPI) {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coreasm.engine.VersionInfoProvider#getVersionInfo()
	 */
	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

}