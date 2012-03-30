/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar.tokens;

import java.util.List;

import dfh.grammar.GrammarException;

/**
 * Companion class to {@link StringTaggedToken}, this test is true if some token
 * at the relevant offset bears the specified tag.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 * @param <T>
 */
public class StringTagTest implements TokenTest<StringTaggedToken> {
	private final String t;

	/**
	 * Makes a test for the specified tag.
	 * 
	 * @param tag
	 *            tag constant
	 */
	public StringTagTest(String tag) {
		this.t = tag;
		if (tag == null)
			throw new GrammarException("tag cannot be null");
	}

	@Override
	public String id() {
		return t.toString();
	}

	@Override
	public int test(List<StringTaggedToken> tokens,
			TokenSequence<StringTaggedToken> sequence, boolean reversed) {
		for (StringTaggedToken tt : tokens) {
			if (t.equals(tt.tag()))
				return reversed ? tt.start() : tt.end();
		}
		return -1;
	}
}
