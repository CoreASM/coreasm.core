package org.coreasm.compiler.plugins.kernel.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.SynthesizeRule;
import org.coreasm.compiler.preprocessor.Trigger;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Spawns id information at id nodes
 * @author Markus Brenner
 *
 */
public class IDSpawner implements SynthesizeRule {
	@Override
	public Map<String, Information> transform(ASTNode n,
			List<Map<String, Information>> children) {
		//lift ids on function rule term
		if(n.getGrammarClass().equals("FunctionRule") && n.getGrammarRule().equals("FunctionRuleTerm")){
			if(children.size() <= 0) return null;
			
			Map<String, Information> first = children.get(0);
			if(first == null) return null;
		}
		else if(n.getGrammarClass().equals("Id") && n.getGrammarRule().equals("ID")){
			Information inf = new Information();
			inf.setValue(n.getToken());
			
			Map<String, Information> result = new HashMap<String, Information>();
			result.put("ID", inf);
			return result;
		}
		
		
		//default behaviour is to remove ids after one step
		return null;
	}

	@Override
	public List<Trigger> getTriggers() {
		List<Trigger> triggers = new ArrayList<Trigger>();
		triggers.add(new Trigger(null, "FunctionRule", "FunctionRuleTerm", null));
		triggers.add(new Trigger(null, "Id", "ID", null));
		return triggers;
	}

}
