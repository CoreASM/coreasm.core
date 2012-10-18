package org.coreasm.eclipse.debug.ui;

import java.util.ArrayList;

import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.coreasm.eclipse.debug.core.model.ASMThread;
import org.coreasm.eclipse.debug.core.model.ASMValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;

public class WatchExpressionDelegate implements IWatchExpressionDelegate {
	private IValue value = null;
	private DebugException exception = null;

	@Override
	public void evaluateExpression(final String expression, IDebugElement context, IWatchExpressionListener listener) {
		try {
			ASMStackFrame frame = null;
			if (context instanceof ASMDebugTarget)
				frame = (ASMStackFrame)((ASMDebugTarget)context).getThreads()[0].getTopStackFrame();
			else if (context instanceof ASMThread)
				frame = (ASMStackFrame)((ASMThread)context).getTopStackFrame();
			else if (context instanceof ASMStackFrame)
				frame = (ASMStackFrame) context;
			if (frame == null) {
				listener.watchEvaluationFinished(null);
				return;
			}
			ArrayList<IVariable> matchingVariables = new ArrayList<IVariable>();
			for (IVariable variable : frame.getVariables()) {
				if (variable.getName().equals(expression) || variable.getName().startsWith(expression + "("))
					matchingVariables.add(variable);
			}
			IVariable[] variables = new IVariable[matchingVariables.size()];
			matchingVariables.toArray(variables);
			if (variables.length == 1 && variables[0].getName().equals(expression))
				value = variables[0].getValue();
			else if (variables.length > 0)
				value = new ASMValue(frame, variables);
		} catch (DebugException e) {
			exception = e;
		}
		listener.watchEvaluationFinished(new IWatchExpressionResult() {
			
			@Override
			public boolean hasErrors() {
				return value == null;
			}
			
			@Override
			public IValue getValue() {
				return value;
			}
			
			@Override
			public String getExpressionText() {
				return expression;
			}
			
			@Override
			public DebugException getException() {
				return exception;
			}
			
			@Override
			public String[] getErrorMessages() {
				if (value == null)
					return new String[] { expression + " cannot be resolved to a function" };
				return null;
			}
		});
	}

}
