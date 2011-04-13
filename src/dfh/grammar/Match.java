package dfh.grammar;

import java.util.LinkedList;
import java.util.List;

/**
 * Node in an match tree.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class Match {
	/**
	 * {@link MatchTest} that finds terminal matches. For use in
	 * {@link #passingMatches(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest TERMINAL = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.isTerminal();
		}
	};
	/**
	 * {@link MatchTest} that finds non-terminal matches. For use in
	 * {@link #passingMatches(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest NON_TERMINAL = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return !m.isTerminal();
		}
	};
	/**
	 * {@link MatchTest} that finds matches for named rules -- any rule with its
	 * own line in the grammar definition. For use in
	 * {@link #passingMatches(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest NAMED = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.explicit();
		}
	};
	/**
	 * {@link MatchTest} that finds matches for anonymous rules -- rules without
	 * their own explicit label. For use in {@link #passingMatches(MatchTest)}
	 * and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest ANONYMOUS = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return !m.explicit();
		}
	};
	/**
	 * {@link MatchTest} that finds all matches. For use in
	 * {@link #passingMatches(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest ALL = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return true;
		}
	};
	/**
	 * {@link MatchTest} that finds zero-width matches. For use in
	 * {@link #passingMatches(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest ZERO_WIDTH = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.zeroWidth();
		}
	};
	/**
	 * {@link MatchTest} that finds matches with non-zero width. For use in
	 * {@link #passingMatches(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest WIDE = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return !m.zeroWidth();
		}
	};
	private final Rule r;
	private final int start;
	private int end = -1;
	private Match parent;
	private Match[] children;
	/**
	 * Used as a placeholder in offset cache.
	 */
	public static final Match DUMMY = new Match();

	/**
	 * Makes a DUMMY node. This serves as the opposite of <code>null</code> in
	 * matching caches.
	 */
	private Match() {
		r = null;
		start = end = -1;
		parent = this;
	}

	/**
	 * Generates {@link Match} with undefined terminal offset.
	 * 
	 * @param r
	 * @param start
	 */
	public Match(Rule r, int start) {
		this.r = r;
		this.start = start;
	}

	/**
	 * Generates {@link Match} with all offsets defined.
	 * 
	 * @param r
	 * @param start
	 * @param end
	 */
	public Match(Rule r, int start, int end) {
		this(r, start);
		this.end = end;
	}

	/**
	 * @return {@link Rule} that generated this {@link Match}
	 */
	public Rule rule() {
		return r;
	}

	/**
	 * @return index of first character at which the {@link Match} matches
	 */
	public int start() {
		return start;
	}

	/**
	 * @return index one past the last character at which the {@link Match}
	 *         matches
	 */
	public int end() {
		return end;
	}

	/**
	 * Sets end offset. To be called during matching. If this is called on a
	 * retrieved {@link Match} an error will be thrown.
	 * 
	 * @param end
	 *            end offset
	 */
	public void setEnd(int end) {
		if (this.end > -1)
			throw new GrammarException("match end cannot be reset");
		this.end = end;
	}

	/**
	 * @return parent {@link Match}, if any
	 */
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

	/**
	 * This method is to be called during matching. After the match children
	 * have been set this will throw an error.
	 * 
	 * @param children
	 *            child matches
	 */
	public void setChildren(Match[] children) {
		if (this.children != null)
			throw new GrammarException("match children cannot be reset");
		this.children = children;
	}

	/**
	 * @return matches nested inside this one
	 */
	public Match[] children() {
		return children;
	}

	@Override
	public String toString() {
		if (this == DUMMY)
			return "[DUMMY]";
		StringBuilder b = new StringBuilder();
		b.append('(').append(r.description()).append(": ");
		b.append(start).append(", ").append(end);
		if (children != null && children.length > 0) {
			b.append(" [");
			boolean nonInitial = false;
			for (Match n : children) {
				if (nonInitial)
					b.append(", ");
				else
					nonInitial = true;
				b.append(n);
			}
			b.append(']');
		}
		b.append(')');
		return b.toString();
	}

	/**
	 * Returns index of {@link Match} among its parent's children. It discovers
	 * this index by linear search among its parent's children. As such searches
	 * go this is relatively efficient since it uses object identity rather than
	 * equality and the sequence iterated over is a usually short array.
	 * 
	 * @return index of {@link Match} among its parent's children
	 */
	public int index() {
		if (parent == null)
			return -1;
		int index = 0;
		for (Match m : parent.children) {
			if (m == this)
				return index;
			index++;
		}
		throw new GrammarException(
				"impossible state: match not among its parent's children");
	}

	/*
	 * some tests
	 */

	/**
	 * @return whether this {@link Match} has no children
	 */
	public boolean isTerminal() {
		return children == null || children.length == 0;
	}

	/**
	 * @return whether this {@link Match} has no parent
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Returns whether the {@link Rule} that generated this {@link Match} has
	 * the given name.
	 * 
	 * @param name
	 *            a {@link Label#id}
	 * @return whether the {@link Rule} that generated this {@link Match} has
	 *         the given id
	 */
	public boolean hasLabel(String name) {
		return r.label().id.equals(name);
	}

	/**
	 * @param label
	 * @return whether this {@link Match} or any of its descendants was
	 *         generated by the {@link Rule} with the given id
	 */
	public boolean has(String label) {
		if (rule().label.id.equals(label))
			return true;
		if (children != null) {
			for (Match m : children) {
				if (m.has(label))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns list of matches dominated by this that pass the given test. The
	 * search is depth first. Children are returned before parents.
	 * 
	 * @param t
	 *            test
	 * @return list of matches dominated by this that pass the given test
	 */
	public LinkedList<Match> passingMatches(MatchTest t) {
		LinkedList<Match> list = new LinkedList<Match>();
		passingMatches(t, list);
		return list;
	}

	/**
	 * Recursively applies given test to this {@link Match} and its children.
	 * 
	 * @param t
	 * @return whether this or any {@link Match} dominated by this passes the
	 *         test
	 */
	public boolean passes(MatchTest t) {
		if (t.test(this))
			return true;
		if (children != null) {
			for (Match m : children) {
				if (m.passes(t))
					return true;
			}
		}
		return false;
	}

	private void passingMatches(MatchTest t, List<Match> accumulator) {
		if (children != null) {
			for (Match m : children)
				m.passingMatches(t, accumulator);
		}
		if (t.test(this))
			accumulator.add(this);
	}

	/**
	 * Returns whether {@link Match} corresponds to a region with no width.
	 * <code>(?=fred)</code> and <code>'fred'*</code> can both produce
	 * zero-width matches.
	 * 
	 * @return whether {@link Match} corresponds to a region with no width
	 */
	public boolean zeroWidth() {
		return start == end;
	}

	/**
	 * Returns whether this {@link Match} corresponds to a named {@link Rule},
	 * as opposed to an implicit rule. For example, in
	 * 
	 * <pre>
	 * &lt;ROOT&gt; = &lt;fred&gt;++
	 * &lt;fred&gt; = 'fred' | 'Fred'
	 * </pre>
	 * 
	 * <code>&lt;ROOT&gt;</code> and <code>&lt;fred&gt;</code> are explicit
	 * while <code>'fred'</code>, <code>&lt;fred&gt;++</code> and so forth are
	 * implicit.
	 * <p>
	 * Any rule defined on its own line with its own label to the left of the
	 * equals sign is an explicit rule.
	 * 
	 * @return whether this {@link Match} corresponds to a named {@link Rule}
	 */
	public boolean explicit() {
		return r.generation > -1;
	}
}
