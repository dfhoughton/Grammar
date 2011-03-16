package dfh.grammar;

import java.util.Map;

/**
 * A rule undefined at the time of grammar compilation. It will always
 * correspond to a terminal rule. <b>Creation date:</b> Mar 16, 2011
 * 
 * @author David Houghton
 * 
 */
public class DeferredDefinitionRule extends Rule {
	private static final long serialVersionUID = 1L;
	private final Map<Label, Rule> rules;
	private Rule r;

	public DeferredDefinitionRule(Label label, Map<Label, Rule> rules) {
		super(label);
		this.rules = rules;
	}

	@Override
	public Matcher matcher(char[] cs, int offset, Node parent) {
		if (r == null)
			r = rules.get(label);
		return r.matcher(cs, offset, parent);
	}

}
