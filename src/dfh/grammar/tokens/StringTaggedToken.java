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
 * Represents the common case where tokens bear strings as tags. This will be
 * less efficient than {@link TaggedToken}, but facilitates tokenization when
 * you do not know the complete class of tags or it is inconvenient to represent
 * these as an enumeration.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 */
public class StringTaggedToken extends Token {

	private final String t;

	public StringTaggedToken(int start, int end, String tag) {
		super(start, end);
		this.t = tag;
	}

	public String tag() {
		return t;
	}
}
