package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SequenceRule extends Rule {
	private static final long serialVersionUID = 1L;
	final Rule[] sequence;

	class SequenceMatcher extends NonterminalMatcher {
		LinkedList<Matcher> matchers = new LinkedList<Matcher>();
		LinkedList<Match> matched = new LinkedList<Match>();

		public SequenceMatcher(CharSequence cs, Integer offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(cs, offset, parent, cache, SequenceRule.this, master);
		}

		@Override
		protected void fetchNext() {
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
			next = new Match(SequenceRule.this, offset, parent);
			while (matched.size() < sequence.length) {
				Matcher m;
				if (matchers.isEmpty()) {
					m = sequence[0].matcher(s, offset, next, cache, this);
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
						next = null;
						break;
					}
				} else {
					matched.add(n);
					if (matched.size() < sequence.length) {
						m = sequence[matched.size()].matcher(s, n.end(), next,
								cache, this);
						matchers.add(m);
					}
				}
			}
			if (next != null) {
				next.setEnd(matched.peekLast().end());
				Match[] children = matched.toArray(new Match[sequence.length]);
				next.setChildren(children);
			}
		}

		@Override
		public String identify() {
			StringBuilder b = new StringBuilder(label.id);
			boolean nonInitial = false;
			b.append("{{");
			for (Rule r : sequence) {
				if (nonInitial)
					b.append(' ');
				else
					nonInitial = true;
				b.append(r);
			}
			b.append("}}");
			if (matched != null) {
				nonInitial = false;
				b.append('[');
				for (Match m : matched) {
					if (nonInitial)
						b.append(", ");
					else
						nonInitial = true;
					b.append(s.subSequence(m.start(), m.end()));
				}
				b.append(']');
			}
			return b.toString();
		}
	}

	public SequenceRule(Label l, List<Rule> list) {
		this(l, list.toArray(new Rule[list.size()]));
	}

	public SequenceRule(Label label, Rule[] sequence) {
		super(label);
		this.sequence = sequence;
	}

	@Override
	public Matcher matcher(CharSequence cs, Integer offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new SequenceMatcher(cs, offset, parent, cache, master);
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
			if (r.generation == -1)
				b.append(r.description());
			else
				b.append(r.label());
		}
		return b.toString();
	}

}
