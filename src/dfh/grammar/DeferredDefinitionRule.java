package dfh.grammar;

import java.util.Map;
import java.util.Set;

import dfh.grammar.Grammar.ConstantOptions;

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
	/**
	 * Holds rule whose definition is deferred.
	 */
	protected Rule r;

	/**
	 * Generates {@link Rule} from given label.
	 * 
	 * @param label
	 */
	public DeferredDefinitionRule(Label label) {
		super(label);
	}

	@Override
	public Matcher matcher(CharSequence cs, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return r.matcher(cs, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return "({DEFERRED}" + label + ")";
	}

	/**
	 * Defines this rule as the given {@link Rule}.
	 * 
	 * @param r
	 */
	public void setRule(Rule r) {
		if (this.r != null)
			throw new GrammarException("rule " + label + "already defined");
		this.r = r;
	}

	@Override
	public String description() {
		if (r instanceof DeferredDefinitionRule)
			return r.label.toString();
		return r == null ? "UNDEFINED" : r.description();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache,
			Set<Rule> studiedRules, ConstantOptions options) {
		return r.study(s, cache, studiedRules, options);
	}

	@Override
	public boolean zeroWidth() {
		return r.zeroWidth();
	}

	@Override
	public Rule shallowClone() {
		DeferredDefinitionRule ddr = new DeferredDefinitionRule(
				(Label) label.clone());
		ddr.r = r;
		return ddr;
	}
}
