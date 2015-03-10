package org.coreasm.compiler.mainprogram;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.LibraryEntryType;
import org.coreasm.compiler.classlibrary.MemoryInclude;

public class MainClass extends MemoryInclude {
	public MainClass(CompilerEngine engine){
		super(engine, "Main", "Kernel", LibraryEntryType.BASE);
	}
	@Override
	protected String buildContent(String entryName) throws Exception {
		String stateMachineName = engine.getPath().getEntryName(LibraryEntryType.DYNAMIC, "StateMachine", "Kernel");
		String content = "";
		if(engine.getPath().basePkg() != "")
			content += "package " + getPackage(entryName) + ";\n";
		content += "public class Main{\n";
		content += "\tpublic static void main(String[] args){\n";
		content += "\t\t(new Thread(new " + stateMachineName + "())).start();\n";
		content += "\t}\n";
		content += "}\n";
		return content;
	}

}
