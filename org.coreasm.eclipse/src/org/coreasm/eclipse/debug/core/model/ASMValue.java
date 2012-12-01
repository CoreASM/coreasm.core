package org.coreasm.eclipse.debug.core.model;

import java.util.Arrays;

import org.coreasm.engine.absstorage.Element;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * This class represents the value of an ASM function. It can also be used as a variable container.
 * @author Michael Stegmaier
 *
 */
public class ASMValue extends ASMDebugElement implements IValue {
	private Element value;
	private String valueString;
	private IVariable[] variables;

	public ASMValue(ASMStackFrame frame, Element value) {
		this(frame, value.toString());
		this.value = value;
	}
	
	public ASMValue(ASMStackFrame frame, String valueString) {
		super((ASMDebugTarget) frame.getDebugTarget());
		this.valueString = valueString;
	}
	
	public ASMValue(ASMStackFrame frame, IVariable[] variables) {
		this(frame, Arrays.toString(variables));
		this.variables = variables;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		if (variables == null) {
			if (value != null)
				return value.getBackground();
		}
		else {
			if (variables.length > 0)
				return "[" + variables[0].getReferenceTypeName() + "]";
			else
				return "[]";
		}
		
		return null;
	}

	@Override
	public String getValueString() throws DebugException {
		return valueString;
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		if (variables == null)
			return new IVariable[0];
		return variables;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return variables != null && variables.length > 0;
	}

}
