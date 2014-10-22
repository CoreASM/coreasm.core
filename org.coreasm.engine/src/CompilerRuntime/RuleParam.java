package CompilerRuntime;

public interface RuleParam {	
	public CompilerRuntime.Element evaluate(CompilerRuntime.LocalStack localStack) throws Exception;
	public Rule getUpdateResponsible();
	public void setParams(java.util.Map<String, CompilerRuntime.RuleParam> params);
}
