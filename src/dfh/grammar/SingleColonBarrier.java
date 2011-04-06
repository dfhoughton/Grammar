package dfh.grammar;

/**
 * {@link Exception} thrown when a rule attempts to backtrack into a single
 * colon backtracking barrier. This error will be caught by the immediately
 * enclosing {@link SequenceRule}, which will then fail to match.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
public class SingleColonBarrier extends GrammarException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param m
	 *            barrier rule's matcher
	 */
	public SingleColonBarrier(Matcher m) {
		super("hit : barrier in " + m.master.rule().label);
	}
}
