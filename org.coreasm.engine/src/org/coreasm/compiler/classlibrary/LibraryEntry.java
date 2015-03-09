package org.coreasm.compiler.classlibrary;

import java.io.Closeable;

public abstract class LibraryEntry implements Closeable{
	public abstract String getName();
	public abstract String getSource();
	public abstract LibraryEntryType getType();
	public abstract void open(String entryName) throws Exception;
	public abstract String readLine() throws Exception;	
	
	protected String getPackage(String className){
		int pos = className.lastIndexOf(".");
		if(pos <= 0) return "";
		return className.substring(0, pos);
	}
	
	public boolean equals(Object o){
		if(o instanceof LibraryEntry){
			LibraryEntry l = (LibraryEntry) o;
			return getName().equals(l.getName()) && getSource().equals(l.getSource()) && getType().equals(l.getType());
		}
		return false;
	}
	
	@Override
	public String toString(){
		return "[" + this.getClass().getName() + ":" + getName() + ":" + getSource() + ":" + getType() + "]";
	}
}
