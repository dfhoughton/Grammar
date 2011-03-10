package dfh.grammar;

/**
 * Represents a portion of a rule that may have a repetition modifier.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class RepeatableRuleFragment implements RuleFragment {

	private Repetition rep = Repetition.NONE;

	public void setRepetition(Repetition rep) {
		this.rep = rep;
	}

}
