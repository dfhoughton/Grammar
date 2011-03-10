package dfh.grammar;

/**
 * Half-parsed piece of a grammar corresponding to a zero-width assertion.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class AssertionFragment implements RuleFragment {
	public final boolean isTrue;
	public final String id;

	public AssertionFragment(String id, boolean isTrue) {
		this.id = id;
		this.isTrue = isTrue;
	}
}
