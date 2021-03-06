/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * For applying a post-match test to a {@link Match}.
 * <p>
 * <b>Creation date:</b> Apr 5, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class Condition implements Serializable {
	private static final long serialVersionUID = 2L;
	public String name;

	/**
	 * Convenience method that generates a condition that is always true.
	 * 
	 * @return a condition that is always true
	 */
	public static Condition TRUE() {
		return new Condition() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean passes(Match n, Matcher m, CharSequence s) {
				return true;
			}
		};
	}

	/**
	 * Convenience method that generates a condition that is always false.
	 * 
	 * @return a condition that is always false
	 */
	public static Condition FALSE() {
		return new Condition() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean passes(Match n, Matcher m, CharSequence s) {
				return false;
			}
		};
	}

	/**
	 * Returns whether the {@link Match} meets the given condition.
	 * <em>Note</em>: a condition should have no mutable state or side effects,
	 * or at least none that affect the possibility of future matches. To the
	 * extent that they have side-effects and mutable state they risk producing
	 * concurrency issues and/or context dependent bugs that are hard to
	 * reproduce and fix.
	 * <p>
	 * Unless overridden, this method will merely delegate its job to
	 * {@link #passes(Match, CharSequence)}, ignoring the {@link Matcher}
	 * argument.
	 * 
	 * @param n
	 *            {@link Match} being tested
	 * @param m
	 *            the {@link Matcher} that produced the {@link Match}
	 * @param s
	 *            {@link CharSequence} being matched against
	 * @return whether the {@link Match} meets the condition
	 */
	public boolean passes(Match n, Matcher m, CharSequence s) {
		return passes(n, s);
	}

	/**
	 * Returns whether the {@link Match} meets the given condition. See
	 * {@link #passes(Match, Matcher, CharSequence)} for caveats.
	 * <p>
	 * Unless overridden, this method will merely delegate its job to
	 * {@link #passes(CharSequence)}, ignoring the {@link Match} argument. The
	 * {@link CharSequence} passed will have its order corrected via
	 * {@link #subsequence(Match, CharSequence)}, so in a backwards assertion
	 * {@link #passes(CharSequence)} will not see a reversed sequence.
	 * 
	 * @param n
	 *            {@link Match} being tested
	 * @param s
	 *            {@link CharSequence} being matched against
	 * @return whether the {@link Match} meets the condition
	 */
	public boolean passes(Match n, CharSequence s) {
		return passes(subsequence(n, s));
	}

	/**
	 * Whether the character subsequence matched meets the given condition.
	 * Unless overridden, this method will always return {@code true}. See
	 * {@link #passes(Match, Matcher, CharSequence)} for caveats.
	 * 
	 * @param s
	 *            {@link CharSequence} being matched against
	 * @return whether the subsequence matched meets the condition
	 */
	public boolean passes(CharSequence s) {
		return true;
	}

	/**
	 * Character subsequence of {@link CharSequence} matched by given
	 * {@link Match}. The order of the subsequence will be fixed in the case
	 * where it has been reversed inside a backwards assertion. This ensures
	 * that the same condition can be used inside and outside lookbehind
	 * assertions as in
	 * 
	 * <pre>
	 * {@code 
	 * a = ~- <b> 'foo' <b>
	 * b = /\d++/r (greater_than_5)
	 * }
	 * </pre>
	 * 
	 * Without correcting for reversion, <i>10foo10</i> would fail to match
	 * {@code <a>} because the condition would see <i>01</i> rather than
	 * <i>10</i> when tested for the first {@code <b>}.
	 * 
	 * @param n
	 *            {@link Match} found in character sequence
	 * @param s
	 *            {@link CharSequence} matched against
	 * @return order-adjusted subsequence matched
	 */
	public static CharSequence subsequence(Match n, CharSequence s) {
		if (s instanceof ReversedCharSequence
				&& ((ReversedCharSequence) s).isReversed()) {
			StringBuilder b = new StringBuilder(n.end() - n.start());
			for (int i = n.end() - 1; i >= n.start(); i--)
				b.append(s.charAt(i));
			return b;
		}
		return s.subSequence(n.start(), n.end());
	}

	public String getName() {
		return name;
	}

	/**
	 * To be called during compilation, not by user.
	 * 
	 * @param name
	 *            the name the condition is identified by in the grammar
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * Adjusts the condition name to move it into the specified namespace.
	 * 
	 * @param base
	 */
	protected void addNameBase(String base) {
		if (name != null)
			name = base + ':' + name;
	}

	/**
	 * Required by {@link Grammar#describe()}. Delegates to
	 * {@link #describe(boolean)} with the parameter {@code false}.
	 * 
	 * @return string representing condition
	 */
	protected final String describe() {
		return describe(false);
	}

	/**
	 * Returns string representing condition, showing hidden sub-conditions only
	 * if {@code showAll} is {@code true}.
	 * 
	 * @param showAll
	 *            whether to show even hidden conditions
	 * @return string representing condition, showing hidden sub-conditions only
	 *         if {@code showAll} is {@code true}
	 */
	protected String describe(boolean showAll) {
		if (visible())
			return name;
		return showAll ? name : "";
	}

	/**
	 * Facilitates the renaming of conditions for
	 * {@link Grammar#defineRule(String, Grammar, String, Condition)}.
	 * 
	 * @param namebase
	 * @param knownConditions
	 * @return renamed copy of current condition
	 */
	Condition copy(String namebase, Set<String> knownConditions) {
		return knownConditions.contains(name) ? new WrappedCondition(namebase,
				this) : this;
	}

	/**
	 * Returns all condition labels required to be defined in defining this
	 * condition. This method is required for grammar composition.
	 * 
	 * @return all condition labels required to be defined in defining this
	 *         condition
	 */
	public Set<String> conditionNames() {
		Set<String> set = new TreeSet<String>();
		set.add(name);
		return set;
	}

	/**
	 * Returns whether the condition will be displayed by
	 * {@link Grammar#describe()}.
	 * 
	 * @return whether the condition will be displayed by
	 *         {@link Grammar#describe()}
	 */
	public boolean visible() {
		return name.charAt(0) != '.';
	}

	/**
	 * Walks down match tree to first {@link Match} not produced by a
	 * {@link ConditionalRule}. This is useful when you want to test the
	 * properties of sequences matched by the rule to which a condition was
	 * applied; for example, when you wish to examine a particular member of a
	 * sequence.
	 * 
	 * @param m
	 *            starting match
	 * @return match not generated by a {@link ConditionalRule}
	 */
	protected Match nonconditionalMatch(Match m) {
		while (m.rule() instanceof ConditionalRule)
			m = m.children()[0];
		return m;
	}
}
