package dfh.grammar;

/**
 * Holds a back reference in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; 'foo' 1
 * </pre>
 * 
 * where 1 in this case refers back to whatever string was matched by
 * <code>&lt;b&gt;</code>.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class BackReferenceFragment implements RuleFragment {
	protected int reference;

	/**
	 * Generates a fragment from the given reference number.
	 * 
	 * @param reference
	 */
	public BackReferenceFragment(int reference) {
		this.reference = reference - 1;
	}

	public String toString() {
		return Integer.toString(reference + 1);
	}
}
