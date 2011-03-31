package dfh.grammar;

import java.util.Map;

public abstract class NonterminalMatcher extends Matcher {

	protected abstract void fetchNext();

	protected Match next;
	protected boolean done = false;
	protected final Map<Label, Map<Integer, CachedMatch>> cache;
	protected final Map<Integer, CachedMatch> subCache;
	protected final Rule rule;

	protected NonterminalMatcher(CharSequence cs2, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Rule rule,
			Matcher master) {
		super(cs2, offset, master);
		this.cache = cache;
		this.rule = rule;
		this.subCache = cache.get(rule.label());
	}

	@Override
	public Match match() {
		rule.matchTrace(this);
		if (done) {
			rule.matchTrace(this, null);
			return null;
		}
		CachedMatch cm = subCache.get(offset);
		boolean alreadyMatched = cm != null;
		if (alreadyMatched && cm.m == null) {
			rule.matchTrace(this, null);
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
		rule.matchTrace(this, n);
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

	@Override
	protected Rule rule() {
		return rule;
	}
}
