package org.coreasm.testing.loading;

import java.lang.reflect.Field;

public class ReflectionHelper {
	public static Object getField(Object o, String name) throws Exception{
		Field f = o.getClass().getField(name);
		if(!f.isAccessible()) f.setAccessible(true);
		return f.get(o);
	}
}
