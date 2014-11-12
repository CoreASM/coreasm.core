package org.coreasm.compiler.plugins.kernel.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.InheritRule;
import org.coreasm.compiler.preprocessor.Trigger;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Part of a rule signature analysis.
 * Creates the rule parameters for the declaration out of id nodes
 * @author Markus Brenner
 *
 */
public class RuleParamInheritRule implements InheritRule {

	@Override
	public List<Map<String, Information>> transform(ASTNode node,
			Map<String, Information> nodeInformation) {
		
		if(node.getGrammarClass().equals("Declaration") && node.getGrammarRule().equals("RuleDeclaration")){
			
			//get the information if available
			Information inf = nodeInformation.get("RuleDeclaration");
			if(inf != null){
				if(inf.getChildren().size() == 1){
					Information rule = inf.getInformation(inf.getChildren().get(0));
					List<String> params = rule.getChildren();
					List<Map<String, Information>> result = new ArrayList<Map<String, Information>>();
					//don't propagate the information to the first node
					result.add(null);
					
					Information list = new Information();
					list.setValue(null, "params");
					for(String s : params){
						list.setValue(null, "params", s);
					}
					
					
					Map<String, Information> currentRule = new HashMap<String, Information>();
					currentRule.put("RuleParameter", list);
					result.add(currentRule);
					return result;					
				}
			}
		}
		else{
			Information inf = nodeInformation.get("RuleParameter");
			if(inf != null){
				List<String> params = inf.getInformation("params").getChildren();
				
				List<Map<String, Information>> result = new ArrayList<Map<String, Information>>();
				for(int i = 0; i < node.getAbstractChildNodes().size(); i++){
					Map<String, Information> tmp = new HashMap<String, Information>();
					Information list = new Information();
					list.setValue(null, "params");
					for(String s : params){
						list.setValue(null, "params", s);
					}
					tmp.put("RuleParameter", list);
					result.add(tmp);
				}
				return result;
			}
		}
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Trigger> getTriggers() {
		List<Trigger> result = new ArrayList<Trigger>();
		result.add(new Trigger(null, "Declaration", "RuleDeclaration", null));
		return result;
	}

}
