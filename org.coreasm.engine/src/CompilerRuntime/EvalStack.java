package CompilerRuntime;

import java.util.Stack;

public class EvalStack {
	private Stack<Object> stack;
	
	public EvalStack(){
		stack = new Stack<Object>();
	}
	
	public Object pop(){
		Object o = stack.pop();
		//System.out.println("popping " + o + " (" + o.getClass().getName() + ")");
		return o;
	}
	
	public void push(Object o){
		//System.out.println("pushing " + o + " (" + o.getClass().getName() + ")");
		stack.push(o);
	}
	
	public boolean isEmpty(){
		return this.stack.isEmpty();
	}
}
