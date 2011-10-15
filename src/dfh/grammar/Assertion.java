package dfh.grammar;

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
public class Assertion extends Rule {
	private static final long serialVersionUID = 1L;

	private class AssertionMatcher extends Matcher {
		private final Map<Label, Map<Integer, CachedMatch>> cache;
		private final Map<Integer, CachedMatch> subCache;
		private final boolean backward;

		private AssertionMatcher(CharSequence cs, Integer offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(cs, offset, master);
			this.cache = cache;
			this.subCache = cache.get(label);
			backward = false;
		}

		public AssertionMatcher(CharSequence reversed, Integer offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master,
				GlobalState gs) {
			super(reversed, offset, master, gs);
			this.cache = gs.backwardsCache;
			this.subCache = cache.get(label);
			backward = true;
		}

		private boolean fresh = true;

		@Override
		public Match match() {
			if (fresh) {
				fresh = false;
				CachedMatch cm = subCache.get(offset);
				if (cm == null) {
					Match n = r.matcher(s, backward ? 0 : offset, cache, this)
							.match();
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
						n = r.matcher(s, 0, cache, this).match();
						n = reverse(n);
					} else {
						n = r.matcher(s, offset, cache, this).match();
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
			return reverse(n, offset);
		}

		private Match reverse(Match n, int base) {
			Match reversed = new Match(n.rule(), base - n.end(), base
					- n.start());
			if (n.children() != null) {
				Match[] children = new Match[n.children().length];
				int half = children.length % 2 == 1 ? children.length / 2 : -1;
				for (int i = 0, lim = children.length / 2; i <= lim; i++) {
					Match m1 = reverse(n.children()[i], base);
					if (i == half)
						children[i] = m1;
					else {
						int j = children.length - i - 1;
						Match m2 = reverse(n.children()[j], base);
						children[i] = m2;
						children[j] = m1;
					}
				}
				reversed.setChildren(children);
			}
			return reversed;
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

	protected final Rule r;
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
	public Matcher matcher(CharSequence s, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		if (forward)
			return new AssertionMatcher(s, offset, cache, master);
		CharSequence reversed = new ReversedCharSequence(s, offset,
				master.options.start);
		GlobalState gs = new GlobalState(master.options, reversed.length(),
				cache);
		return new AssertionMatcher(reversed, offset, cache, master, gs);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append(positive ? '~' : '!');
		b.append(r.uniqueId());
		return b.toString();
	}

	@Override
	public String description() {
		StringBuilder b = new StringBuilder();
		b.append(positive ? '~' : '!');
		if (!forward) {
			b.append('-');
			b.append(subDescription);
		} else
			subDescription(r, b);
		return b.toString();
	}

	static void subDescription(Rule r, StringBuilder b) {
		if (r.generation == -1) {
			boolean needsBrackets = r instanceof SequenceRule
					|| r instanceof RepetitionRule
					|| r instanceof AlternationRule;
			if (needsBrackets)
				b.append("[ ");
			b.append(r.description());
			if (needsBrackets)
				b.append(" ]");
		} else
			b.append(r.label);
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache,
			Set<Rule> studiedRules, GlobalState options) {
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
	public Rule shallowClone() {
		return new Assertion((Label) label.clone(), r, positive, forward);
	}

	public void setSubDescription(String subDescription) {
		if (this.subDescription != null)
			throw new GrammarException(
					"one cannot reset an assertion sub-descriptoin");
		this.subDescription = subDescription;
	}

}
