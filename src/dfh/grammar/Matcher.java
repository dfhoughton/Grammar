package dfh.grammar;

/**
 * An object associated with a {@link Rule} that can iterate over and return all
 * the possible parse trees starting at a given offset in the string.
 * <p>
 * <b>Creation date:</b> Mar 14, 2011
 * 
 * @author David Houghton
 * 
 */
public interface Matcher {

	/**
	 * @param offsetCache
	 * @return next parse tree or null if no parse tree is possible
	 */
	public Node match();

	/**
	 * @return whether the sequence this matcher is iterating over has reached
	 *         its end
	 */
	public boolean mightHaveNext();
}
