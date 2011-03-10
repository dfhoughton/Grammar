package dfh;

import java.util.regex.Pattern;

/**
 * Creates {@link Rule} objects from stringified specifications.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class RuleParser {
	/**
	 * Pattern that defines a rule as "<"<name>">" "=" <remainder>
	 */
	public static final Pattern basePattern = Pattern
			.compile("\\s*+<(\\w++)>\\s*+=\\s*+(.*?)\\s*+");
}
