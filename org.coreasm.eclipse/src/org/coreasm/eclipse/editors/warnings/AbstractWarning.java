package org.coreasm.eclipse.editors.warnings;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.Specification;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.CharacterPosition;
import org.coreasm.engine.parser.Parser;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * An abstract implementation of a warning
 * @author Michael Stegmaier
 *
 */
public abstract class AbstractWarning {
	private final String description;
	private final String data;
	private final int position;
	private final int length;
	
	public AbstractWarning(String description, String data, int position, int length) {
		this.description = description;
		this.data = data;
		this.position = position;
		this.length = length;
	}
	
	public static int calculatePosition(Node node, CharacterPosition charPos, ControlAPI capi, IDocument document) {
		if (capi != null) {
			Parser parser = capi.getParser();
			if (charPos == null && node != null && node.getScannerInfo() != null)
				charPos = node.getScannerInfo().getPos(parser.getPositionMap());
			if (charPos != null) {
				Specification spec = capi.getSpec();
				try {
					int line = charPos.line;
					if (spec != null)
						line = spec.getLine(charPos.line).line;
					return document.getLineOffset(line - 1) + charPos.column - 1;
				} catch (BadLocationException e) {
				}
			}
		}
		if (node != null)
			return node.getScannerInfo().charPosition;
		return 0;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getData() {
		return data;
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getLength() {
		return length;
	}
}
