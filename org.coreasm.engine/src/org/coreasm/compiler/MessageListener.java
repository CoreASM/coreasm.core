package org.coreasm.compiler;

public interface MessageListener {
	public void receiveMessage(LoggingHelper.Level level, String msg);
}
