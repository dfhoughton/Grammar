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

import dfh.grammar.SequenceRule.SequenceMatcher;

/**
 * The object implementing rules such as
 * 
 * <pre>
 * {@code
 * <a> = <b> 'foo' 1
 * }
 * </pre>
 * 
 * where 1 in this case refers back to whatever string was matched by
 * {@code <b>}. Note that back reference matching can't easily be
 * cached because it isn't truly context free. A given back reference may or may
 * not match at a given offset depending on the rule of which it is a component.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class BackReferenceRule extends Rule implements Serializable,
		NonterminalRule {
	private static final long serialVersionUID = 8L;
	protected final int index;

	private class BackReferenceMatcher extends Matcher {

		private boolean fresh = true;

		public BackReferenceMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(offset, master);
		}

		@Override
		public Match match() {
			if (options.debug)
				BackReferenceRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				Match m = ((SequenceMatcher) master).matched.get(index), n = null;
				int delta = m.end() - m.start();
				if (delta == 0) {
					// zero-width matches always match
					n = new Match(BackReferenceRule.this, offset, offset);
				} else {
					int end = offset + delta;
					if (end <= options.end()) {
						boolean matched = true;
						for (int i = offset, j = m.start(); i < end; i++, j++) {
							if (s.charAt(i) != s.charAt(j)) {
								matched = false;
								break;
							}
						}
						if (matched)
							n = new Match(BackReferenceRule.this, offset, end);
					}
				}
				if (options.debug)
					BackReferenceRule.this.matchTrace(this, n);
				return register(n);
			}
			if (options.debug)
				BackReferenceRule.this.matchTrace(this, null);
			return null;
		}

		@Override
		public boolean mightHaveNext() {
			return fresh;
		}

		@Override
		protected Rule rule() {
			return BackReferenceRule.this;
		}
	}

	/**
	 * Generates the back reference rule using the given index.
	 * 
	 * @param label
	 * @param index
	 */
	public BackReferenceRule(Label label, int index) {
		super(label);
		this.index = index;
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return new BackReferenceMatcher(offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return Integer.toString(index + 1);
	}

	@Override
	public String description(boolean inBrackets) {
		return wrap(new StringBuilder(label.toString()));
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
