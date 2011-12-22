package dfh.grammar;

import java.util.List;

public class ConjunctionCondition extends LogicalCondition {
	private static final long serialVersionUID = 2L;

	public ConjunctionCondition(List<Condition> conditions) {
		super(conditions);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		for (Condition c : subconditions) {
			if (!c.passes(n, m, s))
				return false;
		}
		return true;
	}

}
