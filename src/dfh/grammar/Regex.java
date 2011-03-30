package dfh.grammar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex implements RuleFragment {
	final Pattern re;
	private static final Pattern parsingPattern = Pattern
			.compile("/(.*)/([imsdux]*+)");

	public Regex(String re) {
		Matcher m = parsingPattern.matcher(re);
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
