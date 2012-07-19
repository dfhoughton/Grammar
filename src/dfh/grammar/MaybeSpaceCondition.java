package dfh.grammar;

/**
 * Condition to ensure the last hidden space in a rule such as
 * 
 * <pre>
 * rule = 'a' 'b'?
 * </pre>
 * 
 * doesn't match if it would be the last non-zero-width constituent in the
 * sequence. In this example, it would prevent <code>rule</code> from matching
 * "a ", that is "a" with some following whitespace.
 * <p>
 * 
 * @author David F. Houghton - Jul 19, 2012
 * 
 */
public class MaybeSpaceCondition extends Condition {
	private static final long serialVersionUID = 2L;
	/**
	 * Identifier for this condition.
	 */
	public static final String ID = ".ms";
	{
		name = ID;
	}

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		Match[] sequence = n.children()[0].children();
		for (int i = sequence.length - 1; i >= 0; i--) {
			Match c = sequence[i];
			if (c.end() > c.start())
				return !(c.rule() instanceof HiddenSpace);
		}
		return true;
	}

	@Override
	public boolean visible() {
		return false;
	}
}
