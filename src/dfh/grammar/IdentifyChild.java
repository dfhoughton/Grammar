package dfh.grammar;

/**
 * Identifies whether the given {@link Rule}, a child of this {@link Rule},
 * bears the given label or tag.
 * 
 * <b>Creation date:</b> Jun 10, 2011
 * 
 * @author David Houghton
 * 
 */
public interface IdentifyChild {
	boolean is(Match parent, Match child, String label);
}
