package dfh.grammar;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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

		public RepetitionMatcher(CharSequence cs, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, cache, RepetitionRule.this, master);
		}
	}

	private abstract class GreedyAndPossessive extends RepetitionMatcher {

		private final boolean backtracks;

		protected GreedyAndPossessive(CharSequence cs, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				boolean backtracks, Matcher master) {
			super(cs, offset, cache, label, master);
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
				m = r.matcher(s, start, cache, this);
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
		protected GreedyMatcher(CharSequence cs, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, cache, label, true, master);
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
				next = new Match(RepetitionRule.this, offset);
				next.setChildren(children);
				next.setEnd(matched.isEmpty() ? offset : matched.peekLast()
						.end());
			}
		}

	}

	private class StingyMatcher extends RepetitionMatcher {

		protected StingyMatcher(CharSequence cs, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, cache, label, master);
			matchers = new LinkedList<Matcher>();
			matched = new LinkedList<Match>();
			matchers.add(r.matcher(cs, offset, cache, master));
		}

		@Override
		protected void fetchNext() {
			next = new Match(RepetitionRule.this, offset);
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
							matchers.add(r.matcher(s, n.end(), cache, this));
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

		protected PossessiveMatcher(CharSequence cs, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Label label,
				Matcher master) {
			super(cs, offset, cache, label, false, master);
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
					next = new Match(RepetitionRule.this, offset);
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
	public Matcher matcher(CharSequence cs, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		switch (repetition.t) {
		case possessive:
			return new PossessiveMatcher(cs, offset, cache, label, master);
		case stingy:
			return new StingyMatcher(cs, offset, cache, label, master);
		default:
			return new GreedyMatcher(cs, offset, cache, label, master);
		}
	}

	@Override
	protected String uniqueId() {
		return r.uniqueId() + repetition;
	}

	@Override
	public String description() {
		StringBuilder b = new StringBuilder();
		if (r.generation == -1) {
			if (r instanceof SequenceRule) {
				b.append("[ ");
				b.append(r.description());
				b.append(" ]");
			} else
				b.append(r.description());
		} else
			b.append(r.label());
		b.append(repetition);
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache, int offset,
			Set<Rule> studiedRules) {
		studiedRules.add(this);
		if (studiedRules.contains(r))
			return new HashSet<Integer>(0);
		return r.study(s, cache, offset, studiedRules);
	}
}
