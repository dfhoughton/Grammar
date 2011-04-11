package dfh.grammar;

/**
 * Holds an assertion in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 *    &lt;a&gt; = ~ '1' &lt;rule&gt;
 *    &lt;b&gt; = ! '1' &lt;rule&gt;
 *    &lt;c&gt; = ~ [ '1' | '2'] &lt;rule&gt;
 *    &lt;d&gt; = ~ '1' &lt;rule&gt;
 * &lt;rule&gt; = /\b\d++/
 * </pre>
 * 
 * The <code>~</code> and <code>!</code> mark positive and negative assertions,
 * respectively. Assertions have a width of zero, so they amount to a test which
 * doesn't consume any content.
 * <p>
 * At the moment only forward assertions are implemented. I plan to implement
 * variable width backwards assertions *without regular expressions* in the near
 * future.
 * <p>
 * <b>Creation date:</b> Apr 7, 2011
 * 
 * @author David Houghton
 * 
 */
public class AssertionFragment implements RuleFragment {
	protected final boolean positive;
	protected RuleFragment rf;
	protected final boolean forward;

	/**
	 * Generates a fragment with given assertion status.
	 * 
	 * @param positive
	 *            whether matching the assertion rule constitutes a match of the
	 *            assertion
	 * @param forward
	 */
	public AssertionFragment(boolean positive, boolean forward) {
		this.positive = positive;
		this.forward = forward;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(positive ? '~' : '!');
		b.append(forward ? '+' : '-');
		b.append(rf);
		return b.toString();
	}
}
