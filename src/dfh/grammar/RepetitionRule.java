package dfh.grammar;

import java.util.LinkedList;
import java.util.Map;

/**
 * Rule to handle all the various repetition options.
 * <p>
 * <b>Creation date:</b> Mar 16, 2011
 * 
 * @author David Houghton
 * 
 */
public class RepetitionRule extends Rule {
	private static final long serialVersionUID = 1L;
	private final Rule r;
	private final Repetition repetition;

	abstract class GreedyAndPossessive extends NonterminalMatcher {

		protected LinkedList<Node> matched;
		protected LinkedList<Matcher> matchers;
		private final boolean backtracks;

		protected GreedyAndPossessive(CharSequence cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache, Label label,
				boolean backtracks) {
			super(cs, offset, parent, cache, label);
			this.backtracks = backtracks;
		}

		protected void initialize() {
			matched = new LinkedList<Node>();
			matchers = new LinkedList<Matcher>();
			while (matched.size() < repetition.top) {
				if (grab())
					break;
			}
			if (matched.size() < repetition.bottom) {
				matched.clear();
				if (backtracks) {
					matchers.clear();
					matchers = null;
				}
			} else if (!backtracks) {
				matchers.clear();
				matchers = null;
			}
		}

		protected boolean grab() {
			int start = matched.isEmpty() ? offset : matched.peekLast().end();
			Matcher m;
			if (matchers.size() > matched.size())
				m = matchers.peekLast();
			else {
				m = r.matcher(cs, start, parent, cache);
				matchers.add(m);
			}
			Node n = m.match();
			if (n == null) {
				matchers.removeLast();
				return true;
			} else {
				matched.add(n);
				return false;
			}
		}

		@Override
		public Rule rule() {
			return RepetitionRule.this;
		}
	}

	/**
	 * <b>Creation date:</b> Mar 17, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	class GreedyMatcher extends GreedyAndPossessive {
		protected GreedyMatcher(CharSequence cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache, Label label) {
			super(cs, offset, parent, cache, label, true);
		}

		@Override
		protected void fetchNext() {
			if (matched == null)
				initialize();
			else {
				matched.remove();
				// see if we can find some other way forward
				while (!matchers.isEmpty() && matched.size() < repetition.top) {
					if (grab())
						break;
				}
				if (matched.size() < repetition.bottom) {
					matched.clear();
					matchers.clear();
					matchers = null;
				}
			}
			if (matched.isEmpty()) {
				next = null;
				done = true;
			} else {
				Node[] children = matched.toArray(new Node[matched.size()]);
				next = new Node(RepetitionRule.this, offset, parent);
				next.setChildren(children);
				next.setEnd(matched.peek().end());
			}
		}

	}

	private class StingyMatcher extends NonterminalMatcher {
		private LinkedList<Matcher> matchers = new LinkedList<Matcher>();
		private LinkedList<Node> nodes = new LinkedList<Node>();

		protected StingyMatcher(CharSequence cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache, Label label) {
			super(cs, offset, parent, cache, label);
			matchers.add(r.matcher(cs, offset, parent, cache));
		}

		@Override
		protected void fetchNext() {
			next = new Node(RepetitionRule.this, offset, parent);
			while (next != null || nodes.size() < repetition.bottom) {
				Matcher m = matchers.peekLast();
				if (m.mightHaveNext()) {
					Node n = m.match();
					if (n == null) {
						decrement();
					} else {
						if (nodes.size() == repetition.top)
							nodes.removeLast();
						nodes.add(n);
						if (nodes.size() < repetition.top)
							matchers.add(r.matcher(cs, n.end(), next, cache));
					}
				} else {
					decrement();
				}
			}
		}

		private void decrement() {
			matchers.removeLast();
			if (matchers.isEmpty()) {
				done = true;
				next = null;
				matchers = null;
				nodes = null;
			} else {
				nodes.removeLast();
			}
		}

		@Override
		public Rule rule() {
			return RepetitionRule.this;
		}

	}

	private class PossessiveMatcher extends GreedyAndPossessive {

		protected PossessiveMatcher(CharSequence cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache, Label label) {
			super(cs, offset, parent, cache, label, false);
		}

		@Override
		protected void fetchNext() {
			if (matched == null) {
				initialize();
				if (matched.isEmpty()) {
					done = true;
					matched = null;
					next = null;
				} else {
					next = new Node(RepetitionRule.this, offset, parent);
					Node[] children = matched.toArray(new Node[matched.size()]);
					next.setChildren(children);
					next.setEnd(matched.peek().end());
				}
			} else {
				done = true;
				next = null;
				matched = null;
			}
		}

	}

	public RepetitionRule(Label label, Rule r, Repetition rep) {
		super(label);
		this.r = r;
		this.repetition = rep;
	}

	@Override
	public Matcher matcher(CharSequence cs, int offset, Node parent,
			Map<Label, Map<Integer, Node>> cache) {
		switch (repetition.t) {
		case possessive:
			return new PossessiveMatcher(cs, offset, parent, cache, label);
		case stingy:
			return new StingyMatcher(cs, offset, parent, cache, label);
		default:
			return new GreedyMatcher(cs, offset, parent, cache, label);
		}
	}

}
