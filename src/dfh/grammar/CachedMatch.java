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
	Match m;

	CachedMatch(Match m) {
		this.m = m;
	}
}
