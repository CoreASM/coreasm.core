package org.coreasm.compiler.components.mainprogram;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.components.classlibrary.LibraryEntryType;
import org.coreasm.compiler.components.classlibrary.MemoryInclude;

/**
 * The default main class of the compilation unit.
 * This entry will generate the Main.java file in the root
 * of the compilation directory, providing the main entry
 * point.
 * The class will simply provide the main method and start
 * a thread with the state machine.
 * @author Spellmaker
 *
 */
public class MainClass extends MemoryInclude {
	/**
	 * Constructs the entry
	 * @param engine The compiler engine supervising the compilation process
	 */
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
