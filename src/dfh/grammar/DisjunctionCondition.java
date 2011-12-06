package dfh.grammar;

import java.util.List;

public class DisjunctionCondition extends LogicalCondition {
	private static final long serialVersionUID = 1L;

	public DisjunctionCondition(List<Condition> conditions) {
		super(conditions);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		for (Condition c : subconditions) {
			if (c.passes(n, m, s))
				return true;
		}
		return false;
	}

}
