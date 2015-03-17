package org.coreasm.compiler;

/**
 * The default path configuration for the compiler.
 * By default, the paths are defined as:
 * <table>
 * <thead>
 * <tr><th>Name</th><th>Path</th></tr>
 * </thead>
 * <tr><th>basePkg</th><th>''</th></tr>
 * <tr><th>runtimePkg</th><th>'CompilerRuntime'</th></tr>
 * <tr><th>pluginStaticPkg</th><th>'plugins.sttic'</th></tr>
 * <tr><th>pluginDynamicPkg</th><th>'plugins.dynamic'</th></tr>
 * <tr><th>rulePkg</th><th>'Rules'</th></tr>
 * <tr><th>runtimeProvider</th><th>runtimePkg() + '.RuntimeProvider.getRuntime()'</th></tr>
 * </table>
 * 
 * @author Spellmaker
 *
 */
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
