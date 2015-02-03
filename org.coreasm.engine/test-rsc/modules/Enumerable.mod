interface EnumerableMock {
	public java.util.Collection<? extends CompilerRuntime.Element> enumerate();
    public boolean contains(CompilerRuntime.Element e);
    public int size();
    public boolean supportsIndexedView();
    public java.util.List<CompilerRuntime.Element> getIndexedView() throws UnsupportedOperationException;
}
