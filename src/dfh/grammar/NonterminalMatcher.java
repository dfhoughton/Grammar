package dfh.grammar;

import java.util.Map;

public abstract class NonterminalMatcher implements Matcher {

	protected final Integer offset;
	protected final Match parent;

	protected abstract void fetchNext();

	protected final CharSequence cs;
	protected Match next;
	protected boolean done = false;
	protected final Map<Label, Map<Integer, CachedMatch>> cache;
	protected final Map<Integer, CachedMatch> subCache;
	protected final Rule rule;

	protected NonterminalMatcher(CharSequence cs2, int offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Rule rule) {
		this.cs = cs2;
		this.offset = offset;
		this.parent = parent;
		this.cache = cache;
		this.rule = rule;
		this.subCache = cache.get(rule.label());
	}

	@Override
	public Match match() {
		rule.matchTrace(this, cs, offset);
		if (done) {
			rule.matchTrace(this, cs, offset, null);
			return null;
		}
		CachedMatch cm = subCache.get(offset);
		boolean alreadyMatched = cm != null;
		if (alreadyMatched && cm.m == null) {
			rule.matchTrace(this, cs, offset, null);
			return null;
		}
		if (next == null)
			fetchNext();
		if (!alreadyMatched) {
			cm = new CachedMatch(next == null ? null : Match.dummy);
			subCache.put(offset, cm);
		}
		Match n = next;
		next = null;
		rule.matchTrace(this, cs, offset, n);
		return n;
	}

	@Override
	public boolean mightHaveNext() {
		if (done) {
			return false;
		}
		if (next == null)
			fetchNext();
		return next != null;
	}

	@Override
	public String toString() {
		return "M:" + rule.label().id;
	}
}
