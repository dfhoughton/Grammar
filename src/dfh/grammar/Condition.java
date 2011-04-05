package dfh.grammar;

import java.io.Serializable;

/**
 * For applying a post-match test to a {@link Match}.
 * <p>
 * <b>Creation date:</b> Apr 5, 2011
 * 
 * @author David Houghton
 * 
 */
public interface Condition extends Serializable {
	/**
	 * Returns whether the {@link Match} meets the given condition.
	 * <em>Note</em>: a condition should have no mutable state or side effects,
	 * or at least none that affect the possibility of future matches. To the
	 * extent that they have side-effects and mutable state they risk producing
	 * concurrency issues and/or context dependent bugs that are hard to
	 * reproduce and fix.
	 * 
	 * @param m
	 *            {@link Match} being tested
	 * @param s
	 *            {@link CharSequence} being matched against
	 * @return whether the {@link Match} meets the condition
	 */
	boolean passes(Match m, CharSequence s);
}
