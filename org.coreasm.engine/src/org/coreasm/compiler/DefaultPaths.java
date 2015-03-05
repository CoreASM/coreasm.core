package org.coreasm.compiler;

public class DefaultPaths implements CompilerPathConfig {

	@Override
	public String basePkg() {
		return "";
	}

	@Override
	public String runtimePkg() {
		return "CompilerRuntime";
	}

	@Override
	public String pluginStaticPkg() {
		return "plugins.static";
	}

	@Override
	public String pluginDynamicPkg() {
		return "plugins.dynamic";
	}

	@Override
	public String rulePkg() {
		return "Rules";
	}

}
