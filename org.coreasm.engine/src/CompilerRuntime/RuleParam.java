package CompilerRuntime;

import org.coreasm.engine.absstorage.Element;

public interface RuleParam {	
	public Element evaluate(CompilerRuntime.LocalStack localStack) throws Exception;
	public Rule getUpdateResponsible();
	public void setParams(java.util.Map<String, CompilerRuntime.RuleParam> params);
}
