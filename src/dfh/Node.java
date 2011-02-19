package dfh;

/**
 * Node in an AST tree.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 * 
 * @param <K>
 *            type of object over which tree is defined
 */
public class Node<K> {
	private final Rule<K> r;
	private final int start, end;
	private final Node<K> parent;
	
	public Node(Rule<K> r, int start, int end, Node<K> parent) {
		this.r = r;
		this.start = start;
		this.end = end;
		this.parent = parent;
	}
	
	public Rule<K> rule() {
		return r;
	}
	
	public int start() {
		return start;
	}
	
	public int end() {
		return end;
	}
	
	public Node<K> parent() {
		return parent;
	}
}
