package dfh.grammar;

/**
 * Return value struct for {@link RuleParser#next()}.
 * <p>
 * 
 * @author David F. Houghton - Jun 18, 2012
 * 
 */
final class SyntacticParse {
	final Label l;
	final SequenceFragment f;
	final ConditionFragment c;

	SyntacticParse(Label l, SequenceFragment f, ConditionFragment c) {
		this.l = l;
		this.f = f;
		this.c = c;
	}
}
