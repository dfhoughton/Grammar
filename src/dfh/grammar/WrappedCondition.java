package dfh.grammar;

/**
 * Facilitates the renaming of conditions for
 * {@link Grammar#defineRule(String, Grammar, String, Condition)}.
 * <p>
 * 
 * @author David F. Houghton - Mar 11, 2012
 * 
 */
public class WrappedCondition extends Condition {
	private Condition inner;

	WrappedCondition(String namebase, Condition inner) {
		this.inner = inner;
		name = namebase + ':' + inner.getName();
	}

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		return inner.passes(n, m, s);
	}
}
