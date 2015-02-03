package org.coreasm.testing.modules;

import org.coreasm.testing.TestingHelperModule;

public class EvalStackModule extends TestingHelperModule {
	public EvalStackModule() {
		moduleFile = "EvalStack.mod";
	}
	@Override
	public String modifyCode(String code) {
		return code.replace("CompilerRuntime.EvalStack", "EvalStack");
	}
}
