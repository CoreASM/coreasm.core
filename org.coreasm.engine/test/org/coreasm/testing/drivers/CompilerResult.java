package org.coreasm.testing.drivers;

import java.util.ArrayList;
import java.util.List;

public class CompilerResult {
	public Object result;
	public Throwable error;
	public List<String> messages;
	
	public CompilerResult(){
		messages = new ArrayList<String>();
	}
}
