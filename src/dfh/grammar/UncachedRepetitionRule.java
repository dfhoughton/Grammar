/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Like {@link RepetitionRule} but it doesn't cache its success or failure
 * offsets. Written to be used with the non-caching
 * {@link UpLevelBackReferenceRule}.
 * <p>
 * <b>Creation date:</b> Dec 10, 2011
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class UncachedRepetitionRule extends RepetitionRule {
	private static final long serialVersionUID = 6L;

	private abstract class UncachedRepetitionMatcher extends NonterminalMatcher {
		protected LinkedList<Match> matched;
		protected LinkedList<Matcher> matchers;

		public UncachedRepetitionMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
			super(offset, cache, UncachedRepetitionRule.this, master);
		}

		@Override
		public Match match() {
			if (options.debug)
				rule.matchTrace(this);
			if (done) {
				if (options.debug)
					rule.matchTrace(this, null);
				return null;
			}
			if (next == null && !(options.containsCycles && cycleCheck()))
				fetchNext();
			Match n = next;
			next = null;
			if (options.debug)
				rule.matchTrace(this, n);
			return register(n);
		}

		protected boolean grab() {
			int start = matched.isEmpty() ? offset : matched.peekLast().end();
			Matcher m;
			if (matchers.size() > matched.size())
				m = matchers.peekLast();
			else {
				m = r.matcher(start, cache, this);
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

	private abstract class GreedyAndPossessive extends
			UncachedRepetitionMatcher {

		private final boolean backtracks;

		protected GreedyAndPossessive(Integer offset,
				Map<Integer, CachedMatch>[] cache, Label label,
				boolean backtracks, Matcher master) {
			super(offset, cache, label, master);
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
	}

	/**
	 * <b>Creation date:</b> Mar 17, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private class GreedyMatcher extends GreedyAndPossessive {
		protected GreedyMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
			super(offset, cache, label, true, master);
		}

		@Override
		protected void fetchNext() {
			while (true) {
				if (matched == null)
					initialize();
				else if (matched.isEmpty()) {
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
							continue;
					}
				}
				if (matched.size() < repetition.bottom) {
					matched.clear();
					matchers = null;
					next = null;
					done = true;
				} else {
					Match[] children = matched
							.toArray(new Match[matched.size()]);
					next = new Match(UncachedRepetitionRule.this, offset);
					next.setChildren(children);
					next.setEnd(matched.isEmpty() ? offset : matched.peekLast()
							.end());
					if (c == null || c.passes(next, this, s))
						break;
				}
			}
		}

	}

	private class StingyMatcher extends UncachedRepetitionMatcher {
		private int goal;

		protected StingyMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
			super(offset, cache, label, master);
			matchers = new LinkedList<Matcher>();
			matched = new LinkedList<Match>();
			goal = repetition.bottom;
			seekGoal();
			if (next == null) {
				done = true;
				matched = null;
				matchers = null;
			} else if (!testCondition(c, next))
				fetchNext();
		}

		private void seekGoal() {
			boolean found = true;
			next = null;
			OUTER: while (matched.size() < goal) {
				if (grab()) {
					if (matchers.isEmpty()) {
						found = false;
						break;
					} else {
						while (!matchers.peekLast().mightHaveNext()) {
							if (matchers.size() == 1) {
								found = false;
								done = true;
								matchers = null;
								matched = null;
								break OUTER;
							} else {
								matchers.removeLast();
								matched.removeLast();
							}
						}
					}
				}
			}
			if (found) {
				next = new Match(UncachedRepetitionRule.this, offset);
				Match[] children = matched.toArray(new Match[matched.size()]);
				next.setChildren(children);
				if (matched.isEmpty())
					next.setEnd(offset);
				else
					next.setEnd(matched.peekLast().end());
			}
		}

		@Override
		protected void fetchNext() {
			while (true) {
				if (goal == 0)
					goal = 1;
				else
					matched.removeLast();
				while (goal <= repetition.top) {
					seekGoal();
					if (done)
						return;
					if (matchers.isEmpty())
						goal++;
					else
						break;
				}
				if (goal > repetition.top) {
					done = true;
					matchers = null;
					matched = null;
					break;
				} else if (c == null || c.passes(next, this, s))
					break;
			}
		}
	}

	private class PossessiveMatcher extends GreedyAndPossessive {

		protected PossessiveMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Label label, Matcher master) {
			super(offset, cache, label, false, master);
		}

		@Override
		protected void fetchNext() {
			next = null;
			if (matched == null) {
				initialize();
				if (matched.size() >= repetition.bottom) {
					next = new Match(UncachedRepetitionRule.this, offset);
					Match[] children = matched
							.toArray(new Match[matched.size()]);
					next.setChildren(children);
					next.setEnd(matched.isEmpty() ? offset : matched.peekLast()
							.end());
					if (!(c == null || c.passes(next, this, s))) {
						next = null;
						done = true;
						matched = null;
					}
				} else {
					done = true;
					matched = null;
				}
			} else {
				done = true;
				matched = null;
			}
		}
	}

	/**
	 * Generates a repetition rule with the given state.
	 * 
	 * @param label
	 * @param r
	 * @param rep
	 * @param alternateTags
	 */
	public UncachedRepetitionRule(Label label, Rule r, Repetition rep,
			Set<String> alternateTags) {
		super(label, r, rep, alternateTags);
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		switch (repetition.t) {
		case possessive:
			return new PossessiveMatcher(offset, cache, label, master);
		case stingy:
			return new StingyMatcher(offset, cache, label, master);
		default:
			return new GreedyMatcher(offset, cache, label, master);
		}
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder("{{UC}}");
		b.append(r.uniqueId()).append(repetition);
		if (condition != null)
			b.append('(').append(condition).append(')');
		return b.toString();
	}
}
