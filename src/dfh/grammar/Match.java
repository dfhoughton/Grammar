package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dfh.grammar.util.Dotify;

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
	 * {@link #get(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest TERMINAL = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.isTerminal();
		}
	};
	/**
	 * {@link MatchTest} that finds non-terminal matches. For use in
	 * {@link #get(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest NON_TERMINAL = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return !m.isTerminal();
		}
	};
	/**
	 * {@link MatchTest} that finds matches for named rules -- any rule with its
	 * own line in the grammar definition. For use in {@link #get(MatchTest)}
	 * and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest NAMED = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.explicit();
		}
	};
	/**
	 * {@link MatchTest} that finds matches for anonymous rules -- rules without
	 * their own explicit label. For use in {@link #get(MatchTest)} and
	 * {@link #passes(MatchTest)}.
	 */
	public static final MatchTest ANONYMOUS = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return !m.explicit();
		}
	};
	/**
	 * {@link MatchTest} that finds all matches. For use in
	 * {@link #get(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest ALL = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return true;
		}
	};
	/**
	 * {@link MatchTest} that finds zero-width matches. For use in
	 * {@link #get(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest ZERO_WIDTH = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.zeroWidth();
		}
	};
	/**
	 * {@link MatchTest} that finds matches with non-zero width. For use in
	 * {@link #get(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest WIDE = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return !m.zeroWidth();
		}
	};
	/**
	 * {@link MatchTest} that finds matches with non-zero width leaves. For use
	 * in {@link #get(MatchTest)} and {@link #passes(MatchTest)}.
	 */
	public static final MatchTest WIDE_LEAF = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.isTerminal() && !m.zeroWidth();
		}
	};
	private final Rule r;
	private final int start;
	private int end = -1;
	private Match parent;
	private Match[] children;
	private String group;
	private Set<String> labels;
	/**
	 * Indicates that all terminal modifications have been completed and all
	 * methods are available.
	 */
	private boolean done = false;
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
		done = true;
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
		b.append('(').append(r.description(false)).append(": ");
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
	 * the given name or tag.
	 * <p>
	 * Cannot be used in {@link Condition}; {@link GrammarException} will be
	 * thrown.
	 * 
	 * @param name
	 *            a {@link Label#id}
	 * @return whether the {@link Rule} that generated this {@link Match} has
	 *         the given id
	 */
	public boolean hasLabel(String name) {
		return labels().contains(name);
	}

	/**
	 * Debugging method that calls {@link Dotify#dot(Match)}.
	 * 
	 * @return stringification of this notable suitable for graphing by
	 *         GraphViz, etc.
	 */
	public String dot() {
		return Dotify.dot(this);
	}

	/**
	 * Returns set of strings to which {@link #hasLabel(String)} will return
	 * <code>true</code> for this {@link Match}. This method is useful for
	 * debugging but not optimized for speed.
	 * <p>
	 * Cannot be used in {@link Condition}; {@link GrammarException} will be
	 * thrown.
	 * 
	 * @return set of strings to which {@link #hasLabel(String)} will return
	 *         <code>true</code> for this {@link Match}
	 */
	public synchronized Set<String> labels() {
		if (done) {
			if (labels == null) {
				labels = new TreeSet<String>();
				labels.add(r.label.id);
				labels.add(r.uid());
				r.addLabels(labels);
				if (parent != null)
					parent.r.addLabels(this, labels);
			}
			return labels;
		} else
			throw new GrammarException(
					"labels() only available after grammar has completed match");
	}

	/**
	 * Returns whether this object has the given object as an ancestor. This is
	 * requires an identity match -- '==' -- not {@link #equals(Object)}.
	 * 
	 * @param m
	 *            potential ancestor
	 * @return whether this object has the given object as an ancestor
	 */
	public boolean hasAncestor(Match m) {
		if (m == null || parent == null)
			return false;
		if (parent == m)
			return true;
		return parent.hasAncestor(m);
	}

	/**
	 * @param label
	 * @return whether this {@link Match} or any of its descendants was
	 *         generated by the {@link Rule} with the given id
	 */
	public boolean has(String label) {
		if (hasLabel(label))
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
	 * Walk the match tree, applying the test to every node encountered. If the
	 * test evaluates to true, any child nodes are skipped; otherwise the
	 * children are walked as well.
	 * <p>
	 * You can only walk the match tree after matching has completed. Doing
	 * otherwise will cause a runtime error to be thrown.
	 * 
	 * @param t
	 *            the code to apply to each node encountered
	 * @return whether the test ever evaluated to true
	 */
	public boolean walk(MatchTest t) {
		if (done) {
			if (t.test(this))
				return true;
			boolean matched = false;
			for (Match c : children)
				matched = c.walk(t) || matched;
			return matched;
		}
		throw new GrammarException(
				"dfh.grammar.Match.walk(dfh.grammar.MatchTest) can only be called after matching has completed");
	}

	/**
	 * Returns list of matches dominated by this that pass the given test. The
	 * search is depth first. Children are returned before parents.
	 * 
	 * @param t
	 *            test
	 * @return list of matches dominated by this that pass the given test
	 */
	public LinkedList<Match> get(MatchTest t) {
		LinkedList<Match> list = new LinkedList<Match>();
		passingMatches(t, list);
		return list;
	}

	/**
	 * Returns a {@link Match} passing the given test if any exists in the tree
	 * rooted at this {@link Match}. The names is an allusion to the axiom of
	 * choice. The actual procedure is a recursive, left-right search returning
	 * parents in preference to children.
	 * 
	 * @param t
	 *            condition on sought node
	 * @return {@link Match} passing test
	 */
	public Match choose(MatchTest t) {
		if (t.test(this))
			return this;
		if (children != null) {
			for (Match n : children) {
				Match chosen = n.choose(t);
				if (chosen != null)
					return chosen;
			}
		}
		return null;
	}

	/**
	 * Returns return highest nodes on match tree rooted at this node and
	 * passing the test. The current node is ignored. Any descendants of a
	 * matching descendant are ignored.
	 * 
	 * @param t
	 * @return highest nodes on match tree rooted at this node and passing the
	 *         test
	 */
	public LinkedList<Match> getClosestDescendants(MatchTest t) {
		LinkedList<Match> list = new LinkedList<Match>();
		if (children != null) {
			for (Match child : children)
				child.closestMatches(t, list);
		}
		return list;
	}

	/**
	 * Obtains highest nodes in tree matching given test, ignoring the
	 * descendants of any node that matches.
	 * 
	 * @param t
	 * @param accumulator
	 */
	private void closestMatches(MatchTest t, LinkedList<Match> accumulator) {
		if (t.test(this))
			accumulator.add(this);
		else if (children != null) {
			for (Match m : children)
				m.closestMatches(t, accumulator);
		}
	}

	/**
	 * @param t
	 * @return closest ancestor {@link Match} passing given test
	 */
	public Match getAncestor(MatchTest t) {
		if (parent == null)
			return null;
		if (t.test(parent))
			return parent;
		return parent.getAncestor(t);
	}

	/**
	 * @param t
	 * @return ancestor {@link Match Matches} passing given test in order of
	 *         proximity in parse tree
	 */
	public List<Match> getAncestors(MatchTest t) {
		return passingAncestors(t, new LinkedList<Match>());
	}

	private List<Match> passingAncestors(MatchTest t, List<Match> accumulator) {
		if (parent == null)
			return accumulator;
		if (t.test(parent))
			accumulator.add(parent);
		return parent.passingAncestors(t, accumulator);
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
	 * Returns whether {@link Match} corresponds to a region with no width. For
	 * example, <code>(?=fred)</code> and <code>'fred'*</code> can both produce
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

	/**
	 * Convenience method that applies {@link #TERMINAL} to
	 * {@link #get(MatchTest)}.
	 * 
	 * @return list of terminal matches
	 */
	public List<Match> leaves() {
		return get(TERMINAL);
	}

	/**
	 * Convenience method that applies {@link #WIDE_LEAF} to
	 * {@link #get(MatchTest)}.
	 * 
	 * @return list of terminal matches
	 */
	public List<Match> wideLeaves() {
		return get(WIDE_LEAF);
	}

	/**
	 * Returns {@link Match Matches} generated by the given rule in depth-first
	 * left-to-right order. Work is delegated to {@link #get(MatchTest)} with an
	 * anonymous test generated on each call. Some efficiency can be gained by
	 * using {@link #get(MatchTest)} with a pre-generated test.
	 * 
	 * @param rule
	 * @return {@link Match Matches} generated by the given rule
	 */
	public List<Match> get(final String rule) {
		return get(new MatchTest() {
			@Override
			public boolean test(Match m) {
				return m.hasLabel(rule);
			}
		});
	}

	public Match choose(final String rule) {
		return choose(new MatchTest() {
			@Override
			public boolean test(Match m) {
				return m.hasLabel(rule);
			}
		});
	}

	/**
	 * Returns nearest ancestor generated by given rule. Delegates to
	 * {@link #getAncestor(MatchTest)}.
	 * 
	 * @param rule
	 * @return nearest ancestor generated by given rule
	 */
	public Match getAncestor(final String rule) {
		return getAncestor(new MatchTest() {
			@Override
			public boolean test(Match m) {
				return m.hasLabel(rule);
			}
		});
	}

	/**
	 * Returns ancestors generated by given rule. Delegates to
	 * {@link #getAncestors(MatchTest)}.
	 * 
	 * @param rule
	 * @return ancestors generated by given rule
	 */
	public List<Match> getAncestors(final String rule) {
		return getAncestors(new MatchTest() {
			@Override
			public boolean test(Match m) {
				return m.hasLabel(rule);
			}
		});
	}

	/**
	 * Returns whether any descendant passes given test.
	 * 
	 * @param t
	 * @return whether any descendant passes given test
	 */
	public boolean descendantPasses(MatchTest t) {
		if (children != null) {
			for (Match m : children) {
				if (t.test(m))
					return true;
			}
			for (Match m : children) {
				if (m.descendantPasses(t))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether this {@link Match} or any of its descendants passes the
	 * given test.
	 * 
	 * @param t
	 * @return whether this {@link Match} or any of its descendants passes the
	 *         given test
	 */
	public boolean selfOrDescendantPasses(MatchTest t) {
		return t.test(this) || descendantPasses(t);
	}

	/**
	 * Returns whether any ancestor {@link Match} of this in the parse tree
	 * passes the given test.
	 * 
	 * @param t
	 * @return whether any ancestor {@link Match} of this in the parse tree
	 *         passes the given test
	 */
	public boolean ancestorPasses(MatchTest t) {
		if (parent == null)
			return false;
		if (t.test(parent))
			return true;
		return parent.ancestorPasses(t);
	}

	/**
	 * Returns whether this {@link Match} or any of its ancestors in the parse
	 * tree passes the given test.
	 * 
	 * @param t
	 * @return whether this {@link Match} or any of its ancestors in the parse
	 *         tree passes the given test
	 */
	public boolean selfOrAncestorPasses(MatchTest t) {
		return t.test(this) || ancestorPasses(t);
	}

	/**
	 * Tests whether the two matches cover a common subsequence.
	 * 
	 * @param other
	 * @return whether matches have a common subsequence
	 */
	public boolean overlaps(Match other) {
		if (end <= other.start || other.end <= start)
			return false;
		return true;
	}

	/**
	 * Returns a string corresponding to the subsequence matched.
	 * <p>
	 * Note that the group is not recorded until the {@link Match} is returned
	 * by the {@link Grammar}, so this string isn't available during the
	 * matching process. If you need this during debugging you'll have to use
	 * {@link CharSequence#subSequence(int, int)}, using {@link Match#start()}
	 * and {@link Match#end()} as the two parameters.
	 * 
	 * @return a string corresponding to the subsequence matched
	 */
	public String group() {
		return group;
	}

	/**
	 * Records subsequence this match corresponds to.
	 * 
	 * @param s
	 *            mother sequence of match
	 */
	void setGroup(CharSequence s) {
		group = s.subSequence(start, end).toString();
		if (children != null) {
			for (Match child : children)
				child.setGroup(s);
		}
	}
}
