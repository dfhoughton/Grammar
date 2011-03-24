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
	 * @param s
	 * @param offset
	 */
	void matchTrace(Matcher m, CharSequence s, int offset) {
		if (g.trace != null) {
			StringBuilder b = new StringBuilder();
			b.append(m.identify());
			locate(b, s, offset);
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
		b.append('v');
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
	 * @param offset
	 * @param n
	 */
	void matchTrace(Matcher m, CharSequence s, int offset, Match n) {
		if (g.trace != null) {
			StringBuilder b = new StringBuilder();
			b.append("   ");
			b.append(m.identify());
			locate(b, s, offset);
			b.append(" returning ");
			b.append(n);
			if (n != null) {
				b.append(" = ");
				b.append(s.subSequence(n.start(), n.end()));
			}
			g.trace.println(b);
		}
	}
}
