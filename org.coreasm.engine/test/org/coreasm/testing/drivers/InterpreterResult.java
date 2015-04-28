package org.coreasm.testing.drivers;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.UpdateMultiset;

public class InterpreterResult {
	public Location location;
	public UpdateMultiset updates;
	public Element value;
	
	public List<String> messages;
	public Throwable error;
	
	public InterpreterResult(){
		messages = new ArrayList<String>();
	}
}
