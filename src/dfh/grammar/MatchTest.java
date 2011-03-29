package dfh.grammar;

/**
 * For testing whether a {@link Match} meets some arbitrary test.
 * <p>
 * <b>Creation date:</b> Mar 29, 2011
 * 
 * @author David Houghton
 * 
 */
public interface MatchTest {
	/**
	 * Maximally flexible test prototype.
	 * 
	 * @param m
	 *            the {@link Match} that must pass the test
	 * @return whether the {@link Match} passes
	 */
	boolean test(Match m);
}
