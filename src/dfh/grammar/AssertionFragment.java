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

	/**
	 * Generates a fragment from the given reference number.
	 * 
	 * @param id
	 *            phrase identifying condition
	 */
	public AssertionFragment(boolean positive) {
		this.positive = positive;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(positive ? '~' : '!');
		b.append(rf);
		return b.toString();
	}
}
