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
		LinkedList<Node> nodeStack = new LinkedList<Node>();

		public SequenceMatcher(CharSequence cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache) {
			super(cs, offset, parent, cache, label);
		}

		@Override
		protected void fetchNext() {
			if (nodeStack.size() > 0) {
				while (!nodeStack.isEmpty()) {
					nodeStack.removeLast();
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
			next = new Node(SequenceRule.this, offset, parent);
			while (nodeStack.size() < sequence.length) {
				Matcher m;
				if (matcherStack.isEmpty()) {
					m = sequence[0].matcher(cs, offset, next, cache);
					matcherStack.add(m);
				} else
					m = matcherStack.peekLast();
				Node n = m.match();
				if (n == null) {
					matcherStack.removeLast();
					if (!nodeStack.isEmpty())
						nodeStack.removeLast();
					if (matcherStack.isEmpty()) {
						done = true;
						next = null;
						if (!subCache.containsKey(offset))
							subCache.put(offset, null);
						break;
					}
				} else {
					nodeStack.add(n);
					if (nodeStack.size() < sequence.length) {
						m = sequence[nodeStack.size()].matcher(cs, n.end(),
								next, cache);
						matcherStack.add(m);
					}
				}
			}
			if (next != null) {
				next.setEnd(nodeStack.peekLast().end());
				Node[] children = nodeStack.toArray(new Node[sequence.length]);
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
	public Matcher matcher(CharSequence cs, int offset, Node parent,
			Map<Label, Map<Integer, Node>> cache) {
		return new SequenceMatcher(cs, offset, parent, cache);
	}

}
