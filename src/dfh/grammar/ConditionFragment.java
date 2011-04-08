package dfh.grammar;

/**
 * Holds a condition in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * &lt;a&gt; = /\d++/ (less_than_20)
 * </pre>
 * 
 * where <code>{less_than_20}</code> must later be defined by invoking
 * {@link LeafRule#conditionalize(Condition, String)}.
 * <p>
 * <b>Creation date:</b> Apr 5, 2011
 * 
 * @author David Houghton
 * 
 */
public class ConditionFragment implements RuleFragment {
	protected final String id;

	/**
	 * Generates a fragment representing the specified condition.
	 * 
	 * @param id
	 *            phrase identifying condition
	 */
	public ConditionFragment(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return '(' + id + ')';
	}
}