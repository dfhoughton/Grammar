package dfh.grammar;

import java.util.ArrayList;
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
	private static final long serialVersionUID = 2L;
	final Rule[] sequence;
	private Condition c;
	final List<Set<String>> tagList;

	class SequenceMatcher extends NonterminalMatcher {
		LinkedList<Matcher> matchers = new LinkedList<Matcher>();
		LinkedList<Match> matched = new LinkedList<Match>();

		public SequenceMatcher(CharSequence cs, Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(cs, offset, cache, SequenceRule.this, master);
		}

		@Override
		protected void fetchNext() {
			try {
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
						Match n = m.mightHaveNext() ? m.match() : null;
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
								m = sequence[matched.size()].matcher(s,
										n.end(), cache, this);
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
			} catch (SingleColonBarrier s) {
				done = true;
				next = null;
			}

		}
	}

	/**
	 * Generates a {@link SequenceRule} with the given sequence.
	 * 
	 * @param l
	 * @param list
	 */
	public SequenceRule(Label l, List<Rule> list, List<Set<String>> tagList) {
		this(l, list.toArray(new Rule[list.size()]), tagList);
	}

	/**
	 * Generates a {@link SequenceRule} with the given sequence.
	 * 
	 * @param label
	 * @param sequence
	 * @param tagList
	 */
	public SequenceRule(Label label, Rule[] sequence, List<Set<String>> tagList) {
		super(label);
		this.sequence = sequence;
		this.tagList = tagList;
	}

	@Override
	public Matcher matcher(CharSequence cs, Integer offset,
			Map<Integer, CachedMatch>[] cache, Matcher master) {
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
	public String description(boolean inBrackets) {
		StringBuilder b = new StringBuilder();
		boolean nonInitial = false;
		int index = -1;
		for (Rule r : sequence) {
			index++;
			boolean hasTags = !tagList.get(index).isEmpty();
			if (nonInitial)
				b.append(' ');
			else
				nonInitial = true;
			if (hasTags) {
				if (!inBrackets)
					b.append('[');
				b.append('{');
				boolean ni2 = false;
				for (String label : tagList.get(index)) {
					if (ni2)
						b.append(',');
					else
						ni2 = true;
					b.append(label);
				}
				b.append("} ");
			}
			if (r.generation == -1) {
				boolean alternation = r instanceof AlternationRule;
				if (alternation)
					b.append("[ ");
				b.append(r.description(true));
				if (alternation)
					b.append(" ]");
			} else
				b.append(r.label());
			if (hasTags) {
				b.append(' ');
				if (!inBrackets)
					b.append(']');
			}
		}
		if (condition != null)
			b.append(" (").append(condition).append(')');
		return wrap(b);
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, Set<Rule> studiedRules,
			GlobalState options) {
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
				Arrays.copyOf(sequence, sequence.length),
				new ArrayList<Set<String>>(tagList));
		return sr;
	}

	@Override
	public Rule conditionalize(Condition c, String id) {
		if (this.c == null) {
			this.c = c;
			this.condition = id;
		} else {
			if (this.c instanceof LogicalCondition) {
				if (!((LogicalCondition) this.c).replace(id, c))
					throw new GrammarException("could not define " + id
							+ " in this condition");
			} else if (this.c instanceof LeafCondition) {
				LeafCondition lc = (LeafCondition) this.c;
				if (lc.cnd.equals(id))
					this.c = c;
				else
					throw new GrammarException("rule " + this
							+ " does not carry condition " + id);
			} else
				throw new GrammarException("condition on rule " + this
						+ " cannot be redefined");
		}
		return this;
	}

	@Override
	public void addLabels(Match match, Set<String> labels) {
		for (int i = 0; i < sequence.length; i++) {
			if (match.rule() == sequence[i]) {
				labels.addAll(tagList.get(i));
				break;
			}
		}
	}

	@Override
	protected void setUid() {
		if (uid == null) {
			uid = uniqueId();
			for (Rule r : sequence)
				r.setUid();
		}
	}

	@Override
	protected void setCacheIndex(Map<String, Integer> uids) {
		if (cacheIndex == -1) {
			Integer i = uids.get(uid());
			if (i == null) {
				i = uids.size();
				uids.put(uid(), i);
			}
			cacheIndex = i;
			for (Rule r : sequence)
				r.setCacheIndex(uids);
		}
	}

	@Override
	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
		if (visited.contains(this))
			return currentMax;
		visited.add(this);
		int max = Math.max(cacheIndex, currentMax);
		for (Rule r : sequence)
			max = Math.max(max, r.maxCacheIndex(max, visited));
		return max;
	}

	@Override
	protected void rules(Map<String, Rule> map) {
		if (!map.containsKey(uid())) {
			map.put(uid(), this);
			for (Rule r : sequence)
				r.rules(map);
		}
	}

	@Override
	protected void fixAlternation() {
		for (Rule r : sequence)
			r.fixAlternation();
	}
}
