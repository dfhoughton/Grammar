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

	private abstract class GreedyAndPossessive extends NonterminalMatcher {

		protected LinkedList<Node> matched;

		protected GreedyAndPossessive(char[] cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache, Label label) {
			super(cs, offset, parent, cache, label);
		}

		protected void initialize() {
			matched = new LinkedList<Node>();
			int start = offset;
			while (matched.size() < repetition.top) {
				Matcher m = r.matcher(cs, start, parent, cache);
				Node n = m.match();
				if (n == null)
					break;
				else {
					start = n.end();
					matched.add(n);
				}
			}
			if (matched.size() < repetition.bottom)
				matched.clear();
		}

	}

	/**
	 * TODO: make this iterate over the matches of sub-rules
	 * <p>
	 * <b>Creation date:</b> Mar 17, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	class GreedyMatcher extends GreedyAndPossessive {
		protected GreedyMatcher(char[] cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache, Label label) {
			super(cs, offset, parent, cache, label);
		}

		@Override
		protected void fetchNext() {
			if (matched == null)
				initialize();
			else
				matched.remove();
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

		protected StingyMatcher(char[] cs, int offset, Node parent,
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

	}

	private class PossessiveMatcher extends GreedyAndPossessive {

		protected PossessiveMatcher(char[] cs, int offset, Node parent,
				Map<Label, Map<Integer, Node>> cache, Label label) {
			super(cs, offset, parent, cache, label);
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
	public Matcher matcher(char[] cs, int offset, Node parent,
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
