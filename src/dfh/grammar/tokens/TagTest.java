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

/**
 * Companion class to {@link TaggedToken}, this test is true if some token at
 * the relevant offset bears the specified tag.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 * @param <T>
 */
public class TagTest<T extends Enum<T>> implements TokenTest<TaggedToken<T>> {
	private final T t;

	/**
	 * Makes a test for the specified tag constant.
	 * 
	 * @param t
	 *            tag constant
	 */
	public TagTest(T t) {
		this.t = t;
	}

	@Override
	public String id() {
		return t.toString();
	}

	@Override
	public int test(List<TaggedToken<T>> tokens,
			TokenSequence<TaggedToken<T>> sequence, boolean reversed) {
		for (TaggedToken<T> tt : tokens) {
			if (tt.tag() == t)
				return reversed ? tt.start() : tt.end();
		}
		return -1;
	}
}
