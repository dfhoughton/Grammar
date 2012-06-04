/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import dfh.grammar.Label.Type;

/**
 * Implements a backtracking barrier. See {@link BarrierFragment}.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class BacktrackingBarrier extends Rule implements Serializable,
		NonterminalRule {
	private static final long serialVersionUID = 7L;
	/**
	 * {@link Label} used by all : barriers.
	 */
	public static final Label SINGLE_COLON_LABEL = new Label(Type.implicit, ":");
	/**
	 * {@link Label} used by all :: barriers.
	 */
	public static final Label DOUBLE_COLON_LABEL = new Label(Type.implicit,
			"::");
	/**
	 * Whether this is a single or double colon barrier.
	 */
	protected final boolean isSingle;

	public BacktrackingBarrier(boolean isSingle) {
		super(isSingle ? SINGLE_COLON_LABEL : DOUBLE_COLON_LABEL);
		this.isSingle = isSingle;
	}

	private class BarrierMatcher extends Matcher {

		private boolean fresh = true;

		protected BarrierMatcher(Integer offset, Matcher master) {
			super(offset, master);
		}

		@Override
		public Match match() {
			if (fresh) {
				fresh = false;
				return register(new Match(BacktrackingBarrier.this, offset,
						offset));
			}
			throwBarrierException();
			return null;
		}

		private void throwBarrierException() {
			rule().event(this, "hit backtracking barrier");
			if (isSingle)
				throw new SingleColonBarrier(this);
			throw new DoubleColonBarrier(this);
		}

		@Override
		protected boolean mightHaveNext() {
			if (fresh)
				return true;
			throwBarrierException();
			return fresh;
		}

		@Override
		protected Rule rule() {
			return BacktrackingBarrier.this;
		}

	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return new BarrierMatcher(offset, master);
	}

	@Override
	protected String uniqueId() {
		return label.id;
	}

	@Override
	public String description(boolean inBrackets) {
		return label.id;
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options) {
		// non-terminal rules don't study
		return null;
	}

	@Override
	public boolean zeroWidth() {
		return true;
	}

	@Override
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		cache.put(uid(), true);
		return true;
	}
}
