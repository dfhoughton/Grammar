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
import java.util.Set;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Rule;

/**
 * Companion class to {@link StringTaggedToken}, this test is true if some token
 * at the relevant offset bears the specified tag.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
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
	public int test(List<StringTaggedToken> starting,
			List<StringTaggedToken> ending, boolean reversed) {
		if (starting != null) {
			for (StringTaggedToken tt : starting) {
				if (t.equals(tt.tag()))
					return reversed ? tt.start() : tt.end();
			}
		}
		return -1;
	}

	/**
	 * Convenience method that generates the map required by
	 * {@link Grammar#Grammar(java.io.BufferedReader, Map)} and the like. This
	 * method assumes that the rules will be identified in the grammar by the
	 * corresponding tag.
	 * 
	 * @param tags
	 *            constants identify a set of {@link StringTaggedToken
	 *            StringTaggedTokens}
	 * @return map from rule labels to token rules
	 */
	public static Map<String, Rule> precompile(Set<String> tags) {
		Map<String, Rule> map = new HashMap<String, Rule>(
				(int) (tags.size() * 1.75));
		for (String t : tags) {
			Rule r = new TokenRule<StringTaggedToken>(new StringTagTest(t));
			map.put(t, r);
		}
		return map;
	}

}
