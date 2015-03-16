package org.coreasm.engine.plugins.number;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.SynthesizeRule;
import org.coreasm.compiler.preprocessor.Trigger;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Part of a value analysis. Calculates (if possible) results of simple
 * numeric operations and stores the result on the operator node
 * @author Markus Brenner
 *
 */
public class NumberValueTransformer implements SynthesizeRule {
	private List<String> ops;
	
	/**
	 * Initializes the rule
	 */
	public NumberValueTransformer(){
		ops = new ArrayList<String>();
		ops.add("+");
		ops.add("-");
		ops.add("*");
		ops.add("/");
		ops.add("div");
		ops.add("%");
		ops.add("^");
		ops.add(">");
		ops.add(">=");
		ops.add("<");
		ops.add("<=");
	}
	
	
	@Override
	public Map<String, Information> transform(ASTNode n,
			List<Map<String, Information>> children) {		
		if(n.getGrammarClass().equals("BinaryOperator") && n.getGrammarRule().equals("")){
			if(!ops.contains(n.getToken())) return null;
			//find out if child node values are known
			Information lhs = children.get(0).get("value");
			Information rhs = children.get(1).get("value");
			
			if(lhs == null || rhs == null) return null;
			
			try{				
				Double val1 = (Double)lhs.getInformation("value").getValue();
				Double val2 = (Double)rhs.getInformation("value").getValue();
				
				String type1 = (String) lhs.getInformation("type").getValue();
				String type2 = (String) rhs.getInformation("type").getValue();
				
				if(type1 != "NUMBER" || type2 != "NUMBER"){
					return null;
				}
				
				Map<String, Information> result = new HashMap<String, Information>();
				Information i = new Information();
				if(n.getToken().equals("+")){
					double res = val1 + val2;
					i.setValue("NUMBER", "type");
					i.setValue(res, "value");
					i.setValue("@NumberElement@.getInstance(" + res + ")", "code");
				}
				else if(n.getToken().equals("-")){
					double res = val1 - val2;
					i.setValue("NUMBER", "type");
					i.setValue(res, "value");
					i.setValue("@NumberElement@.getInstance(" + res + ")", "code");
				}
				else if(n.getToken().equals("*")){
					double res = val1 * val2;
					i.setValue("NUMBER", "type");
					i.setValue(res, "value");
					i.setValue("@NumberElement@.getInstance(" + res + ")", "code");
				}
				else if(n.getToken().equals("/")){
					double res = val1 / val2;
					i.setValue("NUMBER", "type");
					i.setValue(res, "value");
					i.setValue("@NumberElement@.getInstance(" + res + ")", "code");
				}
				else if(n.getToken().equals("div")){
					double res = (val1 - (val1 % val2)) / val2;
					i.setValue("NUMBER", "type");
					i.setValue(res, "value");
					i.setValue("@NumberElement@.getInstance(" + res + ")", "code");
				}
				else if(n.getToken().equals("%")){
					double res = val1 % val2;
					i.setValue("NUMBER", "type");
					i.setValue(res, "value");
					i.setValue("@NumberElement@.getInstance(" + res + ")", "code");
				}
				else if(n.getToken().equals("^")){
					double res = Math.pow(val1, val2);
					i.setValue("NUMBER", "type");
					i.setValue(res, "value");
					i.setValue("@NumberElement@.getInstance(" + res + ")", "code");
				}
				else if(n.getToken().equals(">")){
					boolean res = val1 > val2;
					i.setValue("BOOLEAN", "type");
					i.setValue(res, "value");
					i.setValue("CompilerRuntime.BooleanElement.valueOf(" + res + ")", "code");
				}
				else if(n.getToken().equals(">=")){
					boolean res = val1 >= val2;
					i.setValue("BOOLEAN", "type");
					i.setValue(res, "value");
					i.setValue("CompilerRuntime.BooleanElement.valueOf(" + res + ")", "code");
				}
				else if(n.getToken().equals("<")){
					boolean res = val1 < val2;
					i.setValue("BOOLEAN", "type");
					i.setValue(res, "value");
					i.setValue("CompilerRuntime.BooleanElement.valueOf(" + res + ")", "code");
				}
				else if(n.getToken().equals("<=")){
					boolean res = val1 <= val2;
					i.setValue("BOOLEAN", "type");
					i.setValue(res, "value");
					i.setValue("CompilerRuntime.BooleanElement.valueOf(" + res + ")", "code");
				}
				else{
					return null;
				}
				result.put("value", i);
				return result;
								
			}
			catch(NullPointerException e){
				return null;
			}
			catch(ClassCastException e){
				return null;
			}
		}
		
		
		
		return null;
	}


	@Override
	public List<Trigger> getTriggers() {
		List<Trigger> triggers = new ArrayList<Trigger>();
		triggers.add(new Trigger(null, "BinaryOperator", "", null));
		return triggers;
	}

}
