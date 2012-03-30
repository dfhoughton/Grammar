/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar.tokens;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Rule;

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
	 * @param tag
	 *            tag constant
	 */
	public TagTest(T tag) {
		this.t = tag;
		if (tag == null)
			throw new GrammarException("tag cannot be null");
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

	/**
	 * Convenience method that generates the map required by
	 * {@link Grammar#Grammar(java.io.BufferedReader, Map)} and the like. This
	 * method assumes that the rules will be identified in the grammar by the
	 * string returned the constants' <code>name()</code> method.
	 * 
	 * @param tags
	 *            constants identify a set of {@link TaggedToken TaggedTokens}
	 * @return map from rule labels to token rules
	 */
	public static <K extends Enum<K>> Map<String, Rule> precompile(Class<K> tags) {
		Map<String, Rule> map = new HashMap<String, Rule>(
				(int) (tags.getEnumConstants().length * 1.75));
		for (K k : tags.getEnumConstants()) {
			Rule r = new TokenRule<TaggedToken<K>>(new TagTest<K>(k));
			map.put(k.name(), r);
		}
		return map;
	}
}
