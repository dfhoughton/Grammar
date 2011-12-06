package dfh.grammar;

import java.util.List;

public class XORCondition extends LogicalCondition {
	private static final long serialVersionUID = 1L;

	public XORCondition(List<Condition> conditions) {
		super(conditions);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		int count = 0;
		for (Condition c : subconditions) {
			if (c.passes(n, m, s))
				count++;
		}
		return count % 2 == 1;
	}

}
