package org.coreasm.compiler.plugins.kernel.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.components.preprocessor.Information;
import org.coreasm.compiler.components.preprocessor.SynthesizeRule;
import org.coreasm.compiler.components.preprocessor.Trigger;
import org.coreasm.engine.interpreter.ASTNode;

//stores rule declarations.
//rule declarations are stored in the field "RuleDeclaration"
//with the structure | null ---<rulename>---> null --<ruleparams> ---> null
/**
 * Part of an analysis to find rule signatures.
 * Promotes rule signatures through the declaration to store them at the root node and
 * builds rule signatures out of id nodes.
 * @author Markus Brenner
 *
 */
public class SignatureTransformer implements SynthesizeRule {

	@Override
	public Map<String, Information> transform(ASTNode n,
			List<Map<String, Information>> children) {
		
		if(n.getGrammarClass().equals("Declaration") && n.getGrammarRule().equals("RuleSignature")){
			HashMap<String, Information> result = new HashMap<String, Information>();
			
			//first node is id
			if(children.size() <= 0) return null;
			
			String rname = (String) children.get(0).get("ID").getValue();
			if(rname == null) return null;
			
			List<String> params = new ArrayList<String>();
			
			for(int i = 1; i < children.size(); i++){
				String s = (String) children.get(i).get("ID").getValue();
				if(s == null) return null;
				params.add(s);
			}
			
			Information inf = new Information();
			inf.setValue(null, rname);
			for(String s : params){
				inf.setValue(null, rname, s);
			}
			result.put("RuleDeclaration", inf);
			
			return result;
		}
		else if (n.getGrammarClass().equals("Declaration") && n.getGrammarRule().equals("RuleDeclaration")){
			HashMap<String, Information> result = new HashMap<String, Information>();
			
			if(children.size() < 0) return null;
			
			Information inf = children.get(0).get("RuleDeclaration");
			
			if(inf == null) return null;
			
			result.put("RuleDeclaration", inf);		
			
			return result;
		}
		else if(n.getGrammarClass().equals("CoreASM") && n.getGrammarRule().equals("CoreASM")){
			//collect all rules and put them in the head
			HashMap<String, Information> result = new HashMap<String, Information>();
			Information resultInfo = new Information();
			
			for(Map<String, Information> m : children){
				Information inf = m.get("RuleDeclaration");
				if(inf == null) continue;
				List<String> tmp = inf.getChildren();
				if(tmp == null) continue;
				for(String s : tmp){
					//if there is a rule collision, give up and throw an error
					//not optimal, but it shouldn't happen normally
					if(resultInfo.getInformation(s) != null) return null;
					resultInfo.setValue(null, s);
					List<String> params = inf.getInformation(s).getChildren();
					
					if(params != null){
						for(String p : params){
							resultInfo.setValue(null, s, p);
						}
					}
				}
			}
			result.put("RuleDeclaration", resultInfo);
			return result;
		}
		
		
		return null;
	}

	@Override
	public List<Trigger> getTriggers() {
		List<Trigger> triggers = new ArrayList<Trigger>();
		triggers.add(new Trigger(null, "Declaration", "RuleSignature", null));
		triggers.add(new Trigger(null, "Declaration", "RuleDeclaration", null));
		triggers.add(new Trigger(null, "CoreASM", "CoreASM", null));
		return triggers;
	}

}
