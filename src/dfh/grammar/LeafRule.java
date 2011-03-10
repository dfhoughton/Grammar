package dfh.grammar;

/**
 * {@link Rule} defined over sequence of terminal objects rather than other
 * <code>Rules</code>.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class LeafRule extends Rule {

	private static final long serialVersionUID = 1L;

	public LeafRule(String label) {
		super(label);
	}
}
