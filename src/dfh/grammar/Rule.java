package dfh.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
public abstract class Rule {

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
	protected Set<String> labels;
	protected boolean mayBeZeroWidth = true;
	/**
	 * A reference to the rule from which this rule was generated if the rule is
	 * reversed.
	 */
	Rule unreversed;

	protected String wrap(StringBuilder b) {
		if (!(labels == null || labels.isEmpty())) {
			String s = b.toString();
			b = new StringBuilder("[{");
			boolean nonInitial = false;
			for (String label : labels) {
				if (nonInitial)
					b.append(',');
				else
					nonInitial = true;
				b.append(label);
			}
			b.append("} ");
			b.append(s).append(" ]");
		}
		return b.toString();
	}

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
	public abstract Matcher matcher(Integer offset,
			Map<Integer, CachedMatch>[] cache, Matcher master);

	@Override
	public String toString() {
		return label.id;
	}

	public String uid() {
		return uid;
	}

	protected void setUid() {
		if (uid == null) {
			uid = uniqueId();
			if (label.id.endsWith(Assertion.REVERSAL_SUFFIX))
				uid += Assertion.REVERSAL_SUFFIX;
		}
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
				b.append(" => ");
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
			CharSequence ss = s.subSequence(offset, end);
			b.append(ss);
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
	 * @param b
	 *            whether this description already resides in brackets
	 * @return a String describing the rule
	 */
	public abstract String description(boolean b);

	/**
	 * The rule description is invoked by {@link Grammar#describe()}. It is the
	 * text that appears after the '=' in a rule definition.
	 * 
	 * @return a String describing the rule
	 */
	public String description() {
		return description(false);
	}

	/**
	 * Prepare for matching against the given {@link CharSequence}. This is an
	 * optimization to allow terminal rules to cache matches and allow the root
	 * {@link Matcher} to skip impossible matching offsets.
	 * 
	 * @param s
	 *            {@link CharSequence} to be matched against
	 * @param cache
	 *            match cache that will be used
	 * @param options
	 *            {@link GlobalState} in use in this match
	 * @return set of start offsets of matches
	 */
	public abstract Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options);

	/**
	 * For recursively searching the rules for ones that might begin a match.
	 * 
	 * @param initialRules
	 */
	protected void initialRules(Set<String> initialRules) {
		initialRules.add(uid());
	}

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
	 * Returns a {@link Rule} that duplicates this except that it won't return
	 * any {@link Match} that doesn't meet the given condition. This method must
	 * be overridden by child classes as the base implementation only throws a
	 * {@link GrammarException} when called.
	 * 
	 * @param c
	 * @param id
	 *            condition identifier
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

	public Rule reLabel(String s) { // TODO needs documentation
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
	 * @param id
	 *            label to be used by reversed version of rule
	 * 
	 * @return a {@link Rule} that duplicates this except that it won't return
	 *         any {@link Match} that doesn't meet the given condition
	 */
	public Rule reverse(String id) {
		throw new GrammarException(this.getClass() + " cannot be reversed");
	}

	/**
	 * Sets index of cache for this rule in offset matching cache. This method
	 * must be overridden in non-terminal rules to ensure all rules set their
	 * cache index. See, for example {@link AlternationRule#setCacheIndex(Map)}.
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
	 * so that there is mapping from {@link CyclicRule#r#uid()} or
	 * {@link DeferredDefinitionRule#r#uid()}to the proper tag set.
	 * <p>
	 * This really need only be overridden in non-terminal rules.
	 */
	protected void fixAlternation() {
	}

	protected void logCondition(Matcher m, boolean passes) {
		StringBuilder b = new StringBuilder();
		b.append("\ncondition ").append(condition).append(' ');
		b.append(passes ? "passes" : "does not pass");
		b.append('\n');
		m.options.trace.println(b);
	}

	/**
	 * Walks tree from this rule collecting all rules reachable from this. This
	 * must be overridden by non-terminal rules.
	 * 
	 * @param set
	 *            collector of rules
	 * @param explicit
	 *            whether to list only rules that were explicitly labeled, as
	 *            opposed to those implicit in a larger rule definition
	 */
	protected void subRules(Set<Rule> set, Set<Rule> all, boolean explicit) {
		if (!all.contains(this)) {
			all.add(this);
			if (explicit) {
				if (generation > -1)
					set.add(this);
				if (unreversed != null)
					unreversed.subRules(set, all, explicit);
			} else
				set.add(this);
		}
	}

	/**
	 * Returns whether this rule depends on the given for its matching. This
	 * method is not optimized. It is meant to be used during grammar
	 * composition, not matching.
	 * 
	 * @param r
	 * @return whether this rule depends on r for its matching
	 */
	public boolean dependsOn(Rule r) {
		Set<Rule> set = new HashSet<Rule>();
		subRules(set, new HashSet<Rule>(), false);
		return set.contains(r);
	}

	/**
	 * For recording redundant labels during compilation.
	 * 
	 * @param l2
	 */
	protected void addLabel(Label l2) {
		if (labels == null) {
			labels = new TreeSet<String>();
		}
		labels.add(l2.id);
	}

	/**
	 * Returns whether this {@link Rule} will be tested against a
	 * {@link ReversedCharSequence}.
	 * 
	 * @return whether this {@link Rule} will be tested against a
	 *         {@link ReversedCharSequence}
	 */
	public boolean isReversed() {
		return uid().endsWith(Assertion.REVERSAL_SUFFIX);
	}

	/**
	 * Used to calculate start offsets when studying. Used to set
	 * {@link #mayBeZeroWidth} required by
	 * {@link SequenceRule#initialRules(Set)}.
	 * 
	 * @param cache
	 *            for keeping track of visited rules and caching the results of
	 *            the visits; prevents infinite recursive loops
	 * @return whether the rule could ever return a zero-width match
	 */
	protected abstract Boolean mayBeZeroWidth(Map<String, Boolean> cache);

	public abstract Rule deepCopy(String nameBase, Map<String, Rule> cycleMap);

	/**
	 * Returns the names of conditions associated with this rule. This base
	 * implementation should be overridden in rules that may carry conditions.
	 * 
	 * @return the names of conditions associated with this rule
	 */
	public Set<String> conditionNames() {
		return new HashSet<String>(0);
	}

	/**
	 * Returns whether this rule necessarily produces terminal matches. This
	 * method should be overridden by classes such as {@link LiteralRule}. It is
	 * required by {@link Match#isTerminal()}.
	 * 
	 * @return whether this rule necessarily produces terminal matches
	 */
	protected boolean isTerminal() {
		return false;
	}
}
