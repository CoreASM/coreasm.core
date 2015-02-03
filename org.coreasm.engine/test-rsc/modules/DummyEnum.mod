class DummyEnum implements EnumerableMock{
	public java.util.List<CompilerRuntime.Element> elements;
	public DummyEnum(){
		elements = new java.util.ArrayList<CompilerRuntime.Element>();
	}
	
	public java.util.Collection<? extends CompilerRuntime.Element> enumerate(){
		return elements;
	}
    public boolean contains(CompilerRuntime.Element e){
    	return elements.contains(e);
    }
    public int size(){
    	return elements.size();
    }
    public boolean supportsIndexedView(){
    	return false;
    }
    public java.util.List<CompilerRuntime.Element> getIndexedView() throws UnsupportedOperationException{
    	throw new UnsupportedOperationException();
    }
}
