package dfh.grammar;

import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.text.Segment;

/**
 * {@link Rule} defined over sequence of terminal objects rather than other
 * <code>Rules</code>.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class LeafRule extends Rule {
	private class LeafMatcher implements Matcher {
		private final Node parent;
		private final int offset;
		private final char[] chars;
		private Map<Integer, Node> cache;

		public LeafMatcher(char[] chars, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache) {
			this.chars = chars;
			this.parent = parent;
			this.offset = offset;
			this.cache = cache.get(label);
		}

		@Override
		public Node match() {
			if (cache.containsKey(offset)) {
				return cache.get(offset);
			}
			Segment s = new Segment(chars, offset, chars.length - offset);
			java.util.regex.Matcher m = p.matcher(s);
			Node n = null;
			if (m.lookingAt())
				n = new Node(LeafRule.this, offset, m.end() + offset, parent);
			cache.put(offset, n);
			return n;
		}

		@Override
		public boolean mightHaveNext() {
			return cache.containsKey(offset);
		}

		@Override
		public String toString() {
			return "M:" + LeafRule.this;
		}

		@Override
		public Rule rule() {
			return LeafRule.this;
		}
	}

	private static final long serialVersionUID = 1L;
	private final Pattern p;

	public LeafRule(Label label, Pattern p) {
		super(label);
		this.p = p;
	}

	@Override
	public Matcher matcher(char[] s, final int offset, Node parent,
			Map<Label, Map<Integer, Node>> cache) {
		return new LeafMatcher(s, offset, parent, cache);
	}
}
