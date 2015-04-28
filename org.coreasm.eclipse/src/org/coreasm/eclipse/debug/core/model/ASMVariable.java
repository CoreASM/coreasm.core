package org.coreasm.eclipse.debug.core.model;

import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.absstorage.BooleanBackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * This class represents an ASM function/variable
 * @author Michael Stegmaier
 *
 */
public class ASMVariable extends ASMDebugElement implements IVariable {
	private ASMStackFrame frame;
	private FunctionElement function;
	private String name;
	private IValue value;
	private boolean valueChanged;
	
	public ASMVariable(ASMStackFrame frame, String name, FunctionElement function, IValue value, boolean valueChanged) {
		this(frame, name, value, valueChanged);
		this.function = function;
	}

	public ASMVariable(ASMStackFrame frame, String name, IValue value, boolean valueChanged) {
		super((ASMDebugTarget) frame.getDebugTarget());
		this.frame = frame;
		this.name = name;
		this.value = value;
		this.valueChanged = valueChanged;
	}

	@Override
	public void setValue(String expression) throws DebugException {
		String value = EngineDebugger.getRunningInstance().setValue(name, expression);
		if (value != null) {
			this.value = new ASMValue(frame, value);
			valueChanged = true;
		}
	}

	@Override
	public void setValue(IValue value) throws DebugException {
		this.value = value;
	}

	@Override
	public boolean supportsValueModification() {
		try {
			if (!frame.getThread().getTopStackFrame().equals(frame))
				return false;
			return name.indexOf('(') >= 0 && !name.startsWith("universeElement(") && !name.startsWith("program(") && !(value.getValueString().startsWith("{") && value.getValueString().endsWith("}")) && !(value.getValueString().startsWith("[") && value.getValueString().endsWith("]"));
		} catch (DebugException e) {
		}
		return name.indexOf('(') >= 0 && !name.startsWith("universeElement(") && !name.startsWith("program(");
	}

	@Override
	public boolean verifyValue(String expression) throws DebugException {
		return true;
	}

	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		return true;
	}

	@Override
	public IValue getValue() throws DebugException {
		return value;
	}

	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		if (function != null) {
			if (function.getSignature() != null)
				return function.getSignature().toString();
			else
				return function.getBackground();
		}
		return value.getReferenceTypeName();
	}

	@Override
	public boolean hasValueChanged() throws DebugException {
		return valueChanged;
	}

	@Override
	public String toString() {
		try {
			if (BooleanBackgroundElement.BOOLEAN_BACKGROUND_NAME.equals(value.getReferenceTypeName()))
				return name.substring(name.indexOf('(') + 1, name.indexOf(')'));
			return value.getValueString();
		} catch (DebugException e) {
			return null;
		}
	}
}
