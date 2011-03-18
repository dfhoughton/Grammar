package dfh.grammar;

import java.util.Map;

public abstract class NonterminalMatcher implements Matcher {

	protected final int offset;
	protected final Node parent;

	protected abstract void fetchNext();

	protected final char[] cs;
	protected Node next;
	protected boolean done = false;
	protected final Map<Label, Map<Integer, Node>> cache;
	protected final Map<Integer, Node> subCache;
	private final Label label;

	protected NonterminalMatcher(char[] cs, int offset, Node parent,
			Map<Label, Map<Integer, Node>> cache, Label label) {
		this.cs = cs;
		this.offset = offset;
		this.parent = parent;
		this.cache = cache;
		this.label = label;
		this.subCache = cache.get(label);
	}

	@Override
	public Node match() {
		if (done)
			return null;
		boolean alreadyMatched = subCache.containsKey(offset);
		if (alreadyMatched && subCache.get(offset) == null)
			return null;
		if (next == null)
			fetchNext();
		if (!alreadyMatched)
			subCache.put(offset, next == null ? null : Node.dummy);
		Node n = next;
		next = null;
		return n;
	}

	@Override
	public boolean mightHaveNext() {
		if (done)
			return false;
		if (next == null)
			fetchNext();
		return next != null;
	}

	@Override
	public String toString() {
		return "M:" + label.id;
	}
}
