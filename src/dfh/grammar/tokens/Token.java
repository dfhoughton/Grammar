/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar.tokens;

/**
 * Bare minimum representation of a token.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 */
public abstract class Token {
	private final int start;
	private final int end;

	public Token(int start, int end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * The start offset.
	 * 
	 * @return the index of the first character in the token
	 */
	public int start() {
		return start;
	}

	/**
	 * The end offset.
	 * 
	 * @return the index one past that of the last character in the token
	 */
	public int end() {
		return end;
	}

	/**
	 * Character length of the token.
	 * 
	 * @return the length of the token in characters
	 */
	public int length() {
		return end - start;
	}
}
