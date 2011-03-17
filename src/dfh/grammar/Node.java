package dfh.grammar;

/**
 * Node in an AST tree.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class Node {
	private final Rule r;
	private final int start;
	private int end;
	private final Node parent;
	private Node[] children;
	/**
	 * Used as a placeholder in offset cache.
	 */
	public static final Node dummy = new Node();

	/**
	 * Makes a dummy node.
	 */
	private Node() {
		r = null;
		start = end = -1;
		parent = null;
		setChildren(null);
	}

	public Node(Rule r, int start, Node parent) {
		this.r = r;
		this.start = start;
		this.parent = parent;
	}

	public Node(Rule r, int start, int end, Node parent) {
		this(r, start, parent);
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

	public Node parent() {
		return parent;
	}

	void setChildren(Node[] children) {
		this.children = children;
	}

	public Node[] children() {
		return children;
	}
}
