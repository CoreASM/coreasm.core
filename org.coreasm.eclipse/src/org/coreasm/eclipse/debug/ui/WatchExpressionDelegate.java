package org.coreasm.eclipse.debug.ui;

import java.util.ArrayList;

import org.coreasm.eclipse.debug.core.model.ASMDebugTarget;
import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.coreasm.eclipse.debug.core.model.ASMThread;
import org.coreasm.eclipse.debug.core.model.ASMValue;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;

public class WatchExpressionDelegate implements IWatchExpressionDelegate {
	

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
			if (frame == null)
				listener.watchEvaluationFinished(null);
			else
				new EvaluationJob(expression, frame, listener).schedule();
		} catch (DebugException e) {
			listener.watchEvaluationFinished(null);
			return;
		}
	}

	private final class EvaluationJob extends Job {
		private ASMStackFrame frame;
		private String expression;
		private IWatchExpressionListener listener;
		private IValue value = null;
		private DebugException exception = null;
		private String error = null;
		
		public EvaluationJob(String expression, ASMStackFrame frame, IWatchExpressionListener listener) {
			super("EvaluationJob");
			setSystem(true);
			this.frame = frame;
			this.expression = expression;
			this.listener = listener;
		}
		
		@Override
		protected IStatus run(IProgressMonitor arg0) {
			try {
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
				if (value == null) {
					EngineDebugger debugger = EngineDebugger.getRunningInstance();
					if (debugger != null)
						value = new ASMValue(frame, debugger.evaluateExpression(expression, frame.getState()));
				}
			} catch (Exception e) {
				error = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
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
					if (value == null) {
						if (error != null)
							return new String[] { error };
						return new String[] { expression + " cannot be resolved to a function" };
					}
					return null;
				}
			});
			return Status.OK_STATUS;
		}
		
	}
}
