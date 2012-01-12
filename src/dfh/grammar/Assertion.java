package dfh.grammar;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implements zero-width assertions. See {@link AssertionFragment}.
 * <p>
 * <b>Creation date:</b> Apr 7, 2011
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class Assertion extends Rule implements Serializable, NonterminalRule {
	private static final long serialVersionUID = 3L;
	/**
	 * suffix added to {@link Rule#uid} of reversed rules to signal their
	 * reversal.
	 */
	public static final String REVERSAL_SUFFIX = ":r";

	private class AssertionMatcher extends Matcher {
		private final Map<Integer, CachedMatch>[] cache;
		private final Map<Integer, CachedMatch> subCache;
		private final boolean backward;

		private AssertionMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(offset, master);
			this.cache = cache;
			this.subCache = cache[rule().cacheIndex];
			backward = false;
		}

		public AssertionMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master,
				GlobalState gs) {
			super(offset, master, gs);
			this.cache = gs.backwardsCache;
			this.subCache = cache[rule().cacheIndex];
			backward = true;
		}

		private boolean fresh = true;

		@Override
		public Match match() {
			if (fresh) {
				fresh = false;
				// TODO: should we check cache here at all?
				CachedMatch cm = subCache.get(offset);
				if (cm == null) {
					Match n = r.matcher(
							backward ? master.options.rcs.translate(offset) + 1
									: offset, cache, this).match();
					if (positive) {
						if (n != null) {
							Match next = new Match(Assertion.this, offset,
									offset);
							if (backward)
								n = reverse(n);
							next.setChildren(new Match[] { n });
							n = next;
						}
					} else {
						if (n != null)
							n = null;
						else
							n = new Match(Assertion.this, offset, offset);
					}
					cm = n == null ? CachedMatch.MISMATCH : CachedMatch.MATCH;
					subCache.put(offset, cm);
					return register(n);
				} else if (cm == CachedMatch.MISMATCH)
					return null;
				else if (positive) {
					Match n;
					if (backward) {
						n = r.matcher(0, cache, this).match();
						n = reverse(n);
					} else {
						n = r.matcher(offset, cache, this).match();
					}
					Match next = new Match(Assertion.this, offset, offset);
					next.setChildren(new Match[] { n });
					return register(next);
				} else {
					Match n = new Match(Assertion.this, offset, offset);
					return register(n);
				}
			}
			return null;
		}

		/**
		 * Swaps all members of match tree and adjusts offsets
		 * 
		 * @param n
		 * @return
		 */
		private Match reverse(Match n) {
			if (options.isReversed) {
				Match reversed = new Match(n.rule(), options.rcs.translate(n
						.end()) + 1, options.rcs.translate(n.start()) + 1);
				if (n.children() != null) {
					Match[] children = new Match[n.children().length];
					int half = children.length % 2 == 1 ? children.length / 2
							: -1;
					for (int i = 0, lim = children.length / 2; i <= lim; i++) {
						Match m1 = reverse(n.children()[i]);
						if (i == half)
							children[i] = m1;
						else {
							int j = children.length - i - 1;
							Match m2 = reverse(n.children()[j]);
							children[i] = m2;
							children[j] = m1;
						}
					}
					reversed.setChildren(children);
				}
				return reversed;
			}
			return n;
		}

		@Override
		protected boolean mightHaveNext() {
			return fresh;
		}

		@Override
		protected Rule rule() {
			return Assertion.this;
		}

	}

	protected Rule r;
	protected final boolean positive;
	protected final boolean forward;
	private String subDescription;

	public Assertion(Label label, Rule r, boolean positive, boolean forward) {
		super(label);
		this.r = r;
		this.positive = positive;
		this.forward = forward;
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		if (forward)
			return new AssertionMatcher(offset, cache, master);
		GlobalState gs = new GlobalState(master.options, cache);
		return new AssertionMatcher(offset, cache, master, gs);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append(positive ? '~' : '!');
		b.append(forward ? '+' : '-');
		b.append(r.uniqueId());
		return b.toString();
	}

	@Override
	public String description(boolean inBrackets) {
		StringBuilder b = new StringBuilder();
		b.append(positive ? '~' : '!');
		if (!forward) {
			b.append('-');
			b.append(subDescription);
		} else
			subDescription(r, b);
		return wrap(b);
	}

	static void subDescription(Rule r, StringBuilder b) {
		if (r.generation == -1) {
			boolean needsBrackets = r instanceof SequenceRule
					|| r instanceof RepetitionRule
					|| r instanceof AlternationRule;
			if (needsBrackets)
				b.append("[ ");
			b.append(r.description(true));
			if (needsBrackets)
				b.append(" ]");
		} else
			b.append(r.label);
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, Set<Rule> studiedRules,
			GlobalState options) {
		// we don't keep assertion offsets; they would be redundant
		if (forward)
			r.study(s, cache, studiedRules, options);
		return new HashSet<Integer>(0);
	}

	@Override
	public boolean zeroWidth() {
		return true;
	}

	@Override
	protected Rule sClone() {
		return new Assertion((Label) label.clone(), r, positive, forward);
	}

	void setSubDescription(String subDescription) {
		if (this.subDescription != null)
			throw new GrammarException(
					"one cannot reset an assertion sub-descriptoin");
		this.subDescription = subDescription;
	}

	@Override
	protected void setCacheIndex(Map<String, Integer> uids) {
		if (cacheIndex == -1) {
			Integer i = uids.get(uid());
			if (i == null) {
				i = uids.size();
				uids.put(uid(), i);
			}
			cacheIndex = i;
			r.setCacheIndex(uids);
		}
	}

	@Override
	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
		if (visited.contains(this))
			return currentMax;
		visited.add(this);
		int max = Math.max(cacheIndex, currentMax);
		return r.maxCacheIndex(max, visited);
	}

	@Override
	protected void rules(Map<String, Rule> map) {
		if (!map.containsKey(uid())) {
			map.put(uid(), this);
			r.rules(map);
		}
	}

	@Override
	protected void fixAlternation() {
		r.fixAlternation();
	}

	@Override
	protected void subRules(Set<Rule> set) {
		if (!set.contains(this)) {
			set.add(this);
			r.subRules(set);
		}
	}

	@Override
	protected Boolean mightBeZeroWidth(Map<String, Boolean> cache) {
		if (!cache.containsKey(uid())) {
			cache.put(uid(), true);
			r.mightBeZeroWidth(cache);
		}
		return true;
	}
}
