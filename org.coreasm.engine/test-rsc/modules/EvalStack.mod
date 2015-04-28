class EvalStack{
	private java.util.Stack<Object> stack;
	public EvalStack(){
		stack = new java.util.Stack<Object>();
	}
	
	public Object pop(){
		return stack.pop();
	}
	public void push(Object o){
		stack.push(o);
	}
	public boolean isEmpty(){
		return this.stack.isEmpty();
	}
}