package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.text.Segment;

public class SequenceRule extends Rule {
	private static final long serialVersionUID = 1L;
	private final Rule[] sequence;

	private class SequenceMatcher implements Matcher {
		private final Segment segment;
		private final int offset;
		private final Node parent;
		private final char[] cs;
		Queue<Matcher> matcherStack = new LinkedList<Matcher>();
		Queue<Node> nodeStack = new LinkedList<Node>();
		private Node next;
		private boolean done = false;
		private final Map<Label, Map<Integer, Node>> cache;
		private final Map<Integer, Node> subcache;

		public SequenceMatcher(char[] cs, int offset, Node parent, Map<Label, Map<Integer, Node>> cache) {
			this.cs = cs;
			this.segment = new Segment(cs, offset, cs.length - offset);
			this.offset = offset;
			this.parent = parent;
			this.cache = cache;
			this.subcache = cache.get(label);
		}

		@Override
		public Node match() {
			boolean alreadyMatched = subcache.containsKey(offset);
			if (alreadyMatched && subcache.get(offset) == null)
				return null;
			if (next == null)
				fetchNext();
			if (!alreadyMatched)
				subcache.put(offset, next == null ? null : Node.dummy);
			Node n = next;
			next = null;
			return n;
		}

		@Override
		public boolean hasNext() {
			if (done)
				return false;
			if (next == null)
				fetchNext();
			return next != null;
		}

		private void fetchNext() {
			if (nodeStack.size() == 0)
				fetchFirst();
			else {
				nodeStack.remove();
				while (nodeStack.size() < sequence.length) {
					Matcher m = matcherStack.peek();
					if (m.hasNext()) {
						m.iterate();
						nodeStack.add(m.match());
					}
				}
			}
		}

		private void fetchFirst() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void iterate() throws GrammarException {
			if (!done)
				fetchNext();
		}

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
				}
			} else {
				GroupFragment gf = (GroupFragment) rrf;
				Label l1 = AlternationRule.label(gf);
				r = rules.get(l1);
				if (r == null) {
					r = new AlternationRule(l1, gf, rules);
					rules.put(l1, r);
				}
			}
			rules.put(r.label(), r);
			if (rrf.rep != Repetition.NONE) {
				Label l = RepetitionRule.label(r, rrf.rep);
				RepetitionRule rr = new RepetitionRule(l, r);
				rules.put(l, rr);
				r = rr;
			}
			sequence[index++] = r;
		}
	}

	@Override
	public Matcher matcher(char[] cs, int offset, Node parent,Map<Label, Map<Integer, Node>> cache) {
		// TODO Auto-generated method stub
		return new SequenceMatcher(cs, offset, parent,cache);
	}

}
