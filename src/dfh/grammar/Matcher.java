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
public interface Matcher {

	/**
	 * <code>next()</code> obtains the next match and iterates. If all matches
	 * have been found, it returns <code>null</code>.
	 * 
	 * @param offsetCache
	 * @return next parse tree or null if no parse tree is possible
	 */
	public Node match();

	/**
	 * Tests whether iteration is possible without iterating. For most purposes
	 * one can skip {@link #mightHaveNext()}, using the following design pattern
	 * instead:
	 * 
	 * <pre>
	 * Node n;
	 * while ((n = m.match()) != null) {
	 * 	// do something with n
	 * }
	 * </pre>
	 * 
	 * @return whether the sequence this matcher is iterating over has reached
	 *         its end
	 */
	boolean mightHaveNext();

	/**
	 * @return the {@link Rule} that generated this {@link Matcher}
	 */
	public Rule rule();
}
