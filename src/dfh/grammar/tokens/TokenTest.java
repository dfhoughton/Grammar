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
 * Tests whether the tokens ending at a particular offset have a particular
 * property -- tag, say.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 * @param <K>
 *            a type of token
 */
public interface TokenTest<K extends Token> {

	/**
	 * A string distinguishing this test from functionally distinct tests.
	 * 
	 * @return functionally unique identifier for the test
	 */
	public String id();

	/**
	 * Tests whether the tokens at a given offset have a particular property.
	 * The return value is a token end offset unless the test is on a reversed
	 * sequence -- in a lookbehind -- in which case it is a start offset. If the
	 * test fails, it must return -1.
	 * 
	 * @param starting
	 *            the tokens starting at the given offset
	 * @param ending
	 *            the tokens ending at the given offset
	 * @param reversed
	 *            whether the test is occurring in a reversed sequence
	 * @return the opposite offset of the token matched; must return -1 if the
	 *         test fails
	 */
	public int test(List<K> starting, List<K> ending, boolean reversed);

}
