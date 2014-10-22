package CompilerRuntime;

import java.util.concurrent.Callable;

/**
 * An interface representing a CoreASM Rule
 * @author Markus Brenner
 *
 */
public abstract class Rule extends Element implements Callable<RuleResult> {
	protected Element agent;
	protected java.util.ArrayList<CompilerRuntime.RuleParam> params;
	protected CompilerRuntime.LocalStack localStack;
	protected CompilerRuntime.EvalStack evalStack;
	
	@Override
	public String getBackground(){
		return RuleBackgroundElement.RULE_BACKGROUND_NAME;
	}
	
	public void clearResults(){
		localStack = new CompilerRuntime.LocalStack();
		evalStack = new CompilerRuntime.EvalStack();
	}
	public void setAgent(Element a){
		this.agent = a;
	}
	public Element getAgent(){
		return this.agent;
	}
	public void initRule(java.util.ArrayList<CompilerRuntime.RuleParam> params, CompilerRuntime.LocalStack ls){
		this.evalStack = new CompilerRuntime.EvalStack();
		this.params = new java.util.ArrayList<CompilerRuntime.RuleParam>(params);
		if(ls == null){
			this.localStack = new CompilerRuntime.LocalStack();
		}
		else{
			this.localStack = ls;
		}
	}
	
	public Rule getUpdateResponsible(){
		return this;
	}
	
	public abstract Rule getCopy();
	
	public abstract int parameterCount();
}
