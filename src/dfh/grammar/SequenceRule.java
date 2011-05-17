package dfh.grammar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Matches a sequence of sub-rules. E.g.,
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; &lt;c&gt;
 * </pre>
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class SequenceRule extends Rule {
	private static final long serialVersionUID = 1L;
	final Rule[] sequence;
	private Condition c;

	class SequenceMatcher extends NonterminalMatcher {
		LinkedList<Matcher> matchers = new LinkedList<Matcher>();
		LinkedList<Match> matched = new LinkedList<Match>();

		public SequenceMatcher(CharSequence cs, Integer offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(cs, offset, cache, SequenceRule.this, master);
		}

		@Override
		protected void fetchNext() {
			while (true) {
				if (matched.size() > 0) {
					while (!matched.isEmpty()) {
						matched.removeLast();
						if (matchers.peekLast().mightHaveNext())
							break;
						else
							matchers.removeLast();
					}
					if (matchers.isEmpty()) {
						next = null;
						done = true;
						return;
					}
				}
				next = null;
				boolean found = true;
				while (matched.size() < sequence.length) {
					Matcher m;
					if (matchers.isEmpty()) {
						m = sequence[0].matcher(s, offset, cache, this);
						matchers.add(m);
					} else
						m = matchers.peekLast();
					Match n = null;
					try {
						n = m.mightHaveNext() ? m.match() : null;
					} catch (SingleColonBarrier s) {
						done = true;
						found = false;
						break;
					}
					if (n == null) {
						matchers.removeLast();
						if (!matched.isEmpty()) {
							matched.removeLast();
						}
						if (matchers.isEmpty()) {
							done = true;
							found = false;
							break;
						}
					} else {
						matched.add(n);
						if (matched.size() < sequence.length) {
							m = sequence[matched.size()].matcher(s, n.end(),
									cache, this);
							matchers.add(m);
						}
					}
				}
				if (found) {
					next = new Match(SequenceRule.this, offset, matched
							.peekLast().end());
					Match[] children = matched
							.toArray(new Match[sequence.length]);
					next.setChildren(children);
					if (c == null || c.passes(next, this, s))
						break;
				} else
					break;
			}
		}
	}

	/**
	 * Generates a {@link SequenceRule} with the given sequence.
	 * 
	 * @param l
	 * @param list
	 */
	public SequenceRule(Label l, List<Rule> list) {
		this(l, list.toArray(new Rule[list.size()]));
	}

	/**
	 * Generates a {@link SequenceRule} with the given sequence.
	 * 
	 * @param label
	 * @param sequence
	 */
	public SequenceRule(Label label, Rule[] sequence) {
		super(label);
		this.sequence = sequence;
	}

	@Override
	public Matcher matcher(CharSequence cs, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new SequenceMatcher(cs, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		boolean nonInitial = false;
		for (Rule r : sequence) {
			if (nonInitial)
				b.append(' ');
			else
				nonInitial = true;
			b.append(r.uniqueId());
		}
		b.append(']');
		if (condition != null)
			b.append('(').append(condition).append(')');
		return b.toString();
	}

	@Override
	public String description() {
		StringBuilder b = new StringBuilder();
		boolean nonInitial = false;
		for (Rule r : sequence) {
			if (nonInitial)
				b.append(' ');
			else
				nonInitial = true;
			if (r.generation == -1) {
				boolean alternation = r instanceof AlternationRule;
				if (alternation)
					b.append("[ ");
				b.append(r.description());
				if (alternation)
					b.append(" ]");
			} else
				b.append(r.label());
		}
		if (condition != null)
			b.append(" (").append(condition).append(')');
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache,
			Set<Rule> studiedRules, GlobalState options) {
		Set<Integer> startOffsets = null;
		boolean foundStarts = false;
		for (Rule r : sequence) {
			if (foundStarts) {
				r.study(s, cache, studiedRules, options);
				continue;
			}
			Set<Integer> set = r.study(s, cache, studiedRules, options);
			if (startOffsets == null)
				startOffsets = new HashSet<Integer>(set);
			else
				startOffsets.addAll(set);
			if (!r.zeroWidth())
				foundStarts = true;
		}
		return startOffsets;
	}

	@Override
	public boolean zeroWidth() {
		for (Rule r : sequence) {
			if (!r.zeroWidth())
				return false;
		}
		return true;
	}

	@Override
	public Rule shallowClone() {
		SequenceRule sr = new SequenceRule((Label) label.clone(),
				Arrays.copyOf(sequence, sequence.length));
		return sr;
	}

	@Override
	public Rule conditionalize(Condition c, String id) {
		this.c = c;
		this.condition = id;
		return this;
	}
}
