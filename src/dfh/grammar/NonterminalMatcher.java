package dfh.grammar;

import java.util.Map;

public abstract class NonterminalMatcher implements Matcher {

	protected final int offset;
	protected final Match parent;

	protected abstract void fetchNext();

	protected final CharSequence cs;
	protected Match next;
	protected boolean done = false;
	protected final Map<Label, Map<Integer, Match>> cache;
	protected final Map<Integer, Match> subCache;
	protected final Label label;
	protected final Matcher master;

	protected NonterminalMatcher(CharSequence cs2, int offset, Match parent,
			Map<Label, Map<Integer, Match>> cache, Label label, Matcher master) {
		this.cs = cs2;
		this.offset = offset;
		this.parent = parent;
		this.cache = cache;
		this.label = label;
		this.subCache = cache.get(label);
		this.master = master;
	}

	@Override
	public Match match() {
		System.err.println("match " + this.getClass().getSimpleName() + " "
				+ identify() + " at '" + cs.subSequence(offset, cs.length())
				+ "'");
		if (done)
			return null;
		boolean alreadyMatched = subCache.containsKey(offset);
		if (alreadyMatched && subCache.get(offset) == null) {
			System.err.println("m " + this + " "
					+ this.getClass().getSimpleName().substring(0, 2)
					+ " returning null" + " at '"
					+ cs.subSequence(offset, cs.length()) + "'");
			return null;
		}
		if (next == null)
			fetchNext();
		if (!alreadyMatched)
			subCache.put(offset, next == null ? null : Match.dummy);
		Match n = next;
		next = null;
		System.err.println("m "
				+ this
				+ " "
				+ this.getClass().getSimpleName().substring(0, 2)
				+ " returning "
				+ (n == null ? "null" : cs.subSequence(n.start(), n.end())
						+ " (" + n + ")" + " at '"
						+ cs.subSequence(offset, cs.length()) + "'"));
		return n;
	}

	@Override
	public boolean mightHaveNext() {
		System.err.println("mightHaveNext " + this.getClass().getSimpleName()
				+ " " + identify() + " at '"
				+ cs.subSequence(offset, cs.length()) + "'");
		if (done) {
			System.err.println("mhn " + this + " returning false" + " at '"
					+ cs.subSequence(offset, cs.length()) + "'");
			return false;
		}
		if (next == null)
			fetchNext();
		System.err.println("mhn " + this + " returning " + (next != null)
				+ " at '" + cs.subSequence(offset, cs.length()) + "'");
		return next != null;
	}

	@Override
	public String toString() {
		return "M:" + label.id;
	}
}
