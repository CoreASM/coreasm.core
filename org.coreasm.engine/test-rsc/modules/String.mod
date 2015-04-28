class StringMock extends Element{
	public String val;
	public StringMock(String s){
		val = s;
	}
	
	public String toString() {
		return val;
	}
	
	public boolean equals(Object o){
		if(!(o instanceof StringMock)) return false;
		StringMock sm = (StringMock) o;
		return sm.val.equals(val);
	}
}
