package dfh.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dfh.grammar.Grammar.GlobalState;

/**
 * Implements zero-width forward assertions. See {@link AssertionFragment}.
 * <p>
 * <b>Creation date:</b> Apr 7, 2011
 * 
 * @author David Houghton
 * 
 */
public class Assertion extends Rule {
	private static final long serialVersionUID = 1L;

	private class AssertionMatcher extends Matcher {
		private final Map<Label, Map<Integer, CachedMatch>> cache;
		private final Map<Integer, CachedMatch> subCache;

		private AssertionMatcher(CharSequence cs, Integer offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(cs, offset, master);
			this.cache = cache;
			this.subCache = cache.get(label);
		}

		private boolean fresh = true;

		@Override
		public Match match() {
			if (fresh) {
				fresh = false;
				CachedMatch cm = subCache.get(offset);
				if (cm == null) {
					Match n = r.matcher(s, offset, cache, this).match();
					if (positive) {
						if (n != null) {
							Match next = new Match(Assertion.this, offset,
									offset);
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
					return n;
				} else if (cm == CachedMatch.MISMATCH)
					return null;
				else if (positive) {
					Match n = r.matcher(s, offset, cache, this).match();
					Match next = new Match(Assertion.this, offset, offset);
					next.setChildren(new Match[] { n });
					return next;
				} else {
					Match n = new Match(Assertion.this, offset, offset);
					return n;
				}
			}
			return null;
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

	public Assertion(Label label, Rule r, boolean positive) {
		super(label);
		this.r = r;
		this.positive = positive;
	}

	@Override
	public Matcher matcher(CharSequence s, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new AssertionMatcher(s, offset, cache, master);
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
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache,
			Set<Rule> studiedRules, GlobalState options) {
		// we don't keep assertion offsets; they would be redundant
		r.study(s, cache, studiedRules, options);
		return new HashSet<Integer>(0);
	}

	@Override
	public boolean zeroWidth() {
		return true;
	}

	@Override
	public Rule shallowClone() {
		return new Assertion((Label) label.clone(), r, positive);
	}

}
