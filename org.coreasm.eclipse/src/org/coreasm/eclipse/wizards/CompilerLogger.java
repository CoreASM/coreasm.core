package org.coreasm.eclipse.wizards;

import java.io.IOException;

import org.coreasm.compiler.LoggingHelper.Level;
import org.coreasm.compiler.MessageListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


public class CompilerLogger implements MessageListener{
	private MessageConsoleStream out;
	
	public CompilerLogger(MessageConsole console){
		out = console.newMessageStream();
	}
	
	@Override
	public void receiveMessage(Level arg0, String arg1) {
		String s = "";
		
		switch(arg0){
			case DEBUG: s = "[DEBUG] "; break;
			case WARN: s = "[WARN] "; break;
			case ERROR: s = "[ERR] "; break;
		}
		try{
			out.write(s + arg1 + System.getProperty("line.separator"));
		}
		catch(IOException e){
			//hide exception
		}
	}
	
	public void destroy() throws IOException{
		out.close();
	}
}
