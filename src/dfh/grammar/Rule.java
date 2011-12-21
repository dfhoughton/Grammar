package dfh.grammar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	/**
	 * The rule's name. Though it is not <code>final</code>, it should be
	 * treated as such.
	 */
	protected Label label;
	/**
	 * Cached unique id.
	 */
	protected String uid;
	/**
	 * The index of the offset cache for this rule. Set to -1 until it is set by
	 * {@link #setCacheIndex(Map)}.
	 */
	protected int cacheIndex = -1;
	/**
	 * Used by {@link Grammar#describe()}.
	 */
	int generation = -1;

	/**
	 * Used during compilation to hold condition identifier for use in
	 * constructing unique ids. See {@link #uniqueId()}.
	 */
	protected String condition;

	/**
	 * A set of labels associated with this rule wherever it appears. Such
	 * labels are assigned like so:
	 * 
	 * <pre>
	 * foo = [{bar,quux} "the rule" ]
	 * baz = "qux" [{twiddle} /in line regex/ ]
	 * </pre>
	 * 
	 * That is, it is a pure "named capture".
	 */
	private Set<String> labels;

	/**
	 * Assigns given label to {@link Rule}.
	 * 
	 * @param label
	 */
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
	 * @param cache
	 *            collection of offset matching caches
	 * @param master
	 *            reference to enclosing {@link Matcher} for use in
	 *            backreference testing
	 * @return {@link Matcher} initialized to manage matching state at this
	 *         offset
	 */
	public abstract Matcher matcher(CharSequence s, Integer offset,
			Map<Integer, CachedMatch>[] cache, Matcher master);

	@Override
	public String toString() {
		return label.id;
	}

	public String uid() {
		return uid;
	}

	protected void setUid() {
		if (uid == null)
			uid = uniqueId();
	}

	/**
	 * @return memory address, useful for debugging
	 */
	protected String address() {
		String a = super.toString();
		a = a.substring(a.lastIndexOf('@') + 1);
		return a;
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
	 *            matcher performing match
	 */
	protected final void matchTrace(Matcher m) {
		StringBuilder b = new StringBuilder();
		b.append("\nmatching ").append(label()).append("\n\t");
		locate(b, m.s, m.offset);
		stackTrace(b, m);
		m.options.trace.println(b);
	}

	/**
	 * Print message to debugging stream along with stack trace.
	 * 
	 * @param m
	 * @param message
	 */
	protected final void event(Matcher m, String message) {
		if (m.options.debug) {
			StringBuilder b = new StringBuilder();
			b.append("{{ event: ");
			b.append(message);
			locate(b, m.s, m.offset);
			stackTrace(b, m);
			b.append("\n}}");
			m.options.trace.println(b);
		}
	}

	private void stackTrace(StringBuilder b, Matcher m) {
		b.append("\nstack:\n\t");
		List<String> labels = new ArrayList<String>();
		while (m.rule() != null) {
			labels.add(String.format("%s %d", m.rule().label(), m.offset));
			if (m.master != null)
				m = m.master;
		}
		Collections.reverse(labels);
		boolean nonInitial = false;
		for (String s : labels) {
			if (nonInitial)
				b.append(" :: ");
			else
				nonInitial = true;
			b.append(s);
		}
	}

	private void locate(StringBuilder b, CharSequence s, int offset) {
		b.append(" at ");
		b.append(offset);
		b.append(" (");
		int start = Math.max(0, offset - 5);
		int end = Math.min(s.length(), offset + 5);
		if (start < offset) {
			if (start > 0)
				b.append("...");
			b.append('"');
			b.append(s.subSequence(start, offset));
			b.append('"');
		}
		b.append('_');
		if (end > offset) {
			b.append('"');
			b.append(s.subSequence(offset, end));
			b.append('"');
			if (end < s.length())
				b.append("...");
		}
		b.append(')');
	}

	/**
	 * Debugging output to print after matching. This includes any match
	 * returned and a matching stack trace.
	 * 
	 * @param m
	 *            the {@link Matcher} matching
	 * @param n
	 *            the {@link Match} returned
	 */
	protected final void matchTrace(Matcher m, Match n) {
		StringBuilder b = new StringBuilder();
		b.append("result for ").append(m.rule().label).append("\n\t");
		if (n == null)
			b.append("NO MATCH");
		else
			b.append(String.format("(%d, %d) \"%s\"", n.start(), n.end(),
					m.s.subSequence(n.start(), n.end())));
		m.options.trace.println(b);
	}

	/**
	 * The rule description is invoked by {@link Grammar#describe()}. It is the
	 * text that appears after the '=' in a rule definition.
	 * 
	 * @return a String describing the rule
	 */
	public abstract String description();

	/**
	 * Prepare for matching against the given {@link CharSequence}. This is an
	 * optimization to allow terminal rules to cache matches and allow the root
	 * {@link Matcher} to skip impossible matching offsets.
	 * 
	 * @param s
	 *            {@link CharSequence} to be matched against
	 * @param cache
	 *            match cache that will be used
	 * @param studiedRules
	 *            cache to forestall the re-studying of a rule
	 * @param options
	 *            {@link GlobalState} in use in this match
	 * @return set of start offsets of matches
	 */
	public abstract Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, Set<Rule> studiedRules,
			GlobalState options);

	/**
	 * Returns whether this rule can match the null string. This method is used
	 * in {@link #study(CharSequence, Map, Set, GlobalState) studying} in
	 * {@link CharSequence}. When in doubt, default to <code>true</code>. This
	 * may make the matching less efficient but is less likely to cause the
	 * {@link Grammar} to miss a match.
	 * <p>
	 * Note that {@link LiteralRule} and {@link LeafRule} both return false.
	 * This is because if either of these can legitimately match a zero-width
	 * string in the character sequence,
	 * <em>this will show up as a match when studying the sequence</em>.
	 * Compositional rules like {@link SequenceRule}, {@link AlternationRule},
	 * and {@link RepetitionRule}, however, need this because their study method
	 * may return a spurious empty set.
	 * 
	 * @return whether this rule can match the null string
	 */
	public abstract boolean zeroWidth();

	/**
	 * Returns clone of this {@link Rule} but not of any {@link Rule Rules} on
	 * which it is dependent. This method is required by {@link Grammar#clone()}
	 * .
	 * 
	 * @return clone of this {@link Rule} but not of any {@link Rule Rules} on
	 *         which it is dependent
	 */
	public abstract Rule shallowClone();

	/**
	 * Returns a {@link Rule} that duplicates this except that it won't return
	 * any {@link Match} that doesn't meet the given condition. This method must
	 * be overridden by child classes as the base implementation only throws a
	 * {@link GrammarException} when called.
	 * 
	 * @param c
	 * @return a {@link Rule} that duplicates this except that it won't return
	 *         any {@link Match} that doesn't meet the given condition
	 */
	public Rule conditionalize(Condition c, String id) {
		throw new GrammarException(this.getClass()
				+ " cannot be conditionalized");
	}

	/**
	 * Method required by {@link Grammar#defineRule(String, Rule)}. It should
	 * not be used otherwise.
	 * 
	 * @param s
	 */
	void setLabel(String s) {
		label = new Label(label.t, s);
	}

	public Rule reLabel(String s) {
		throw new GrammarException(this.getClass() + " ");
	}

	/**
	 * Returns a {@link Rule} that matches the mirror image of the strings
	 * matched by this. If this {@link Rule} matches <code>ab+</code>, its
	 * reverse will match <code>b+a</code>. If it matches <code>abc</code>, it's
	 * reverse will match <code>cba</code>.
	 * <p>
	 * Reversed rules are necessary for variable length backwards look-behind
	 * assertions. They need not, and should not, implement studying or make any
	 * use of the offset cache as backwards assertions are not studied and the
	 * results of matching their constituent components are not cached.
	 * <p>
	 * This method must be overridden by child classes which might be reversed
	 * as the base implementation only throws a {@link GrammarException} when
	 * called.
	 * 
	 * @return a {@link Rule} that duplicates this except that it won't return
	 *         any {@link Match} that doesn't meet the given condition
	 */
	public Rule reverse() {
		throw new GrammarException(this.getClass() + " cannot be reversed");
	}

	/**
	 * Sets index of cache for this rule in offset matching cache.
	 * 
	 * @param uids
	 *            a map from unique ids to indices
	 */
	protected void setCacheIndex(Map<String, Integer> uids) {
		if (cacheIndex == -1) {
			Integer i = uids.get(uid());
			if (i == null) {
				i = uids.size();
				uids.put(uid(), i);
			}
			cacheIndex = i;
		}
	}

	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
		if (visited.contains(this))
			return currentMax;
		visited.add(this);
		return Math.max(cacheIndex, currentMax);
	}

	protected void rules(Map<String, Rule> map) {
		if (!map.containsKey(uid()))
			map.put(uid(), this);
	}

	/**
	 * Add to the label set any additional tags associated with all instances of
	 * this rule.
	 * 
	 * @param labels
	 */
	protected void addLabels(Set<String> labels) {
		if (this.labels != null)
			labels.addAll(this.labels);
	}

	/**
	 * Add to the labels of the child match any tags associated with the child
	 * rule only in this parent rule.
	 * 
	 * @param match
	 *            the match of a child rule of this rule
	 * @param labels
	 */
	protected void addLabels(Match match, Set<String> labels) {
	}

	/**
	 * Assign a set of alternate labels to the rule. This is only used during
	 * compilation.
	 * 
	 * @param labels
	 *            tags assigned by a named capture expression
	 */
	void setLabels(Set<String> labels) {
		this.labels = labels;
	}

	/**
	 * find {@link AlternationRule} rules and fix {@link AlternationRule#tagMap}
	 * so that there is mapping from {@link CyclicRule#r#uid()} to the proper
	 * tag set.
	 * <p>
	 * This really need only be overridden in non-terminal rules.
	 */
	protected void fixAlternationCycles() {
	}
}
