package dfh.grammar;

/**
 * {@link Exception} thrown when a rule attempts to backtrack into a double
 * colon backtracking barrier. This error will be caught by the root
 * {@link Matcher} and the entire match will fail.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
public class DoubleColonBarrier extends GrammarException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param m
	 *            barrier rule's matcher
	 */
	public DoubleColonBarrier(Matcher m) {
		super("hit :: barrier in " + m.master.rule().label);
	}
}