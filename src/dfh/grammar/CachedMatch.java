package dfh.grammar;

/**
 * Object to put into the match cache so we can distinguish three states:
 * <ol>
 * <li>untested
 * <li>failure
 * <li>match
 * </ol>
 * <b>Creation date:</b> Mar 24, 2011
 * 
 * @author David Houghton
 * 
 */
public class CachedMatch {
	final Match m;

	/**
	 * To reduce object creation we have a common mismatch object.
	 */
	public static final CachedMatch MISMATCH = new CachedMatch(null);
	/**
	 * To reduce object creation we have a common match object for
	 * non-terminals.
	 */
	public static final CachedMatch MATCH = new CachedMatch(Match.DUMMY);

	public CachedMatch(Match m) {
		this.m = m;
	}
}
