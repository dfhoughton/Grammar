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
	final Rule r;
	final Repetition repetition;

	private abstract class RepetitionMatcher extends NonterminalMatcher {
		protected LinkedList<Match> matched;
		protected LinkedList<Matcher> matchers;

		public RepetitionMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, parent, cache, RepetitionRule.this, master);
		}

		@Override
		public String identify() {
			StringBuilder b = new StringBuilder(label.id);
			if (matched != null) {
				boolean nonInitial = false;
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

	private abstract class GreedyAndPossessive extends RepetitionMatcher {

		private final boolean backtracks;

		protected GreedyAndPossessive(CharSequence cs, int offset,
				Match parent, Map<Label, Map<Integer, CachedMatch>> cache,
				Label label, boolean backtracks, Matcher master) {
			super(cs, offset, parent, cache, label, master);
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
				m = r.matcher(s, start, parent, cache, this);
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
	}

	/**
	 * <b>Creation date:</b> Mar 17, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private class GreedyMatcher extends GreedyAndPossessive {
		protected GreedyMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, parent, cache, label, true, master);
		}

		@Override
		protected void fetchNext() {
			if (matched == null)
				initialize();
			else if (matched.isEmpty()) {
				matchers.clear();
				matchers = null;
				next = null;
				done = true;
				return;
			} else {
				matched.removeLast();
				// see if we can find some other way forward
				if (matchers.peekLast().mightHaveNext()) {
					while (!(matchers.isEmpty() || matched.size() == repetition.top)) {
						if (grab())
							break;
					}
				} else {
					matchers.removeLast();
					if (!matchers.isEmpty()
							&& matchers.peekLast().mightHaveNext())
						fetchNext();
				}
			}
			if (matched.size() < repetition.bottom) {
				matched.clear();
				matchers = null;
				next = null;
				done = true;
			} else {
				Match[] children = matched.toArray(new Match[matched.size()]);
				next = new Match(RepetitionRule.this, offset, parent);
				next.setChildren(children);
				next.setEnd(matched.isEmpty() ? offset : matched.peekLast()
						.end());
			}
		}

	}

	private class StingyMatcher extends RepetitionMatcher {

		protected StingyMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, parent, cache, label, master);
			matchers = new LinkedList<Matcher>();
			matched = new LinkedList<Match>();
			matchers.add(r.matcher(cs, offset, parent, cache, master));
		}

		@Override
		protected void fetchNext() {
			next = new Match(RepetitionRule.this, offset, parent);
			while (next != null && matched.size() < repetition.bottom) {
				Matcher m = matchers.peekLast();
				if (m.mightHaveNext()) {
					Match n = m.match();
					if (n == null) {
						decrement();
					} else {
						if (matched.size() == repetition.top)
							matched.removeLast();
						matched.add(n);
						if (matched.size() < repetition.top)
							matchers.add(r.matcher(s, n.end(), next, cache,
									this));
					}
				} else {
					decrement();
				}
			}
			next.setEnd(matched.isEmpty() ? offset : matched.peekLast().end());
			Match[] children = matched.toArray(new Match[matched.size()]);
			next.setChildren(children);
		}

		private void decrement() {
			matchers.removeLast();
			if (matchers.isEmpty()) {
				done = true;
				next = null;
				matchers = null;
				matched = null;
			} else {
				matched.removeLast();
			}
		}
	}

	private class PossessiveMatcher extends GreedyAndPossessive {

		protected PossessiveMatcher(CharSequence cs, int offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, parent, cache, label, false, master);
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
	public Matcher matcher(CharSequence cs, Integer offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		switch (repetition.t) {
		case possessive:
			return new PossessiveMatcher(cs, offset, parent, cache, label,
					master);
		case stingy:
			return new StingyMatcher(cs, offset, parent, cache, label, master);
		default:
			return new GreedyMatcher(cs, offset, parent, cache, label, master);
		}
	}

	@Override
	protected String uniqueId() {
		return r.uniqueId() + repetition;
	}
}
