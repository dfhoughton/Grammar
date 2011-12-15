package dfh.grammar;

import java.util.Set;

/**
 * Identifies whether the given {@link Rule}, a child of this {@link Rule},
 * bears the given label or tag. Also the tags that can be used to identify a
 * given child via {@link Match#hasLabel(String)}.
 * <p>
 * This interface is used in finding named groups such as
 * <code>[{foo} 'bar' ]</code>.
 * 
 * <b>Creation date:</b> Jun 10, 2011
 * 
 * @author David Houghton
 * 
 */
public interface IdentifyChild {
	/**
	 * Returns whether the given label applies to the given child.
	 * 
	 * @param parent
	 * @param child
	 * @param label
	 * @return whether the given label applies to the given child
	 */
	boolean is(Match parent, Match child, String label);

	/**
	 * Returns labels applying to given child.
	 * 
	 * @param parent
	 * @param child
	 * @return labels applying to given child
	 */
	Set<String> labels(Match parent, Match child);
}
