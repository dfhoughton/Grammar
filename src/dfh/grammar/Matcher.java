package dfh.grammar;

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
	 * Reference to the matching parameters in use in this context.
	 */
	protected final GlobalState options;
	/**
	 * Rightmost match found by this {@link Matcher} or any of its descendants.
	 */
	protected Match rightmost = null;

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
		this.options = master.options;
	}

	/**
	 * Constructor only to be used by {@link Grammar}.
	 * 
	 * @param s
	 * @param offset
	 * @param master
	 * @param ruleStates
	 */
	Matcher(CharSequence s, Integer offset, Matcher master, GlobalState options) {
		this.s = s;
		this.offset = offset;
		this.master = master;
		this.options = options;
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

	/**
	 * Returns the rightmost {@link Match} found in the last attempt to obtain a
	 * {@link Match} from this {@link Matcher}. In the event of a successful
	 * match this is just the same as {@link #match()}. The result differs in
	 * the event of a failure to match and can be used to determine either how
	 * the sequence matched against is invalid or the grammar itself is flawed.
	 * 
	 * @return the rightmost {@link Match} found in the last attempt to obtain a
	 *         {@link Match} from this {@link Matcher}
	 */
	public Match rightmostMatch() {
		return rightmost;
	}

	/**
	 * Updates {@link #rightmost} and returns input parameter.
	 * 
	 * @param m
	 * @return the input {@link Match}
	 */
	protected Match register(Match m) {
		if (options.keepRightmost && m != null
				&& (rightmost == null || rightmost.end() <= m.end())) {
			rightmost = m;
			if (master != null)
				master.register(m);
		}
		return m;
	}

	/**
	 * Called by matchers possibly involving conditions while matching to
	 * enforce logging of condition testing when appropriate.
	 * 
	 * @param c
	 * @param child
	 * @return
	 */
	protected boolean testCondition(Condition c, Match child) {
		return c == null || c.passes(child, this, s);
	}
}
