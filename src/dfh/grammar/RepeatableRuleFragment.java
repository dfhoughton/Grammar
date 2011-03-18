package dfh.grammar;


/**
 * Represents a portion of a rule that may have a repetition modifier.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class RepeatableRuleFragment implements RuleFragment {

	Repetition rep = Repetition.NONE;

	public void setRepetition(Repetition rep) {
		this.rep = rep;
	}

	@Override
	public abstract String toString();
}
