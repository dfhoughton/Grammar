package dfh.grammar;

/**
 * Holds a back reference in a rule.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class BackReferenceFragment implements RuleFragment {
	int reference;

	public BackReferenceFragment(int reference) {
		this.reference = reference - 1;
	}

	public String toString() {
		return Integer.toString(reference + 1);
	}
}
