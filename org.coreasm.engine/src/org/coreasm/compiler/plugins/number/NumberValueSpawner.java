package org.coreasm.compiler.plugins.number;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.components.preprocessor.Information;
import org.coreasm.compiler.components.preprocessor.SynthesizeRule;
import org.coreasm.compiler.components.preprocessor.Trigger;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Part of a value analysis. Spawns initial information on number nodes, such as their type
 * and value.
 * @author Markus Brenner
 *
 */
public class NumberValueSpawner implements SynthesizeRule{

	@Override
	public Map<String, Information> transform(ASTNode n, List<Map<String, Information>> children) {
		if(n.getGrammarClass().equals("Expression") && n.getGrammarRule().equals("NUMBER")){
			double val = Double.parseDouble(n.getToken());
			
			Information i = new Information();
			i.setValue(val, "value");
			i.setValue("NUMBER", "type");
			i.setValue("@NumberElement@.getInstance(" + val + ")", "code");
						
			Map<String, Information> result = new HashMap<String, Information>();
			result.put("value", i);
			return result;
		}
		
		
		
		return null;
	}

	@Override
	public List<Trigger> getTriggers() {
		List<Trigger> triggers = new ArrayList<Trigger>();
		triggers.add(new Trigger(null, "Expression", "NUMBER", null));
		return triggers;
	}
	
}
