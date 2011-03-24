package dfh.grammar;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public abstract class Rule implements Serializable {
	private static final long serialVersionUID = 1L;

	final Label label;
	Grammar g;

	public Rule(Label label) {
		this.label = label;
	}

	public Label label() {
		return label;
	}

	/**
	 * Creates a {@link Matcher} to keep track of backtracking and the matching
	 * cache at this offset.
	 * 
	 * @param s
	 *            sequence to match against
	 * @param offset
	 *            offset at which to begin the match
	 * @param parent
	 *            parent node for use in constructing the match tree
	 * @param cache
	 *            collection of offset matching caches
	 * @param master
	 *            reference to enclosing {@link Matcher} for use in
	 *            backreference testing
	 * @return
	 */
	public abstract Matcher matcher(CharSequence s, Integer offset,
			Match parent, Map<Label, Map<Integer, CachedMatch>> cache,
			Matcher master);

	@Override
	public String toString() {
		return label.id;
	}

	/**
	 * @return label-free id used to recognize redundancy during compilation
	 */
	protected abstract String uniqueId();

	/**
	 * Debugging output to print before matching.
	 * 
	 * @param m
	 */
	void matchTrace(Matcher m) {
		if (g.trace != null) {
			StringBuilder b = new StringBuilder();
			b.append(label());
			locate(b, m.s, m.offset);
			b.append("\n     ").append(m.parent);
			g.trace.println(b);
		}
	}

	private void locate(StringBuilder b, CharSequence s, int offset) {
		b.append(" at ");
		b.append(offset);
		b.append(" (");
		int start = Math.max(0, offset - 5);
		int end = Math.min(s.length(), offset + 5);
		if (start < offset) {
			b.append('"');
			b.append(s.subSequence(start, offset));
			b.append('"');
		}
		b.append('_');
		if (end > offset) {
			b.append('"');
			b.append(s.subSequence(offset, end));
			b.append('"');
		}
		b.append(')');
	}

	/**
	 * Debugging output to print after matching.
	 * 
	 * @param m
	 * @param s
	 */
	void matchTrace(Matcher m, Match n) {
		if (g.trace != null) {
			StringBuilder b = new StringBuilder();
			b.append("   ");
			b.append(label());
			locate(b, m.s, m.offset);
			b.append(" returning ");
			b.append(n);
			if (n != null) {
				b.append(" = '");
				b.append(m.s.subSequence(n.start(), n.end()));
				b.append('\'');
			}
			b.append("\n        ").append(m.parent);
			g.trace.println(b);
			if (n == null)
				g.stack.add(n);
		}
	}

	/**
	 * For use in {@link Matcher} during debugging.
	 * 
	 * @return whether we are debugging
	 */
	void push(Match m) {
		if (g.trace != null)
			g.stack.add(m);
	}

	void pop() {
		if (g.trace != null)
			g.stack.removeLast();
	}
}
