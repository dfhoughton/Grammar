/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

/**
 * Object to put into the match cache so we can distinguish three states:
 * <ol>
 * <li>untested
 * <li>failure
 * <li>match
 * </ol>
 * If the offset is untested, there is no {@link CachedMatch} instance in the
 * cache for that {@link Rule} and offset. Otherwise, there is such an object,
 * and its value indicates whether a match is or is not possible at that offset.
 * In the case of terminal rules -- {@link LeafRule} and {@link LiteralRule},
 * for example -- the actual {@link Match} object is cached. Otherwise, the
 * constant {@link #MATCH} and {@link #MISMATCH} objects are used.
 * <p>
 * <b>Creation date:</b> Mar 24, 2011
 * 
 * @author David Houghton
 * 
 */
public class CachedMatch {
	public final Match m;

	/**
	 * To reduce object creation we have a common mismatch object.
	 */
	public static final CachedMatch MISMATCH = new CachedMatch(null);
	/**
	 * To reduce object creation we have a common match object for
	 * non-terminals.
	 */
	public static final CachedMatch MATCH = new CachedMatch(Match.DUMMY);

	/**
	 * @param m
	 *            {@link Match} to cache
	 */
	public CachedMatch(Match m) {
		assert m != null;
		this.m = m;
	}

	@Override
	public String toString() {
		if (this == MATCH)
			return "cm:match";
		if (this == MISMATCH)
			return "cm:mismatch";
		return "cm:" + m;
	}
}
