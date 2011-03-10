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
	private final int start, end;
	private final Node parent;

	public Node(Rule r, int start, int end, Node parent) {
		this.r = r;
		this.start = start;
		this.end = end;
		this.parent = parent;
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

	public Node parent() {
		return parent;
	}
}
