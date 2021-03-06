/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.io.IOException;

/**
 * Exception class for exceptions anticipated by this library. This is made an
 * extension of {@link RuntimeException} to reduce the necessity for error
 * catching blocks. Almost all exceptions are thrown during grammar compilation
 * rather than runtime except those caused by incomplete definition of terminal
 * rules.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class GrammarException extends RuntimeException {
	private static final long serialVersionUID = 8L;

	public GrammarException(String message) {
		super(message);
	}

	/**
	 * Wrapper for {@link IOException} to reduce exception handling clutter.
	 * 
	 * @param e
	 */
	public GrammarException(IOException e) {
		super(e);
	}
}
