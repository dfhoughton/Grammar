package dfh.grammar;

/**
 * Corresponds to a literal expression: "this", for example.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class LiteralFragment extends RepeatableRuleFragment {

	/**
	 * sequence to match
	 */
	public final String literal;

	/**
	 * Generates {@link LiteralFragment} with given sequence.
	 * 
	 * @param literal
	 */
	public LiteralFragment(String literal) {
		this.literal = literal;
	}

	@Override
	public String toString() {
		return '"' + literal + '"' + rep;
	}
}
