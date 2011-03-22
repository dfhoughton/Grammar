package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dfh.grammar.Label.Type;

public class SequenceRule extends Rule {
	private static final long serialVersionUID = 1L;
	final Rule[] sequence;

	class SequenceMatcher extends NonterminalMatcher {
		LinkedList<Matcher> matcherStack = new LinkedList<Matcher>();
		LinkedList<Match> matchStack = new LinkedList<Match>();

		public SequenceMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, Match>> cache) {
			super(cs, offset, parent, cache, SequenceRule.this.label);
		}

		@Override
		protected void fetchNext() {
			if (matchStack.size() > 0) {
				while (!matchStack.isEmpty()) {
					matchStack.removeLast();
					if (matcherStack.peekLast().mightHaveNext())
						break;
					else
						matcherStack.removeLast();
				}
				if (matcherStack.isEmpty()) {
					next = null;
					done = true;
					return;
				}
			}
			next = new Match(SequenceRule.this, offset, parent);
			while (matchStack.size() < sequence.length) {
				Matcher m;
				if (matcherStack.isEmpty()) {
					m = sequence[0].matcher(cs, offset, next, cache, this);
					matcherStack.add(m);
				} else
					m = matcherStack.peekLast();
				Match n = m.match();
				if (n == null) {
					System.err.println("fn " + identify()
							+ " removing last matcher");
					matcherStack.removeLast();
					if (!matchStack.isEmpty()) {
						System.err.println("fn " + identify()
								+ " removing last match");
						matchStack.removeLast();
					}
					if (matcherStack.isEmpty()) {
						System.err.println("fn " + identify()
								+ " could not find a match");
						done = true;
						next = null;
						// redundant, I believe
						// if (!subCache.containsKey(offset))
						// subCache.put(offset, null);
						break;
					}
				} else {
					System.err.println("fn " + identify() + " adding "
							+ cs.subSequence(n.start(), n.end()) + " (" + n
							+ ")");
					matchStack.add(n);
					if (matchStack.size() < sequence.length) {
						m = sequence[matchStack.size()].matcher(cs, n.end(),
								next, cache, this);
						System.err.println("fn " + identify()
								+ " adding matcher " + m);
						matcherStack.add(m);
					}
				}
			}
			if (next != null) {
				next.setEnd(matchStack.peekLast().end());
				Match[] children = matchStack
						.toArray(new Match[sequence.length]);
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
			if (matchStack != null) {
				nonInitial = false;
				b.append('[');
				for (Match m : matchStack) {
					if (nonInitial)
						b.append(", ");
					else
						nonInitial = true;
					b.append(cs.subSequence(m.start(), m.end()));
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
	public Matcher matcher(CharSequence cs, int offset, Match parent,
			Map<Label, Map<Integer, Match>> cache, Matcher master) {
		return new SequenceMatcher(cs, offset, parent, cache);
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

}
