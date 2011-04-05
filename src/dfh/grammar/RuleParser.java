package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dfh.grammar.Label.Type;

/**
 * Creates {@link Rule} objects from stringified specifications. This is a bunch
 * of stateless functional code that assists a {@link Compiler} object.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class RuleParser {
	/**
	 * Basic pattern of a labeled rule.
	 */
	public static final Pattern labelPattern = Pattern.compile("<(\\w++)>");
	/**
	 * Character class for characters in condition identifiers.
	 */
	public static final Pattern conditionLabelPattern = Pattern.compile("\\w");
	/**
	 * Pattern that defines a rule as "<"<name>">" "=" <remainder>
	 */
	public static final Pattern basePattern = Pattern.compile("\\s*+"
			+ labelPattern + "\\s*+=\\s*+(.*?)\\s*+");
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
	public static LinkedList<RuleFragment> parse(String line)
			throws GrammarException {
		if (line == null)
			throw new GrammarException("cannot parse nulls");
		if (ignorePattern.matcher(line).matches())
			return null;
		Matcher m = basePattern.matcher(line);
		if (m.matches()) {
			String id = m.group(1);
			String remainder = m.group(2);
			if (remainder.length() == 0)
				throw new GrammarException("no rule body provided in " + line);
			LinkedList<RuleFragment> parse = new LinkedList<RuleFragment>();
			Type t;
			if (id.equals(Label.ROOT)) {
				t = Type.root;
			} else
				t = Type.indeterminate;
			// we've parsed out the rule label
			parse.add(new Label(t, id));
			if (remainder.charAt(0) == '/') {
				int i1 = remainder.lastIndexOf('/'), i2 = remainder
						.lastIndexOf('{');
				if (i1 == 0)
					throw new GrammarException(
							"no terminal slash in regular expression "
									+ remainder);
				int i3 = remainder.indexOf('#', i1);
				if (i3 > i1) {
					remainder = remainder.substring(0, i3);
					if (i2 > i3)
						i2 = -1;
				}
				if (i2 > i1) {
					parse.add(new Regex(remainder.substring(0, i2)));
					int[] offset = { i2 };
					ConditionFragment c = getCondition(remainder, offset);
					parse.add(c);
				} else
					parse.add(new Regex(remainder));
			} else {
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
				GroupFragment r = new GroupFragment(
						parseBody(body, offset, ']'));
				Repetition rep = getRepetition(body, offset);
				if (rep.redundant() && r.alternates.size() == 1
						&& r.alternates.get(0).size() == 1) {
					if (gf == null)
						parse.addAll(r.alternates.get(0));
					else {
						for (RuleFragment rf : r.alternates.get(0))
							gf.add(rf);
					}
				} else {
					r.setRepetition(rep);
					add(parse, gf, r);
				}
			} else if (c == '|') {
				if (gf == null) {
					gf = new GroupFragment(parse);
					parse.clear();
					parse.add(gf);
				} else
					gf.newSequence();
				offset[0]++;
			} else if (c == '"' || c == '\'') {
				String literal = getLiteral(body, offset, c);
				RepeatableRuleFragment r = new LiteralFragment(literal);
				Repetition rep = getRepetition(body, offset);
				r.setRepetition(rep);
				add(parse, gf, r);
			} else if (c == '#') {
				break;
			} else if (Character.isDigit(c)) {
				int reference = getBackReference(body, offset);
				if (reference == 0)
					throw new GrammarException(
							"back references must be greater than 0");
				Repetition rep = getRepetition(body, offset);
				if (rep != Repetition.NONE)
					throw new GrammarException(
							"back reference cannot be modified with repetition suffixes");
				if (gf == null) {
					if (reference > parse.size())
						throw new GrammarException("back reference "
								+ reference + " is too big");
				} else {
					if (reference > gf.currentSequence.size())
						throw new GrammarException("back reference "
								+ reference + " is too big");
				}
				BackReferenceFragment brf = new BackReferenceFragment(reference);
				add(parse, gf, brf);
			} else if (c == '{') {
				if (parse.isEmpty())
					throw new GrammarException("condition without rule in "
							+ body);
				ConditionFragment cond = getCondition(body, offset);
				parse.add(cond);
				break;
			} else {
				RuleFragment r = nextRule(body, offset, bracket);
				if (r instanceof RepeatableRuleFragment) {
					Repetition rep = getRepetition(body, offset);
					((RepeatableRuleFragment) r).setRepetition(rep);
				}
				add(parse, gf, r);
			}
		}
		if (bracket > 0)
			throw new GrammarException("could not find closing '" + bracket
					+ "' in " + body);
		if (parse.isEmpty())
			throw new GrammarException("empty rule body: " + body);
		if (gf != null)
			gf.done();
		return parse;
	}

	private static int getBackReference(String body, int[] offset) {
		int start = offset[0];
		while (offset[0] < body.length()
				&& Character.isDigit(body.charAt(offset[0]))) {
			offset[0]++;
		}
		return Integer.parseInt(body.substring(start, offset[0]));
	}

	private static void add(List<RuleFragment> parse, GroupFragment gf,
			RuleFragment r) {
		if (gf == null)
			parse.add(r);
		else
			gf.add(r);
	}

	/**
	 * @param body
	 * @param offset
	 * @return quote delimited String literal
	 */
	private static String getLiteral(String body, int[] offset, char delimiter) {
		boolean escaped = false;
		int start = offset[0] + 1;
		boolean found = false;
		while (offset[0] + 1 < body.length()) {
			offset[0]++;
			char c = body.charAt(offset[0]);
			if (escaped)
				escaped = false;
			else if (c == '\\')
				escaped = true;
			else if (c == delimiter) {
				found = true;
				break;
			}
		}
		if (!found)
			throw new GrammarException("could not find closing \" in " + body);
		String s = body.substring(start, offset[0]);
		offset[0]++;
		return s;
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

	private static RuleFragment nextRule(String body, int[] offset, char bracket)
			throws GrammarException {
		Matcher m = labelPattern.matcher(body.substring(offset[0]));
		if (m.lookingAt()) {
			offset[0] += m.end();
			String id = m.group(1);
			if (id.equals(Label.ROOT))
				return new Label(Label.Type.root, id);
			else
				return new Label(Label.Type.indeterminate, id);
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

	private static ConditionFragment getCondition(String body, int[] offset)
			throws GrammarException {
		offset[0]++;
		int start = offset[0];
		Matcher m = conditionLabelPattern.matcher(body);
		while (offset[0] < body.length()) {
			if (body.charAt(offset[0]) == '}')
				break;
			m.region(offset[0], offset[0] + 1);
			if (m.matches())
				offset[0]++;
			else
				throw new GrammarException("ill-formed condition at end of "
						+ body);
		}
		if (start == offset[0])
			throw new GrammarException("zero-width condition identifier in "
					+ body);
		int end = offset[0]++;
		@SuppressWarnings("unused")
		String s = body.substring(start, end);
		trimWhitespace(body, offset);
		if (offset[0] < body.length() && body.charAt(offset[0]) != '#')
			throw new GrammarException(
					"no content other than a comment permitted after a condition: "
							+ body);
		return new ConditionFragment(body.substring(start, end));
	}
}
