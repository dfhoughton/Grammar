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

		protected LinkedList<Match> matched;
		protected LinkedList<Matcher> matchers;
		private final boolean backtracks;

		protected GreedyAndPossessive(CharSequence cs, int offset,
				Match parent, Map<Label, Map<Integer, Match>> cache,
				Label label, boolean backtracks) {
			super(cs, offset, parent, cache, label);
			this.backtracks = backtracks;
		}

		protected void initialize() {
			matched = new LinkedList<Match>();
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
			Match n = m.match();
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
		protected GreedyMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, Match>> cache, Label label) {
			super(cs, offset, parent, cache, label, true);
		}

		@Override
		protected void fetchNext() {
			if (matched == null)
				initialize();
			else {
				matched.removeLast();
				// see if we can find some other way forward
				if (matchers.peekLast().mightHaveNext()) {
					while (!matchers.isEmpty()
							&& matched.size() < repetition.top) {
						if (grab())
							break;
					}
				}
			}
			if (matched.size() < repetition.bottom) {
				matched.clear();
				matchers.clear();
				matchers = null;
				next = null;
				done = true;
			} else {
				Match[] children = matched.toArray(new Match[matched.size()]);
				next = new Match(RepetitionRule.this, offset, parent);
				next.setChildren(children);
				next.setEnd(matched.peekLast().end());
			}
		}

	}

	private class StingyMatcher extends NonterminalMatcher {
		private LinkedList<Matcher> matchers = new LinkedList<Matcher>();
		private LinkedList<Match> matches = new LinkedList<Match>();

		protected StingyMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, Match>> cache, Label label) {
			super(cs, offset, parent, cache, label);
			matchers.add(r.matcher(cs, offset, parent, cache));
		}

		@Override
		protected void fetchNext() {
			next = new Match(RepetitionRule.this, offset, parent);
			while (next != null && matches.size() < repetition.bottom) {
				Matcher m = matchers.peekLast();
				if (m.mightHaveNext()) {
					Match n = m.match();
					if (n == null) {
						decrement();
					} else {
						if (matches.size() == repetition.top)
							matches.removeLast();
						matches.add(n);
						if (matches.size() < repetition.top)
							matchers.add(r.matcher(cs, n.end(), next, cache));
					}
				} else {
					decrement();
				}
			}
			next.setEnd(matches.isEmpty() ? offset : matches.peekLast().end());
			Match[] children = matches.toArray(new Match[matches.size()]);
			next.setChildren(children);
		}

		private void decrement() {
			matchers.removeLast();
			if (matchers.isEmpty()) {
				done = true;
				next = null;
				matchers = null;
				matches = null;
			} else {
				matches.removeLast();
			}
		}

		@Override
		public Rule rule() {
			return RepetitionRule.this;
		}

	}

	private class PossessiveMatcher extends GreedyAndPossessive {

		protected PossessiveMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, Match>> cache, Label label) {
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
					next = new Match(RepetitionRule.this, offset, parent);
					Match[] children = matched
							.toArray(new Match[matched.size()]);
					next.setChildren(children);
					next.setEnd(matched.peekLast().end());
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
	public Matcher matcher(CharSequence cs, int offset, Match parent,
			Map<Label, Map<Integer, Match>> cache) {
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
