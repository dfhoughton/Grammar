package dfh.grammar;

import java.util.List;

/**
 * Base class for conditions representing logical combinations of other
 * conditions.
 * <p>
 * <b>Creation date:</b> Dec 5, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class LogicalCondition extends Condition {
	private static final long serialVersionUID = 1L;
	protected final Condition[] subconditions;

	public LogicalCondition(List<Condition> conditions) {
		subconditions = conditions.toArray(new Condition[conditions.size()]);
	}

	public boolean passes(Match n, Matcher m, CharSequence s) {
		return allPass(n, m, s);
	}

	protected abstract boolean allPass(Match n, Matcher m, CharSequence s);
}
