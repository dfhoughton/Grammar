package dfh.grammar;

import java.util.List;

public class NegationCondition extends LogicalCondition {
	private static final long serialVersionUID = 1L;

	public NegationCondition(List<Condition> conditions) {
		super(conditions);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		return !subconditions[0].passes(n, m, s);
	}

}
