package org.coreasm.testing.value;

public class StorageProvider {
	public final LocationProvider loc;
	public final ElementProvider val;
	
	public StorageProvider(LocationProvider loc, ElementProvider val){
		this.loc = loc;
		this.val = val;
	}
}
