package dfh.grammar;

/**
 * Holds a backtracking barrier in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; &lt;c&gt; : &lt;d&gt;
 * </pre>
 * 
 * where <code>:</code> means "given that <code>&lt;b&gt;</code> has matched
 * somehow or other, if we can't find a match for <code>&lt;d&gt;</code> don't
 * tinker with the first two elements any further: the whole rule fails."
 * Similarly,
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; &lt;c&gt; :: &lt;d&gt;
 * </pre>
 * 
 * where we have <code>::</code> instead of <code>:</code>, means that if we
 * can't continue the match after the barrier then the entire grammar fails to
 * match at the offset it is now considering.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
public class BarrierFragment implements RuleFragment {
	protected final String id;

	/**
	 * Generates a fragment from the given reference number.
	 * 
	 * @param reference
	 */
	public BarrierFragment(boolean single) {
		this.id = single ? ":" : "::";
	}

	public String toString() {
		return id;
	}
}
