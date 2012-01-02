package dfh.grammar;

import java.util.List;

public class DisjunctionCondition extends LogicalCondition {
	private static final long serialVersionUID = 2L;

	public DisjunctionCondition(List<Condition> conditions) {
		super(conditions);
	}

	private DisjunctionCondition(int length) {
		super(length);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		for (Condition c : subconditions) {
			if (c.passes(n, m, s))
				return true;
		}
		return false;
	}

	@Override
	protected LogicalCondition duplicate() {
		DisjunctionCondition cj = new DisjunctionCondition(subconditions.length);
		for (int i = 0; i < subconditions.length; i++) {
			Condition c = subconditions[i];
			if (c instanceof LogicalCondition)
				c = ((LogicalCondition) c).duplicate();
			cj.subconditions[i] = c;
		}
		return cj;
	}

}
