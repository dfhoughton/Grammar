package dfh.grammar;

/**
 * Object produced by {@link RuleParser}.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public interface RuleFragment {
	/**
	 * @return BNF type representation of the fragment
	 */
	String stringify();
}
