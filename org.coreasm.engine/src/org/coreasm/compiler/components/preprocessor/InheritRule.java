package org.coreasm.compiler.components.preprocessor;

import java.util.List;
import java.util.Map;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.compiler.components.preprocessor.Information;
import org.coreasm.compiler.components.preprocessor.Trigger;
/**
 * Inheritance information.
 * An inherit rule transforms information at a node into
 * new information at the child nodes
 * @author Markus Brenner
 *
 */
public interface InheritRule {
	/**
	 * Executes the inheritance rule.
	 * The rule should still check the node on which is it called. It should
	 * not assume that it is called on only the nodes specified by getTriggers.
	 * @param node The node this rule is executed on
	 * @param nodeInformation The Information of the node
	 * @return A list of information mappings, containing an entry for each child (may be null, if no mapping for this child exists)
	 */
	public List<Map<String, Information>> transform(ASTNode node, Map<String, Information> nodeInformation);
	/**
	 * Denotes the trigger conditions of this rule
	 * @return A list of triggers
	 */
	public List<Trigger> getTriggers();
}
