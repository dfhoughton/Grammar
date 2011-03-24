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
	protected final Label label;

	protected NonterminalMatcher(CharSequence cs2, int offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Label label) {
		this.cs = cs2;
		this.offset = offset;
		this.parent = parent;
		this.cache = cache;
		this.label = label;
		this.subCache = cache.get(label);
	}

	@Override
	public Match match() {
		if (done)
			return null;
		CachedMatch cm = subCache.get(offset);
		boolean alreadyMatched = cm != null;
		if (alreadyMatched && cm.m == null) {
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
		return "M:" + label.id;
	}
}
