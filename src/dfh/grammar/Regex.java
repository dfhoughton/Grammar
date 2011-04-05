package dfh.grammar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RuleFragment} for a rule such as
 * 
 * <pre>
 * &lt;a&gt; = /foo/i
 * </pre>
 * 
 * <b>Creation date:</b> Apr 1, 2011
 * 
 * @author David Houghton
 * 
 */
public class Regex implements RuleFragment {
	final Pattern re;
	private static final Pattern parsingPattern = Pattern
			.compile("/(.*)/([imsdux]*+)");

	/**
	 * Generates {@link Regex}, compiling a {@link Pattern} from the given
	 * string.
	 * 
	 * @param re
	 */
	public Regex(String re) {
		Matcher m = parsingPattern.matcher(re.trim());
		if (m.matches()) {
			int options = 0;
			for (char c : m.group(2).toCharArray()) {
				switch (c) {
				case 'i':
					options |= Pattern.CASE_INSENSITIVE;
					break;
				case 'm':
					options |= Pattern.MULTILINE;
					break;
				case 's':
					options |= Pattern.DOTALL;
					break;
				case 'x':
					options |= Pattern.COMMENTS;
					break;
				case 'd':
					options |= Pattern.UNIX_LINES;
					break;
				case 'u':
					options |= Pattern.UNICODE_CASE;
					break;
				default:
					break;
				}
			}
			this.re = Pattern.compile(m.group(1), options);
		} else
			throw new GrammarException("ill-formed regular expression: " + re);
	}

	@Override
	public String toString() {
		return re.toString();
	}
}
