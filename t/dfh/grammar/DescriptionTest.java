package dfh.grammar;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

public class DescriptionTest {

	@Test
	public void basic() {
		String[] rules = {
		//
		"ROOT = 'foo'" };
		Grammar g = new Grammar(rules);
		assertTrue(
				"basic description",
				Pattern.compile("\\s*+ROOT = \"foo\"\\s*+")
						.matcher(g.describe()).matches());
	}

	@Test
	public void tagAll() {
		String[] rules = {
		//
		"ROOT = [{bar} 'foo']" };
		Grammar g = new Grammar(rules);
		assertTrue(
				"basic description",
				Pattern.compile(
						"\\s*+ROOT = \\[\\{bar\\}\\s*+\"foo\"\\s*+\\]\\s*+")
						.matcher(g.describe()).matches());
	}

	@Test
	public void alternation() {
		String[] rules = {
		//
		"ROOT = 'foo' | [{bar} 'quux']" };
		Grammar g = new Grammar(rules);
		assertTrue(
				"basic description",
				Pattern.compile(
						"\\s*+ROOT = \"foo\" \\| \\[\\{bar\\} \"quux\" ]\\s*+")
						.matcher(g.describe()).matches());
	}

	@Test
	public void sequence() {
		String[] rules = {
		//
		"ROOT = 'foo' [{bar} 'quux']" };
		Grammar g = new Grammar(rules);
		assertTrue(
				"basic description",
				Pattern.compile(
						"\\s*+ROOT = \"foo\" \\[\\{bar\\} \"quux\" ]\\s*+")
						.matcher(g.describe()).matches());
	}

	@Test
	public void repetition() {
		String[] rules = {
		//
		"ROOT = [{bar} 'foo']+" };
		Grammar g = new Grammar(rules);
		assertTrue("basic description",
				Pattern.compile("\\s*+ROOT = \\[\\{bar\\} \"foo\" ]\\+\\s*+")
						.matcher(g.describe()).matches());
	}

	@Test
	public void cyclesAndReversedRule() {
		Grammar g = new Grammar(
				new String[] {
						//
						"ROOT = <parens> | <square> | <curly> | <angled>",//
						"parens = not after <escape> '(' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ ')'",//
						"square = not after <escape> '[' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ ']'",//
						"curly = not after <escape> '{' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ '}'",//
						"angled = not after <escape> '<' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ '>'",//
						"escape = /(?<!\\\\)(?:\\\\)++/r (odd)",//
				});
		g.defineCondition("odd", new Condition() {
			@Override
			public boolean passes(CharSequence subsequence) {
				return subsequence.length() % 2 == 1;
			}
		});
		String d = g.describe();
		for (String s : new String[] { "ROOT", "parens", "square", "curly",
				"angled", "escape" }) {
			Pattern pattern = Pattern.compile("^\\s*+" + s, Pattern.MULTILINE);
			assertTrue("contains rule " + s, pattern.matcher(d).find());
		}
	}

	@Test
	public void benchmarkRegression() {
		String[] rules = {
				//
				"<ROOT> = [ <a> | <b> ]{2} <b>",//
				"<a> = 'a'{,2}",//
				"<b> = 'ab'",//
		};
		Grammar g = new Grammar(rules);
		String d = g.describe();
		for (String s : new String[] { "ROOT", "a", "b" }) {
			Pattern pattern = Pattern.compile("^\\s*+" + s, Pattern.MULTILINE);
			assertTrue("contains rule " + s, pattern.matcher(d).find());
		}
	}
}
