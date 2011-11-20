package dfh.grammar;

/**
 * Convenience base class for defining conditions on rules that match floating
 * point numbers.
 * <p>
 * <b>Creation date:</b> Nov 20, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class FloatingPointCondition extends Condition {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		CharSequence ss = Condition.subsequence(n, s);
		try {
			double d = Double.parseDouble(ss.toString());
			return passes(d);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Determines which floating point numbers pass the condition. This is the
	 * only method you must implement when defining an
	 * {@link FloatingPointCondition}.
	 * 
	 * @param d
	 *            floating point number to test
	 * @return whether the number passes the test
	 */
	public abstract boolean passes(double d);
}
