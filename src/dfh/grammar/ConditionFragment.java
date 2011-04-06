package dfh.grammar;

/**
 * Holds a condition in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * &lt;a&gt; = /\d++/ {less_than_20}
 * </pre>
 * 
 * where <code>{less_than_20}</code> must later be defined by invoking
 * {@link LeafRule#conditionalize(Condition)}.
 * <p>
 * <b>Creation date:</b> Apr 5, 2011
 * 
 * @author David Houghton
 * 
 */
public class ConditionFragment implements RuleFragment {
	protected final String id;

	/**
	 * Generates a fragment from the given reference number.
	 * 
	 * @param reference
	 */
	public ConditionFragment(String id) {
		this.id = id;
	}

	public String toString() {
		return '{' + id + '}';
	}
}
