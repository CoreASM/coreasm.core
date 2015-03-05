package org.coreasm.compiler.classlibrary;

import java.io.InputStream;

public abstract class LibraryEntry extends InputStream {
	public abstract String getName();
	public abstract String getSource();
	public abstract LibraryEntryType getType();
	public abstract void open();
	
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
