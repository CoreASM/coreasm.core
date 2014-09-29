package org.coreasm.engine.plugins.signature;

import java.util.List;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.MapFunction;
import org.coreasm.engine.absstorage.UnmodifiableFunctionException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Interpreter;
import org.coreasm.engine.interpreter.InterpreterException;

public class DerivedMapFunction extends MapFunction {
	
	protected final ControlAPI capi;
	protected final List<String> params;
	protected final ASTNode expr;
	
	public DerivedMapFunction(ControlAPI capi, List<String> params, ASTNode expr) {
		super(null);
		this.capi = capi;
		this.params = params;
		this.expr = expr;
	}
	
	@Override
	public Element getValue(List<? extends Element> args) {
		Element value = super.getValue(args);
		if (value == null) {
			value = evaluateExpression(args);
			try {
				super.setValue(args, value);
			} catch (UnmodifiableFunctionException e) {
			}
		}
		return value;
	}
	
	public Element evaluateExpression(List<? extends Element> args) {
		Element result = Element.UNDEF;
		if (args.size() == params.size()) {
			Interpreter interpreter = capi.getInterpreter().getInterpreterInstance();
			bindArguments(interpreter, args);
			
			synchronized(this) {
				ASTNode exprCopy = (ASTNode)interpreter.copyTree(expr);
				try {
					interpreter.interpret(exprCopy, interpreter.getSelf());
					if (exprCopy.getValue() != null)
						result = exprCopy.getValue();
				} catch (InterpreterException e) {
					capi.error(e, expr, interpreter);
				} finally {
					unbindArguments(interpreter);
				}
			}
		}
		
		return result;
	}

	protected void bindArguments(Interpreter interpreter, List<? extends Element> values) {
		for (int i=0; i < params.size(); i++)
			interpreter.addEnv(params.get(i), values.get(i));
	}
	
	protected void unbindArguments(Interpreter interpreter) {
		for (int i=0; i < params.size(); i++)
			interpreter.removeEnv(params.get(i));
	}
}
