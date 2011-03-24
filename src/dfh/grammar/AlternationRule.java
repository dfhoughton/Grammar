package dfh.grammar;

import java.util.Map;

public class AlternationRule extends Rule {
	private static final long serialVersionUID = 1L;

	private class AlternationMatcher extends NonterminalMatcher {
		int index = 0;
		Matcher mostRecent = null;

		public AlternationMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(cs, offset, parent, cache, AlternationRule.this.label);
		}

		@Override
		protected void fetchNext() {
			if (mostRecent == null) {
				mostRecent = alternates[index].matcher(cs, offset, parent,
						cache, this);
			}
			Match child = null;
			OUTER: while (mostRecent.mightHaveNext()
					|| ++index < alternates.length) {
				while (mostRecent.mightHaveNext()) {
					child = mostRecent.match();
					if (child != null)
						break OUTER;
				}
				mostRecent = alternates[index].matcher(cs, offset, parent,
						cache, this);
			}
			if (child == null) {
				done = true;
				next = null;
			} else {
				next = new Match(AlternationRule.this, offset, parent);
				Match[] children = new Match[] { child };
				next.setChildren(children);
				next.setEnd(child.end());
			}
		}

		@Override
		public String identify() {
			return label.id + ":" + mostRecent;
		}
	}

	final Rule[] alternates;

	public AlternationRule(Label label, Rule[] alternates) {
		super(label);
		this.alternates = alternates;
	}

	@Override
	public Matcher matcher(CharSequence cs, int offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new AlternationMatcher(cs, offset, parent, cache, master);
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
}
