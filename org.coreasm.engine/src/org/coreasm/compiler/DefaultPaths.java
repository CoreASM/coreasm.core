package org.coreasm.compiler;

public class DefaultPaths extends CompilerPathConfig {

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
		return "plugins.sttic";
	}

	@Override
	public String pluginDynamicPkg() {
		return "plugins.dynamic";
	}

	@Override
	public String rulePkg() {
		return "Rules";
	}
	
	@Override
	public String runtimeProvider(){
		return runtimePkg() + ".RuntimeProvider.getRuntime()";
	}
}
