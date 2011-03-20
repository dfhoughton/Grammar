package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dfh.grammar.Label.Type;

public class SequenceRule extends Rule {
	private static final long serialVersionUID = 1L;
	private final Rule[] sequence;

	class SequenceMatcher extends NonterminalMatcher {
		LinkedList<Matcher> matcherStack = new LinkedList<Matcher>();
		LinkedList<Match> matchStack = new LinkedList<Match>();

		public SequenceMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, Match>> cache) {
			super(cs, offset, parent, cache, label);
		}

		@Override
		protected void fetchNext() {
			if (matchStack.size() > 0) {
				while (!matchStack.isEmpty()) {
					matchStack.removeLast();
					if (matcherStack.peekLast().rule() instanceof LeafRule)
						matcherStack.removeLast();
					else
						break;
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
					m = sequence[0].matcher(cs, offset, next, cache);
					matcherStack.add(m);
				} else
					m = matcherStack.peekLast();
				Match n = m.match();
				if (n == null) {
					matcherStack.removeLast();
					if (!matchStack.isEmpty())
						matchStack.removeLast();
					if (matcherStack.isEmpty()) {
						done = true;
						next = null;
						if (!subCache.containsKey(offset))
							subCache.put(offset, null);
						break;
					}
				} else {
					matchStack.add(n);
					if (matchStack.size() < sequence.length) {
						m = sequence[matchStack.size()].matcher(cs, n.end(),
								next, cache);
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
		public Rule rule() {
			return SequenceRule.this;
		}
	}

	/**
	 * @param list
	 * @return label for synthetic node
	 */
	public static Label label(List<RuleFragment> list) {
		StringBuilder b = new StringBuilder();
		for (RuleFragment rf : list) {
			if (b.length() > 0)
				b.append(' ');
			b.append(rf.stringify());
		}
		return new Label(Type.nonTerminal, b.toString());
	}

	public SequenceRule(Label label, List<RuleFragment> list,
			Map<Label, Rule> rules) {
		super(label);
		sequence = new Rule[list.size()];
		int index = 0;
		for (RuleFragment rf : list) {
			RepeatableRuleFragment rrf = (RepeatableRuleFragment) rf;
			Rule r;
			if (rrf instanceof Label) {
				Label l = (Label) rrf;
				r = rules.get(l);
				if (r == null) {
					// undefined terminal
					r = new DeferredDefinitionRule(l, rules);
					rules.put(r.label(), r);
				}
			} else {
				GroupFragment gf = (GroupFragment) rrf;
				Label l1 = AlternationRule.label(gf);
				r = rules.get(l1);
				if (r == null) {
					r = new AlternationRule(l1, gf, rules);
					rules.put(l1, r);
					rules.put(r.label(), r);
				}
			}
			if (rrf.rep != Repetition.NONE) {
				Label l = new Label(Type.nonTerminal, rrf.stringify());
				RepetitionRule rr = new RepetitionRule(l, r, rrf.rep);
				rules.put(l, rr);
				r = rr;
			}
			sequence[index++] = r;
		}
	}

	@Override
	public Matcher matcher(CharSequence cs, int offset, Match parent,
			Map<Label, Map<Integer, Match>> cache) {
		return new SequenceMatcher(cs, offset, parent, cache);
	}

}
