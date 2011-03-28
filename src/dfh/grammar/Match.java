package dfh.grammar;

/**
 * Node in an AST tree.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class Match {
	private final Rule r;
	private final int start;
	private int end;
	private Match parent;
	private Match[] children;
	/**
	 * Used as a placeholder in offset cache.
	 */
	public static final Match dummy = new Match();

	/**
	 * Makes a dummy node. This serves as the opposite of <code>null</code> in
	 * matching caches.
	 */
	private Match() {
		r = null;
		start = end = -1;
		parent = this;
	}

	Match(Rule r, int start) {
		this.r = r;
		this.start = start;
	}

	Match(Rule r, int start, int end) {
		this(r, start);
		setEnd(end);
	}

	public Rule rule() {
		return r;
	}

	public int start() {
		return start;
	}

	public int end() {
		return end;
	}

	void setEnd(int end) {
		this.end = end;
	}

	public Match parent() {
		return parent;
	}

	/**
	 * Recursively defines link from child to parent.
	 */
	void establishParentage() {
		if (children == null)
			children = new Match[0];
		else {
			for (Match child : children) {
				if (child.parent != null)
					return;
				else {
					child.parent = this;
					child.establishParentage();
				}
			}
		}
	}

	void setChildren(Match[] children) {
		this.children = children;
	}

	public Match[] children() {
		return children;
	}

	@Override
	public String toString() {
		if (this == dummy)
			return "[DUMMY]";
		StringBuilder b = new StringBuilder();
		b.append('[').append(r.label).append(' ');
		b.append(start).append(", ").append(end);
		if (children != null && children.length > 0) {
			b.append(' ');
			boolean nonInitial = false;
			for (Match n : children) {
				if (nonInitial)
					b.append(", ");
				else
					nonInitial = true;
				b.append(n);
			}
		}
		b.append(']');
		return b.toString();
	}
}
