package dfh.grammar;

/**
 * An object for keeping track of the state of a given terminal {@link Rule} in
 * a particular match context. <b>Creation date:</b> Mar 30, 2011
 * 
 * @author David Houghton
 * 
 */
public class TerminalState implements RuleState {
	public final boolean studied;

	public TerminalState(boolean studied) {
		this.studied = studied;
	}
}
