/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar.tokens;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dfh.grammar.CachedMatch;
import dfh.grammar.Condition;
import dfh.grammar.GlobalState;
import dfh.grammar.GrammarException;
import dfh.grammar.Label;
import dfh.grammar.Label.Type;
import dfh.grammar.Match;
import dfh.grammar.Matcher;
import dfh.grammar.ReversedCharSequence;
import dfh.grammar.Reversible;
import dfh.grammar.Rule;

/**
 * A rule that matches against a {@link TokenSequence} specifically.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 * @param <K>
 */
@Reversible
public class TokenRule<K extends Token> extends Rule implements Cloneable,
		Serializable {
	private static final long serialVersionUID = 2L;

	private class TokenMatcher extends Matcher {
		private final Map<Integer, CachedMatch> cache;
		private boolean fresh = true;

		protected TokenMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(offset, master);
			this.cache = cache[cacheIndex];
		}

		@SuppressWarnings("unchecked")
		@Override
		public Match match() {
			if (options.debug)
				TokenRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				CachedMatch cm = cache.get(offset);
				if (cm == null) {
					if (options.study) {
						if (options.debug)
							TokenRule.this.matchTrace(this, null);
						return null;
					}
				} else {
					if (options.debug)
						TokenRule.this.matchTrace(this, cm.m);
					return cm.m;
				}
				TokenSequence<K> ccs = (TokenSequence<K>) (reversed ? options.rcs
						.bottomSequence() : options.cs);
				Integer off = reversed ? options.rcs.translate(offset - 1)
						: offset;
				List<K> starting, ending;
				if (reversed) {
					starting = ccs.endingAt(off);
					ending = ccs.startingAt(off);
				} else {
					starting = ccs.startingAt(off);
					ending = ccs.endingAt(off);
				}
				if (starting == null && ending == null) {
					cm = CachedMatch.MISMATCH;
				} else {
					int other = test.test(starting, ending, reversed);
					if (other == -1) {
						cm = CachedMatch.MISMATCH;
					} else {
						int start = reversed ? options.rcs.translate(off
								.intValue()) + 1 : off.intValue();
						int end = reversed ? options.rcs.translate(other) + 1
								: other;
						Match m = new Match(TokenRule.this, start, end);
						if (c == null || c.passes(m, this, s))
							cm = new CachedMatch(m);
						else
							cm = CachedMatch.MISMATCH;
					}
				}
				cache.put(offset, cm);
				if (options.debug)
					TokenRule.this.matchTrace(this, cm.m);
				return cm.m;
			}
			if (options.debug)
				TokenRule.this.matchTrace(this, null);
			return null;
		}

		@Override
		protected boolean mightHaveNext() {
			return fresh;
		}

		@Override
		protected Rule rule() {
			return TokenRule.this;
		}

		@Override
		public String toString() {
			return test.id();
		}
	}

	protected final TokenTest<K> test;
	protected Condition c;
	protected boolean reversed = false;

	/**
	 * Creates {@link TokenRule} with given test and label.
	 * 
	 * @param label
	 * @param test
	 */
	public TokenRule(Label label, TokenTest<K> test) {
		super(label);
		this.test = test;
	}

	/**
	 * Convenience method that delegates the labeling of the rule to
	 * {@link TokenTest#id()}.
	 * 
	 * @param test
	 */
	public TokenRule(TokenTest<K> test) {
		this(new Label(Type.explicit, test.id()), test);
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		if (!(master.options.cs instanceof TokenSequence))
			throw new GrammarException(TokenRule.class
					+ " can only match against " + TokenSequence.class);
		return new TokenMatcher(offset, cache, master);
	}

	/**
	 * Uses {@link TokenTest#id()} to establish functional identity. This
	 * identifier is contained in doubled angle brackets to disambiguate it from
	 * non-token rule unique ids.
	 */
	@Override
	protected String uniqueId() {
		if (uid != null)
			return uid;
		return "<<" + test.id() + ">>";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options) {
		boolean reversed = s instanceof ReversedCharSequence;
		if (reversed)
			s = ((ReversedCharSequence) s).bottomSequence();
		if (!(s instanceof TokenSequence))
			throw new GrammarException(TokenRule.class
					+ " can only match against " + TokenSequence.class);
		TokenSequence<K> ccs = (TokenSequence<K>) s;
		Map<Integer, CachedMatch> subCache = cache[cacheIndex];
		Set<Integer> startOffsets = new HashSet<Integer>();
		if (subCache.isEmpty()) {
			for (Integer i : ccs.boundaries()) {
				int end = test.test(ccs.startingAt(i), ccs.endingAt(i), false);
				if (end > -1) {
					boolean good = true;
					Match m = null;
					if (c != null) {
						m = new Match(this, i, end);
						good = (c.passes(m, null, s));
						if (reversed)
							m = null;
					}
					if (good) {
						int start = i.intValue();
						if (m == null) {
							if (reversed) {
								start = options.rcs.translate(end - 1);
								end = options.rcs.translate(i - 1);
								m = new Match(this, start, end);
							} else
								m = new Match(this, i, end);
						}
						subCache.put(start, new CachedMatch(m));
						startOffsets.add(i);
					}
				}
			}
		} else {
			startOffsets.addAll(subCache.keySet());
		}
		return startOffsets;
	}

	/**
	 * See {@link #mayBeZeroWidth(Map)}
	 */
	public boolean zeroWidth() {
		return false;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public Object clone() {
		Label l = new Label(label.t, label.id);
		Rule r = new TokenRule<K>(l, test);
		if (c != null)
			r = r.conditionalize(c, c.getName());
		return r;
	}

	@Override
	public Rule conditionalize(Condition c, String id) {
		this.c = c;
		this.condition = id;
		return this;
	}

	@Override
	public String description(boolean withinBrackets) {
		StringBuilder b = new StringBuilder(label.toString());
		if (condition != null)
			b.append(" (").append(condition).append(')');
		return wrap(b);
	}

	/**
	 * By default it is assumed that no token is zero-width and all token rules
	 * require matching against some such token. If your rule violates these
	 * assumptions, you must override this method.
	 */
	@Override
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		Boolean b = false;
		cache.put(uid(), b);
		return b;
	}

	@Override
	public Rule reverse(String id) {
		Label l = new Label(label().t, id);
		TokenRule<K> r = new TokenRule<K>(l, test);
		r.reversed = !reversed;
		return r;
	}

}
