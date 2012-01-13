package dfh.grammar;

import java.util.List;

public class XORCondition extends LogicalCondition {
	private static final long serialVersionUID = 4L;

	public XORCondition(List<Condition> conditions) {
		super(conditions);
	}

	private XORCondition(int length) {
		super(length);
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

	@Override
	protected LogicalCondition duplicate() {
		XORCondition cj = new XORCondition(subconditions.length);
		for (int i = 0; i < subconditions.length; i++) {
			Condition c = subconditions[i];
			if (c instanceof LogicalCondition)
				c = ((LogicalCondition) c).duplicate();
			cj.subconditions[i] = c;
		}
		return cj;
	}

}
