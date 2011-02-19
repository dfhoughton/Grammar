package dfh;

/**
 * {@link Rule} defined over sequence of terminal objects rather than other
 * <code>Rules</code>.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 * 
 * @param <K>
 *            type of object over which Rule is defined
 */
public class LeafRule<K> extends Rule<K> {

	private static final long serialVersionUID = 1L;

	public LeafRule(String label) {
		super(label);
	}
}
