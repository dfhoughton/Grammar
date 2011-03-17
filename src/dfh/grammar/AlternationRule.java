package dfh.grammar;

import java.util.List;
import java.util.Map;

import dfh.grammar.Label.Type;

public class AlternationRule extends Rule {
	private static final long serialVersionUID = 1L;

	private class AlternationMatcher extends NonterminalMatcher {
		int index = 0;
		Matcher mostRecent = null;

		public AlternationMatcher(char[] cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache) {
			super(cs, offset, parent, cache, label);
		}

		@Override
		protected void fetchNext() {
			if (mostRecent == null)
				mostRecent = alternates[index++].matcher(cs, index, parent,
						cache);
			Node child = null;
			OUTER: while (mostRecent.mightHaveNext()
					|| index < alternates.length) {
				while (mostRecent.mightHaveNext()) {
					child = mostRecent.match();
					if (child != null)
						break OUTER;
				}
				mostRecent = alternates[index++].matcher(cs, index, parent,
						cache);
			}
			if (child == null) {
				done = true;
				next = null;
			} else {
				next = new Node(AlternationRule.this, offset, parent);
				Node[] children = new Node[] { child };
				next.setChildren(children);
				next.setEnd(child.end());
			}
		}
	}

	private final Rule[] alternates;

	public AlternationRule(Label l, GroupFragment gf, Map<Label, Rule> rules) {
		super(l);
		alternates = new Rule[gf.alternates.size()];
		int index = 0;
		for (List<RuleFragment> alternate : gf.alternates) {
			Rule r = makeRule(alternate, rules);
			alternates[index++] = r;
		}
	}

	private Rule makeRule(List<RuleFragment> alternate, Map<Label, Rule> rules) {
		if (alternate.size() == 1) {
			RepeatableRuleFragment rf = (RepeatableRuleFragment) alternate
					.get(0);
			Rule r;
			if (rf instanceof Label) {
				r = rules.get((Label) rf);
			} else {
				GroupFragment gf = (GroupFragment) rf;
				if (gf.alternates.size() == 1) {
					Label l = SequenceRule.label(gf.alternates.get(0));
					r = rules.get(l);
					if (r == null) {
						r = new SequenceRule(l, gf.alternates.get(0), rules);
						rules.put(l, r);
					}
				} else {
					GroupFragment gf2 = gf.noRep();
					Label l = AlternationRule.label(gf2);
					r = rules.get(l);
					if (r == null) {
						r = new AlternationRule(l, gf2, rules);
						rules.put(l, r);
					}
				}
			}
			if (rf.rep.bottom == 1 & rf.rep.top == 1)
				return r;
			Label l = new Label(Type.nonTerminal, rf.stringify());
			Rule rr = rules.get(l);
			if (rr == null) {
				rr = new RepetitionRule(l, r, rf.rep);
				rules.put(l, rr);
			}
			return rr;
		} else {
			Label l = SequenceRule.label(alternate);
			Rule r = rules.get(l);
			if (r == null) {
				r = new SequenceRule(l, alternate, rules);
				rules.put(l, r);
			}
			return r;
		}
	}

	public static Label label(GroupFragment rrf) {
		return new Label(Type.nonTerminal, rrf.stringify());
	}

	@Override
	public Matcher matcher(char[] cs, int offset, Node parent,
			Map<Label, Map<Integer, Node>> cache) {
		return new AlternationMatcher(cs, offset, parent, cache);
	}
}
