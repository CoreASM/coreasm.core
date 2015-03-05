package org.coreasm.compiler;

public interface CompilerPathConfig {
	public String basePkg();
	public String runtimePkg();
	public String pluginStaticPkg();
	public String pluginDynamicPkg();
	public String rulePkg();
}
