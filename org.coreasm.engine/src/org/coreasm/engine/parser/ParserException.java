/*
 * ParserException.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2008-2009 Roozbeh Farahbod 
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine.parser;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.TreeSet;

import org.coreasm.engine.EngineException;

/**
 * A CoreASM parser exceptions.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public class ParserException extends EngineException {

	public final CharacterPosition pos;
	public final String msg;
	public final Throwable cause;
	
	private static final long serialVersionUID = 1L;

	public ParserException(String message) {
		super(message);
		this.pos = CharacterPosition.NO_POSITION;
		this.msg = message;
		this.cause = null;
	}
	
	public ParserException(String message, CharacterPosition pos) {
		super(message);
		this.pos = pos;
		this.msg = message;
		this.cause = null;
	}
	
	public ParserException(Throwable cause) {
		if (cause instanceof org.jparsec.error.ParserException) {
			org.jparsec.error.ParserException pcause = (org.jparsec.error.ParserException) cause;
			StringBuffer buf = new StringBuffer();
			org.jparsec.error.ParseErrorDetails err = pcause.getErrorDetails();
			if (err != null) {
		        showExpecting(buf, err.getExpected().toArray(new String[] {} ));
		        showUnexpected(buf, new String[] {err.getUnexpected()} );
		        showMessages(buf, new String[] {err.getFailureMessage()} );
		        showEncountered(buf, err.getEncountered());
			}
			this.msg = buf.toString();
			pos = new CharacterPosition(pcause.getLocation().line, pcause.getLocation().column);
		} else {
			initCause(cause);
			pos = CharacterPosition.NO_POSITION;
			if (cause.getMessage() == null)
				this.msg = cause.getClass().getSimpleName();
			else
				this.msg = cause.getMessage();
		}
		this.cause = cause;
	}

	/*
	 * The following methods are copied from jfun.parsec.DefaultShowError
	 */
	private static String[] unique(final String[] msgs) {
		if (msgs.length <= 1)
			return msgs;
		final TreeSet<String> set = new TreeSet<String>(Arrays.asList(msgs));
		final String[] umsgs = new String[set.size()];
		set.toArray(umsgs);
		return umsgs;
	}

	  private static void showList(final StringBuffer buf, final String[] msgs) {
		if (msgs.length == 0)
			return;
		for (int i = 0; i < msgs.length - 1; i++) {
			buf.append(msgs[i]).append(' ');
		}
		if (msgs.length > 1)
			buf.append("or ");
		buf.append(msgs[msgs.length - 1]);
	}

	private static void showExpecting(final StringBuffer buf,
			final String[] msgs) {
		if (msgs == null || msgs.length == 0)
			return;
		final String[] umsgs = unique(msgs);
		buf.append("Parser was expecting ");
		showList(buf, umsgs);
		buf.append(".\n");
	}

	private static void showUnexpected(final StringBuffer buf,
			final String[] msgs) {
		if (msgs == null || msgs.length == 0)
			return;
		showList(buf, unique(msgs));
		buf.append(" unexpected.\n");
	}

	private static void showMessages(final StringBuffer buf, final String[] msgs) {
		if (msgs == null || msgs.length == 0)
			return;
		buf.append(msgs[0]);
		for (int i = 1; i < msgs.length; i++) {
			buf.append(" or \n").append(msgs[i]);
		}
		buf.append("\n");
	}

	private static void showEncountered(final StringBuffer buf, final String s) {
		if (s == null)
			return;
		buf.append("Parser did not expect to encounter '" + s + "'.\n");//.append(" encountered.\n");
	}

	@Override
	public void printStackTrace() {
		super.printStackTrace();
		if (cause != null)
			cause.printStackTrace();
	}

	@Override
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		if (cause != null)
			cause.printStackTrace(s);
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		if (cause != null)
			cause.printStackTrace(s);
	}

	
	  
}
