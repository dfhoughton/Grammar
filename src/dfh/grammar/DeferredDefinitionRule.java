package dfh.grammar;

import java.util.Map;

/**
 * A rule undefined at the time of grammar compilation. It will always
 * correspond to a terminal rule. This class exists so that a {@link Grammar}
 * can be compiled and then certain rules dynamically loaded in later.
 * <p>
 * This is similar to Perl's <code>AUTOLOAD</code> mechanism or a function
 * prototype.
 * <p>
 * <b>Creation date:</b> Mar 16, 2011
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
	public Matcher matcher(CharSequence cs, int offset, Match parent,
			Map<Label, Map<Integer, Match>> cache) {
		if (r == null)
			r = rules.get(label);
		return r.matcher(cs, offset, parent, cache);
	}
}
