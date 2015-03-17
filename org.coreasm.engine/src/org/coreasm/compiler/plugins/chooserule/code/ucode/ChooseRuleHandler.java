package org.coreasm.compiler.plugins.chooserule.code.ucode;

import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.chooserule.ChooseRuleNode;

/**
 * Handles the choose rule
 * @author Spellmaker
 *
 */
public class ChooseRuleHandler implements CompilerCodeHandler {

	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		try {
			if(!(node instanceof ChooseRuleNode)) throw new CompilerException("wrong node type in compilation for chooserule");
			ChooseRuleNode chooseRule = (ChooseRuleNode) node;
			
			Map<String, ASTNode> vars = chooseRule.getVariableMap();
			ASTNode doRule = chooseRule.getDoRule();
			ASTNode condition = chooseRule.getCondition();
			ASTNode ifnone = chooseRule.getIfnoneRule();
			
			result.appendLine("//--------------start choose\n");
			//evaluate sources
			int cnt = 0;
			//declare list in which the element lists are stored
			result.appendLine("@decl(java.util.ArrayList<java.util.ArrayList<@RuntimePkg@.Element>>, srclist) = new java.util.ArrayList<java.util.ArrayList<@RuntimePkg@.Element>>();\n");
			for(Entry<String, ASTNode> mapping : vars.entrySet()){
				//evaluate the source of the entry
				result.appendFragment(engine.compile(mapping.getValue(), CodeType.R));
				result.appendLine("@srclist@.add(new java.util.ArrayList<@RuntimePkg@.Element>(((@RuntimePkg@.Enumerable)evalStack.pop()).enumerate()));");
				cnt++;
			}
			
			//note: each of these two forks ends with a state, in which:
			//1. a layer is opened on the localStack
			//2. a selection has been choosen and was pushed to the localStack
			//3. @selected@ contains information, whether a selection could be made
			if(condition == null){
				//remember if choose selected something successfully.
				//default is true, as selecting only fails if an enumerable is empty
				result.appendLine("@decl(boolean, selected) = true;\n");
				result.appendLine("@decl(java.util.ArrayList<@RuntimePkg@.Element>, clist) = null;\n");
				result.appendLine("localStack.pushLayer();\n");
				cnt = 0;
				for(Entry<String, ASTNode> mapping : vars.entrySet()){
					result.appendLine("@clist@ = @srclist@.get(" + cnt + ");\n");
					result.appendLine("if(@clist@.size() <= 0){\n");
					result.appendLine("@selected@ = false;\n");
					result.appendLine("localStack.put(\"" + mapping.getKey() + "\", @RuntimePkg@.Element.UNDEF);\n");
					result.appendLine("}\n");
					result.appendLine("else{\n");
					result.appendLine("localStack.put(\"" + mapping.getKey() + "\", @clist@.get(@RuntimeProvider@.randInt(@clist@.size())));\n");
					result.appendLine("}\n");
					cnt++;
				}
			}
			else{
				//remember if choose selected something successfully
				result.appendLine("@decl(boolean, selected) = true;\n");
				//create a list of possible combinations
				result.appendLine("@decl(java.util.ArrayList<java.util.ArrayList<@RuntimePkg@.Element>>, combinations) = new java.util.ArrayList<java.util.ArrayList<@RuntimePkg@.Element>>();\n");
				//test all possible combinations NOTE: Highly inefficient
				result.appendLine("localStack.pushLayer();\n");
				//open for loops
				cnt = 0;
				for(Entry<String, ASTNode> mapping : vars.entrySet()){
					result.appendLine("@decl(java.util.ArrayList<@RuntimePkg@.Element>, clist_" + cnt + ") = @srclist@.get(" + cnt + ");\n");
					result.appendLine("if(@clist_" + cnt + "@.size() <= 0){\n");
					result.appendLine("@selected@ = false;\n");
					result.appendLine("}\n");
					result.appendLine("for(@decl(int, i_" + cnt + ") = 0; @i_" + cnt + "@ < @clist_" + cnt + "@.size() && @selected@; @i_" + cnt + "@++){\n");
					result.appendLine("localStack.put(\"" + mapping.getKey() + "\", @clist_" + cnt + "@.get(@i_" + cnt + "@));\n");
					cnt++;
				}
				//innermost of the for loops: all temporary values are on the local stack. now execute the guard
				result.appendFragment(engine.compile(condition, CodeType.R));
				result.appendLine("if(evalStack.pop().equals(@RuntimePkg@.BooleanElement.TRUE)){\n");
				//add combination
				cnt = 0;
				result.appendLine("@decl(java.util.ArrayList<@RuntimePkg@.Element>, tmpcombination) = new java.util.ArrayList<@RuntimePkg@.Element>();\n");
				for(int i = 0; i < vars.size(); i++){
					result.appendLine("@tmpcombination@.add(@clist_" + cnt + "@.get(@i_" + cnt + "@));\n");
					cnt++;
				}
				result.appendLine("@combinations@.add(@tmpcombination@);\n");
				result.appendLine("}\n");
				
				//close for loops; effectively doing nothing
				for(int i = 0; i < vars.size(); i++){
					result.appendLine("}\n");
				}
				
				//determine the final state; selected is false, if it is false or the combination list is empty
				//if it is true, then put a random combination to the local state
				result.appendLine("@selected@ = @selected@ && @combinations@.size() >= 1;\n");
				result.appendLine("if(@selected@){\n");
				//combinations exist, randomly choose one and put it to the localstack
				result.appendLine("@decl(java.util.ArrayList<@RuntimePkg@.Element>, selectedcomb) = @combinations@.get(@RuntimeProvider@.randInt(@combinations@.size()));\n");
				cnt = 0;
				for(Entry<String, ASTNode> mapping : vars.entrySet()){
					result.appendLine("localStack.put(\"" + mapping.getKey() + "\", @selectedcomb@.get(" + cnt + "));\n");
					cnt++;
				}
				result.appendLine("}\n");
				//as the combination list might grow large, we explicitly clear it here to safe memory
				result.appendLine("@combinations@ = null;\n");
			}
			result.appendLine("if(@selected@){\n");
			//a selection was made; execute the code
			result.appendFragment(engine.compile(doRule, CodeType.U));
			//the result is now on the stack, clear up the localstack
			result.appendLine("localStack.popLayer();\n");
			result.appendLine("}\n");
			result.appendLine("else{\n");
			//close the scope on the localstack
			result.appendLine("localStack.popLayer();\n");
			if(ifnone == null){
				//nothing happens
				result.appendLine("evalStack.push(new @RuntimePkg@.UpdateList());\n");
			}
			else{
				result.appendFragment(engine.compile(ifnone, CodeType.U));
			}
			result.appendLine("//--------------end choose\n");
			result.appendLine("}\n");
		} catch (Exception e) {
			throw new CompilerException("invalid code generated");
		}
	}

}
