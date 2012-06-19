/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
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
public class Regex extends RepeatableRuleFragment {
	final Pattern re;
	boolean reversible;
	private static final Pattern parsingPattern = Pattern
			.compile("/(.*)/([rimsdux]*+)");

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
				case 'r':
					reversible = true;
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
		StringBuilder b = new StringBuilder();
		b.append('/');
		b.append(re);
		b.append('/');
		if (reversible)
			b.append('r');
		return b.toString();
	}
}
