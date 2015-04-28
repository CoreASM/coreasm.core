package CompilerRuntime;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;

public interface RuleParam {	
	public Location evaluateL(CompilerRuntime.LocalStack localStack) throws Exception;
	public Element evaluateR(CompilerRuntime.LocalStack localStack) throws Exception;
	public Rule getUpdateResponsible();
	public void setParams(java.util.Map<String, CompilerRuntime.RuleParam> params);
}
