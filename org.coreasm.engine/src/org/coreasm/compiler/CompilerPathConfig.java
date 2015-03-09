package org.coreasm.compiler;

import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.classlibrary.LibraryEntryType;

public abstract class CompilerPathConfig {
	public abstract String basePkg();
	public abstract String runtimePkg();
	public abstract String pluginStaticPkg();
	public abstract String pluginDynamicPkg();
	public abstract String rulePkg();
	public abstract String runtimeProvider();
	public String getEntryName(LibraryEntry entry){
		return getEntryName(entry.getType(), entry.getName(), entry.getSource());
	}
	public String getEntryPath(LibraryEntry entry){
		return getEntryName(entry.getType(), entry.getName(), entry.getSource()).replace(".", "\\") + ".java";
	}
	public String getEntryName(LibraryEntryType type, String entryName, String source){
		switch(type){
		case BASE:
			return basePkg() + ((basePkg() == "") ? "" : ".") + entryName;
		case DYNAMIC:
			return pluginDynamicPkg() + "." + source + "." + entryName;
		case RULE:
			return rulePkg() + "." + entryName;
		case RUNTIME:
			return runtimePkg() + "." + entryName;
		case STATIC:
			return pluginStaticPkg() + "." + source + "." + entryName;
		}
		return "";
	}
}
