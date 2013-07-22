/**
 * 
 */
package org.coreasm.engine.plugins.modularity;

import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.interpreter.ScannerInfo;

/**
 * Node for include Statements. This is necessary when using the parser
 * without the engine, so include's don't get replaced before parsing. *
 * 
 * @author Markus
 */
public class IncludeNode extends ASTNode {
	private static final long serialVersionUID = 1L;

	private String filename = null;

	/**
	 * needed to support duplicate
	 * @param self
	 */
	public IncludeNode(IncludeNode self){
		this( self.getPluginName(), self.getToken(), self.getScannerInfo(), self.getConcreteNodeType() );
	}
	
	public IncludeNode(ScannerInfo scannerInfo) {
		super(ModularityPlugin.PLUGIN_NAME, ASTNode.DECLARATION_CLASS, "CoreModule", "IncludeNode", scannerInfo);
	}
	
	public IncludeNode(String pluginName,  
			String token, ScannerInfo scannerInfo, String concreteType){
		super(ModularityPlugin.PLUGIN_NAME, ASTNode.DECLARATION_CLASS, "CoreModule", "", scannerInfo, concreteType);
	}

	public String getFilename() {
		if (filename == null) {
			Node filenameNode = this.getChildNode("alpha");
			String fname = filenameNode.unparse();
			fname = fname.substring(1, fname.length() - 1); // remove quotes
			this.filename = fname;
		}

		return filename;
	}

}