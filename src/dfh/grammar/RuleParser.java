package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dfh.grammar.Label.Type;

/**
 * Creates {@link Rule} objects from stringified specifications.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class RuleParser {
	/**
	 * Basic pattern of a labeled rule.
	 */
	public static final Pattern labelPattern = Pattern
			.compile("([<(])(\\w++)([>)])");
	/**
	 * Pattern that defines a rule as "<"<name>">" "=" <remainder>
	 */
	public static final Pattern basePattern = Pattern.compile("\\s*+"
			+ labelPattern + "\\s*+=(.*?)\\s*+");
	/**
	 * Pattern of repetition symbols such as <code>*</code>.
	 */
	public static final Pattern repetitionPattern = Pattern
			.compile("(?:([?*+]|\\{\\d*+(?:,\\d*+)?\\})([+?]?+))?+");

	/**
	 * Pattern for comments and blank lines.
	 */
	public static final Pattern ignorePattern = Pattern
			.compile("^\\s*+(?:#.*)?$");

	/**
	 * Parses a line of the string representation of a grammar. Does
	 * tokenization and parsing but does not check completeness of rule set.
	 * 
	 * @param line
	 *            string representation of a grammar rule
	 * @return line parsed into properly nested tokens
	 * @throws GrammarException
	 */
	public static List<RuleFragment> parse(String line) throws GrammarException {
		if (line == null)
			throw new GrammarException("cannot parse nulls");
		if (ignorePattern.matcher(line).matches())
			return null;
		Matcher m = basePattern.matcher(line);
		if (m.matches()) {
			boolean isAngle = m.group(1).equals("<");
			if (isAngle) {
				if (!m.group(3).equals(">"))
					throw new GrammarException(
							"mismatched brackets in label for rule " + line);
			} else {
				if (!m.group(3).equals(")"))
					throw new GrammarException(
							"mismatched brackets in label for rule " + line);
			}
			String id = m.group(2);
			String remainder = m.group(4);
			List<RuleFragment> parse = new LinkedList<RuleFragment>();
			Type t;
			if (isAngle) {
				if (id.equals(Label.ROOT)) {
					t = Type.root;
				} else
					t = Type.nonTerminal;
			} else {
				t = Type.terminal;
			}
			// we've parsed out the rule label
			parse.add(new Label(t, id));
			if (t == Type.terminal)
				parse.add(new Regex(remainder));
			else {
				int[] offset = { 0 };
				parse.addAll(parseBody(remainder, offset, (char) 0));
			}
			return parse;
		} else
			throw new GrammarException("ill-formed rule: " + line);
	}

	/**
	 * Parses portion of rule to right of "=".
	 * 
	 * @param body
	 *            body of rule
	 * @param offset
	 *            single element int array allowing pass-by-reference for int,
	 *            defines start of substring being parsed
	 * @param bracket
	 *            end bracket character being sought, 0 when none is sought
	 * @return
	 * @throws GrammarException
	 */
	private static List<RuleFragment> parseBody(String body, int[] offset,
			char bracket) throws GrammarException {
		List<RuleFragment> parse = new LinkedList<RuleFragment>();
		GroupFragment gf = null;
		while (offset[0] < body.length()) {
			trimWhitespace(body, offset);
			if (offset[0] == body.length())
				break;
			char c = body.charAt(offset[0]);
			if (c == bracket) {
				offset[0]++;
				if (gf != null)
					gf.done();
				bracket = 0;
				break;
			} else if (c == '[') {
				offset[0]++;
				RepeatableRuleFragment r = new GroupFragment(parseBody(body,
						offset, ']'));
				Repetition rep = getRepetition(body, offset);
				r.setRepetition(rep);
				addFragment(parse, gf, r);
			} else if (c == '|') {
				if (gf == null) {
					gf = new GroupFragment(parse);
					parse.clear();
					parse.add(gf);
				} else
					gf.newSequence();
				offset[0]++;
			} else {
				RuleFragment r = nextRule(body, offset, bracket);
				if (r instanceof RepeatableRuleFragment) {
					Repetition rep = getRepetition(body, offset);
					((RepeatableRuleFragment) r).setRepetition(rep);
				}
				addFragment(parse, gf, r);
			}
		}
		if (bracket > 0)
			throw new GrammarException("could not find closing '" + bracket
					+ "' in " + body);
		if (parse.isEmpty())
			throw new GrammarException("empty rule body: " + body);
		return parse;
	}

	private static Repetition getRepetition(String body, int[] offset)
			throws GrammarException {
		Matcher m = repetitionPattern.matcher(body.substring(offset[0]));
		// necessarily matches because it will match the null string
		m.lookingAt();
		Repetition r = null;
		if (m.group().length() == 0)
			return Repetition.NONE;
		else {
			String base = m.group(1);
			String modifier = m.group(2);
			switch (base.charAt(0)) {
			case '*':
				if (modifier.equals(""))
					r = Repetition.ASTERISK;
				else if (modifier.equals("+"))
					r = Repetition.ASTERISK_P;
				else
					r = Repetition.ASTERISK_Q;
				break;
			case '+':
				if (modifier.equals(""))
					r = Repetition.PLUS;
				else if (modifier.equals("+"))
					r = Repetition.PLUS_P;
				else
					r = Repetition.PLUS_Q;
				break;
			case '?':
				if (modifier.equals(""))
					r = Repetition.QMARK;
				else if (modifier.equals("+"))
					r = Repetition.QMARK_P;
				else
					r = Repetition.QMARK_Q;
				break;
			case '{':
				// trim off curly brackets
				base = base.substring(1, base.length() - 1);
				if (base.length() == 0 || base.equals(","))
					throw new GrammarException("bad repetition modifier: {"
							+ base + '}');
				int index = base.indexOf(','),
				top,
				bottom;
				if (index == -1) {
					top = bottom = Integer.parseInt(base);
				} else if (index == 0) {
					bottom = 0;
					top = Integer.parseInt(base.substring(1));
				} else if (index == base.length() - 1) {
					bottom = Integer.parseInt(base.substring(0, index));
					top = Integer.MAX_VALUE;
				} else {
					bottom = Integer.parseInt(base.substring(0, index));
					top = Integer.parseInt(base.substring(index + 1));
				}
				Repetition.Type t;
				if (modifier.equals(""))
					t = Repetition.Type.greedy;
				else if (modifier.equals("+"))
					t = Repetition.Type.possessive;
				else
					t = Repetition.Type.stingy;
				r = new Repetition(t, bottom, top);
				break;
			default:
				throw new GrammarException("impossible repetition: "
						+ m.group());
			}
			offset[0] += m.group().length();
		}
		return r;
	}

	private static void addFragment(List<RuleFragment> parse, GroupFragment gf,
			RuleFragment r) {
		if (gf == null)
			parse.add(r);
		else
			gf.add(r);
	}

	private static RuleFragment nextRule(String body, int[] offset, char bracket)
			throws GrammarException {
		Matcher m = labelPattern.matcher(body.substring(offset[0]));
		if (m.lookingAt()) {
			String s = m.group();
			offset[0] += s.length();
			char c1 = s.charAt(0), c2 = s.charAt(s.length() - 1);
			if (c1 == '(' && c2 == ')') {
				String id = s.substring(1, s.length() - 1);
				return new Label(Label.Type.terminal, id);
			} else if (c1 == '<' && c2 == '>') {
				String id = s.substring(1, s.length() - 1);
				if (id.equals(Label.ROOT))
					return new Label(Label.Type.root, id);
				else
					return new Label(Label.Type.nonTerminal, id);
			} else
				throw new GrammarException("ill-formed rule identifier: " + s);
		} else
			throw new GrammarException("ill-formed rule: " + body);
	}

	/**
	 * Adjust offset to end of string or next non-whitespace character.
	 * 
	 * @param body
	 * @param offset
	 */
	private static void trimWhitespace(String body, int[] offset) {
		while (offset[0] < body.length()
				&& Character.isWhitespace(body.charAt(offset[0])))
			offset[0]++;
	}
}
