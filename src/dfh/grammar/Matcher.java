package dfh.grammar;

import java.util.Map;

/**
 * An object associated with a {@link Rule} that can iterate over and return all
 * the possible parse trees meeting the matching conditions (pattern and start
 * and end offsets).
 * <p>
 * <b>Creation date:</b> Mar 14, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class Matcher {
	/**
	 * {@link CharSequence} being matched against.
	 */
	protected final CharSequence s;
	/**
	 * Start offset for any {@link Match} returned by this {@link Matcher}.
	 */
	protected final Integer offset;
	/**
	 * {@link Matcher} generating the parent {@link Match} for any {@link Match}
	 * generated by this {@link Matcher}.
	 */
	protected final Matcher master;
	/**
	 * Reference to collection of {@link RuleState} objects potentially useful
	 * to the matcher.
	 */
	protected final Map<Rule, RuleState> ruleStates;

	/**
	 * Generate a {@link Matcher} with the given state.
	 * 
	 * @param s
	 * @param offset
	 * @param master
	 */
	protected Matcher(CharSequence s, Integer offset, Matcher master) {
		this.s = s;
		this.offset = offset;
		this.master = master;
		this.ruleStates = master.ruleStates;
	}

	/**
	 * Constructor only to be used by {@link Grammar}.
	 * 
	 * @param s
	 * @param offset
	 * @param master
	 * @param ruleStates
	 */
	Matcher(CharSequence s, Integer offset, Matcher master,
			Map<Rule, RuleState> ruleStates) {
		this.s = s;
		this.offset = offset;
		this.master = master;
		this.ruleStates = ruleStates;
	}

	/**
	 * <code>match()</code> obtains the next match and iterates. If all matches
	 * have been found, it returns <code>null</code>.
	 * 
	 * @return next parse tree or null if no parse tree is possible
	 */
	public abstract Match match();

	/**
	 * Tests whether iteration is possible without iterating. For most purposes
	 * one can skip {@link #mightHaveNext()}, using the following design pattern
	 * instead:
	 * 
	 * <pre>
	 * Match n;
	 * while ((n = m.match()) != null) {
	 * 	// do something with n
	 * }
	 * </pre>
	 * 
	 * @return whether the sequence this matcher is iterating over has reached
	 *         its end
	 */
	protected abstract boolean mightHaveNext();

	/**
	 * @return the {@link Rule} that generated this {@link Matcher}
	 */
	protected abstract Rule rule();
}
