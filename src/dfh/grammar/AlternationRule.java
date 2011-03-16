package dfh.grammar;

import java.util.Map;

public class AlternationRule extends Rule {
	private static final long serialVersionUID = 1L;

	public AlternationRule(Label l, GroupFragment gf, Map<Label, Rule> rules) {
		super(l);
	}

	@Override
	public Matcher matcher(char[] cs, int offset, Node parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Label label(GroupFragment rrf) {
		// TODO Auto-generated method stub
		return null;
	}

}
