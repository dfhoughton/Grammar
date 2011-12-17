package dfh.grammar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * The object implementing rules such as
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; | &lt;c&gt;
 * </pre>
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class AlternationRule extends Rule implements IdentifyChild {
	private static final long serialVersionUID = 1L;

	private class AlternationMatcher extends NonterminalMatcher {
		int index = 0;
		Matcher mostRecent = null;

		public AlternationMatcher(CharSequence cs, Integer offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(cs, offset, cache, AlternationRule.this, master);
		}

		@Override
		protected void fetchNext() {
			if (mostRecent == null) {
				mostRecent = alternates[index].matcher(s, offset, cache, this);
			}
			Match child = null;
			OUTER: while (mostRecent.mightHaveNext()
					|| index < alternates.length) {
				while (mostRecent.mightHaveNext()) {
					child = mostRecent.match();
					if (child != null) {
						if (c == null || c.passes(child, this, s))
							break OUTER;
						else
							child = null;
					}
				}
				if (++index == alternates.length)
					break;
				mostRecent = alternates[index].matcher(s, offset, cache, this);
			}
			if (child == null) {
				done = true;
				next = null;
			} else {
				next = new Match(AlternationRule.this, offset, child.end());
				Match[] children = new Match[] { child };
				next.setChildren(children);
			}
		}
	}

	protected final Rule[] alternates;
	Map<String, Rule> tagMap;
	protected Condition c;

	/**
	 * Generates a rule from the given label and alternates.
	 * 
	 * @param label
	 * @param alternates
	 * @param tagMap
	 */
	public AlternationRule(Label label, Rule[] alternates,
			Map<String, Rule> tagMap) {
		super(label);
		this.alternates = alternates;
		this.tagMap = tagMap;
	}

	@Override
	public Matcher matcher(CharSequence cs, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new AlternationMatcher(cs, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		boolean nonInitial = false;
		for (Rule r : alternates) {
			if (nonInitial)
				b.append('|');
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
		for (Rule r : alternates) {
			if (nonInitial)
				b.append(" | ");
			else
				nonInitial = true;
			if (r.generation == -1) {
				b.append(r.description());
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
		Set<Integer> startOffsets = new HashSet<Integer>();
		for (Rule r : alternates)
			startOffsets.addAll(r.study(s, cache, studiedRules, options));
		return startOffsets;
	}

	@Override
	public boolean zeroWidth() {
		for (Rule r : alternates) {
			if (r.zeroWidth())
				return true;
		}
		return false;
	}

	@Override
	public Rule shallowClone() {
		AlternationRule ar = new AlternationRule((Label) label.clone(),
				Arrays.copyOf(alternates, alternates.length),
				new HashMap<String, Rule>(tagMap));
		return ar;
	}

	@Override
	public Rule conditionalize(Condition c, String id) {
		if (this.c == null) {
			this.c = c;
			this.condition = id;
		} else {
			if (this.c instanceof LogicalCondition) {
				if (!((LogicalCondition) this.c).replace(id, c))
					throw new GrammarException("could not defined " + id
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
	public boolean is(Match parent, Match child, String label) {
		Rule r = tagMap.get(label);
		if (r != null) {
			if (r instanceof CyclicRule)
				return ((CyclicRule) r).r == child.rule();
			return r == child.rule();
		}
		return false;
	}

	@Override
	public Set<String> labels(Match parent, Match child) {
		Set<Rule> rs = new HashSet<Rule>();
		for (Rule r : tagMap.values()) {
			if (r == child.rule())
				rs.add(r);
			else if (r instanceof CyclicRule) {
				if (((CyclicRule) r).r == child.rule())
					rs.add(r);
			}
		}
		Set<String> labels = new TreeSet<String>();
		for (Entry<String, Rule> e : tagMap.entrySet()) {
			if (rs.contains(e.getValue()))
				labels.add(e.getKey());
		}
		return labels;
	}

	@Override
	protected void setUid() {
		uid = uniqueId();
		for (Rule r : alternates)
			r.setUid();
	}
}
