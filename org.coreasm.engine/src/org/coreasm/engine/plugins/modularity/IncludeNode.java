package org.coreasm.engine.plugins.modularity;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

@SuppressWarnings("serial")
public class IncludeNode extends ASTNode {
	public IncludeNode(IncludeNode node){
		super(node);
	}
	
	public IncludeNode(ScannerInfo scannerInfo) {
		super(ModularityPlugin.PLUGIN_NAME, ASTNode.DECLARATION_CLASS, "Include", null, scannerInfo);
	}
	
	public String getFilename() {
		return getFirst().getToken();
	}
}