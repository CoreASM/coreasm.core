package org.coreasm.engine.kernel;

import org.coreasm.engine.CoreASMError;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UpdateMultiset;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.ScannerInfo;

/**
 * A node holding a constant value that can be used to replace ASTNodes with constant values.
 * @author Michael Stegmaier
 *
 */
public class ConstantValueNode extends ASTNode {
	private static final long serialVersionUID = 1L;

	public ConstantValueNode(ConstantValueNode node) {
		super(node);
		super.setNode(null, null, node.getValue());
	}
	
	public ConstantValueNode(ScannerInfo info, Element value) {
		super(Kernel.PLUGIN_NAME, ASTNode.EXPRESSION_CLASS, "", null, info);
		setValue(value);
	}
	
	public void setValue(Element value) {
		if (value == null)
			throw new CoreASMError("Constant value must not be null", this);
		super.setNode(null, null, value);
	}
	
	@Override
	public void setNode(Location loc, UpdateMultiset updates, Element value) {
	}
}
