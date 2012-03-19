/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.util.Map;

/**
 * General pattern of a {@link Matcher} that matches against {@link Match}
 * objects rather than the {@link CharSequence}.
 * <p>
 * <b>Creation date:</b> Apr 1, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class NonterminalMatcher extends Matcher {

	/**
	 * Obtains next value of {@link #next}.
	 */
	protected abstract void fetchNext();

	/**
	 * Next {@link Match} that will be returned by {@link #match()}.
	 */
	protected Match next;
	/**
	 * Whether no further matching is possible.
	 */
	protected boolean done = false;
	/**
	 * General matching cache.
	 */
	protected final Map<Integer, CachedMatch>[] cache;
	/**
	 * Matching cache appropriate to this {@link Matcher}'s {@link Rule}.
	 */
	protected final Map<Integer, CachedMatch> subCache;
	/**
	 * {@link Rule} that generated this {@link Matcher}.
	 */
	protected final Rule rule;

	/**
	 * Constructs non-terminal matcher with given state.
	 * 
	 * @param offset
	 * @param cache
	 * @param rule
	 * @param master
	 */
	protected NonterminalMatcher(Integer offset,
			Map<Integer, CachedMatch>[] cache, Rule rule, Matcher master) {
		super(offset, master);
		this.cache = cache;
		this.rule = rule;
		this.subCache = cache[rule.cacheIndex];
	}

	@Override
	public Match match() {
		if (options.debug)
			rule.matchTrace(this);
		if (done) {
			if (options.debug)
				rule.matchTrace(this, null);
			return null;
		}
		CachedMatch cm = subCache.get(offset);
		if (cm == CachedMatch.MISMATCH) {
			if (options.debug)
				rule.matchTrace(this, null);
			return null;
		}
		if (next == null && cycleCheck())
			fetchNext();
		if (cm == null) {
			cm = next == null ? CachedMatch.MISMATCH : CachedMatch.MATCH;
			subCache.put(offset, cm);
		}
		Match n = next;
		next = null;
		if (options.debug)
			rule.matchTrace(this, n);
		return register(n);
	}

	@Override
	public boolean mightHaveNext() {
		if (done)
			return false;
		if (CachedMatch.MISMATCH.equals(subCache.get(offset)))
			return false;
		if (next == null && cycleCheck())
			fetchNext();
		return next != null;
	}

	/**
	 * @return whether we seem to be in a non-progressing recursive loop
	 */
	protected boolean cycleCheck() {
		if (rule.cycle) {
			Matcher m = master;
			int count = 0;
			while (m != null && m.offset.equals(offset)) {
				if (m.rule() == rule) {
					count++;
					if (count == options.maxDepth)
						return false;
				}
				m = m.master;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "M:" + rule.label().id;
	}

	@Override
	protected Rule rule() {
		return rule;
	}
}
