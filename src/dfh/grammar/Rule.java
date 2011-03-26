package dfh.grammar;

import java.io.Serializable;
import java.util.Map;

/**
 * A {@link Matcher} generator. <code>Rules</code> generate
 * <code>Matchers</code> with properly initialized state but have no dynamic
 * state of their own. It is convenient to define <code>Matchers</code> as inner
 * classes of their <code>Rules</code>, since one generally doesn't interact
 * with them apart from their rules and they need access to the
 * <code>Rule</code> that generated them in order to include a reference to it
 * in the {@link Match} nodes they generate.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public abstract class Rule implements Serializable {
	private static final long serialVersionUID = 1L;

	final Label label;
	Grammar g;
	int generation = -1;

	public Rule(Label label) {
		this.label = label;
	}

	/**
	 * Unique tag associated with the <code>Rule</code>. The label corresponds
	 * to the part of a rule definition to the left of the "=".
	 * 
	 * @return unique tag associated with the <code>Rule</code>
	 */
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
	 * Returns a {@link String} representing with as much precision as possible
	 * the pattern applied by this rule. This means if this rule depends on
	 * other rules its unique id should incorporate their unique ids (but be
	 * careful of cycles; see {@link CyclicRule}).
	 * <p>
	 * The purpose of the unique id is to allow the {@link Compiler} to discover
	 * rules with identical patterns. To improve the efficiency of the cache and
	 * reduce memory use, every rule should have a unique pattern. The compiler
	 * uses the unique id to discover and remove redundancy.
	 * 
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
			stackTrace(b, m);
			g.trace.println(b);
		}
	}

	void stackTrace(StringBuilder b, Matcher m) {
		b.append("\n     ");
		while (true) {
			b.append(m.rule().label());
			b.append(' ');
			b.append(m.offset);
			if (m.master == null)
				break;
			else {
				b.append(" :: ");
				m = m.master;
			}
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
			b.append("  ");
			b.append(label());
			locate(b, m.s, m.offset);
			b.append(" returning ");
			b.append(n);
			if (n != null) {
				b.append(" = '");
				b.append(m.s.subSequence(n.start(), n.end()));
				b.append('\'');
			}
			stackTrace(b, m);
			g.trace.println(b);
		}
	}

	/**
	 * The rule description is invoked by {@link Grammar#describe()}.
	 * 
	 * @return a String describing the rule
	 */
	public abstract String description();
}
