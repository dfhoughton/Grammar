package dfh.grammar;

import java.util.List;

public class NegationCondition extends LogicalCondition {
	private static final long serialVersionUID = 6L;

	public NegationCondition(List<Condition> conditions) {
		super(conditions);
	}

	private NegationCondition(int length) {
		super(length);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		return !subconditions[0].passes(n, m, s);
	}

	@Override
	protected LogicalCondition duplicate() {
		NegationCondition cj = new NegationCondition(subconditions.length);
		for (int i = 0; i < subconditions.length; i++) {
			Condition c = subconditions[i];
			if (c instanceof LogicalCondition)
				c = ((LogicalCondition) c).duplicate();
			cj.subconditions[i] = c;
		}
		return cj;
	}

}
