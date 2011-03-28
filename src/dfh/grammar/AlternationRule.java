package dfh.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlternationRule extends Rule {
	private static final long serialVersionUID = 1L;

	private class AlternationMatcher extends NonterminalMatcher {
		int index = 0;
		Matcher mostRecent = null;

		public AlternationMatcher(CharSequence cs, int offset,
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
					if (child != null)
						break OUTER;
				}
				if (++index == alternates.length)
					break;
				mostRecent = alternates[index].matcher(s, offset, cache, this);
			}
			if (child == null) {
				done = true;
				next = null;
			} else {
				next = new Match(AlternationRule.this, offset);
				Match[] children = new Match[] { child };
				next.setChildren(children);
				next.setEnd(child.end());
			}
		}
	}

	final Rule[] alternates;

	public AlternationRule(Label label, Rule[] alternates) {
		super(label);
		this.alternates = alternates;
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
				if (r instanceof SequenceRule) {
					b.append("[ ");
					b.append(r.description());
					b.append(" ]");
				} else
					b.append(r.description());
			} else
				b.append(r.label());
		}
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache, int offset,
			Set<Rule> studiedRules) {
		studiedRules.add(this);
		Set<Integer> startOffsets = new HashSet<Integer>();
		studiedRules.add(this);
		for (Rule r : alternates)
			if (!studiedRules.contains(r))
				startOffsets.addAll(r.study(s, cache, offset, studiedRules));
		return startOffsets;
	}
}
