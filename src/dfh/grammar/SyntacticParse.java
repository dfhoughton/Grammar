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
	final String text;

	SyntacticParse(String text, Label l, SequenceFragment f, ConditionFragment c) {
		this.text = text;
		this.l = l;
		this.f = f;
		this.c = c;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
