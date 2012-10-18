package org.coreasm.eclipse.debug.core.model;

import java.util.Arrays;

import org.coreasm.engine.absstorage.BooleanElement;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * This class represents the value of an ASM function. It can also be used as a variable container.
 * @author Michael Stegmaier
 *
 */
public class ASMValue extends ASMDebugElement implements IValue {
	private String value;
	private IVariable[] variables;

	public ASMValue(ASMStackFrame frame, String value) {
		super((ASMDebugTarget) frame.getDebugTarget());
		this.value = value;
	}
	
	public ASMValue(ASMStackFrame frame, IVariable[] variables) {
		this(frame, Arrays.toString(variables));
		this.variables = variables;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		if (variables == null) {
			try {
				Double.parseDouble(value);
				return "Number";
			} catch (NumberFormatException e) {
				if (BooleanElement.TRUE_NAME.equals(value) || BooleanElement.FALSE_NAME.equals(value))
					return "Boolean";
				else if (value.startsWith("\"") && value.endsWith("\""))
					return "String";
				else if (!value.isEmpty() && Character.isLetterOrDigit(value.charAt(0)))
					return "Enumeration";
			}
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
		return value;
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return variables;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return variables != null;
	}

}
