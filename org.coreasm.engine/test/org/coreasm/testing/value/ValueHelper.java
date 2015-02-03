package org.coreasm.testing.value;

import org.coreasm.engine.absstorage.Update;

public class ValueHelper {
	public static UpdateProvider constructUpdate(String location, boolean value){
		return new UpdateProvider(new LocationProvider(location), new BooleanProvider(value), Update.UPDATE_ACTION);
	}
}
