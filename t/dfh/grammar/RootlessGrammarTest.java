package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Test;

import dfh.grammar.Label.Type;

/**
 * For testing the transition away from the requirement that the root rule be
 * called ROOT.
 * <p>
 * 
 * @author David F. Houghton - Mar 19, 2012
 * 
 */
public class RootlessGrammarTest {

	@Test
	public void test() {
		new Grammar("foo = 'a'");
	}

	@Test
	public void doubleForwardAssertionTest() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<foo> = 'foo' ~<b>",//
				"<b> = 'bar' ~<a>",//
				"<a> = /(?<!\\d)\\d++(?!\\d)/r",//
		};
		Grammar g = new Grammar(rules);
		String s = "foobar5 foobar5";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 2);
	}

	@Test
	public void backref() throws GrammarException, IOException {
		String[] rules = {
				//
				"br = <q> <text> 1",//
				"<q> = /[\"']/",//
				"<text> = /\\w++/",//
		};
		Grammar g = new Grammar(rules);
		String s = "'ned'";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNotNull("found ned", n);
	}

	@Test
	public void doubleColonMatchesTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"foo = <a> | <b>",//
				"<a> = :: 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		assertNull("couldn't shrink initial match", g.matches("b").match());
	}

	@SuppressWarnings("serial")
	@Test
	public void simpleConjunction() {
		String[] rules = {
		//
		"foo = /\\d++/ (lt100 gt10)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("lt100", new IntegerCondition() {

			@Override
			public boolean passes(int i) {
				return i < 100;
			}
		});
		g.defineCondition("gt10", new IntegerCondition() {

			@Override
			public boolean passes(int i) {
				return i > 10;
			}
		});
		assertNotNull("simple conjunction", g.matches("12").match());
	}

	@Test
	public void test3NullAlternation() throws GrammarException, IOException {
		String[] rulesAlternation = {
				//
				"<AP> = <DP> 'a' | 'a'",//
				"<DP> = <AP> 's'",//
		};
		Grammar gAlternation = new Grammar(rulesAlternation);
		String s = "asasasasa";
		Matcher m = gAlternation.matches(s);
		Match n = m.match();
		assertNull(n);
	}

	@Test
	public void arbitraryRuleTest() throws GrammarException, IOException {

		String[] rules = {
		//
		"foo = <q> <text> 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineRule("q", Pattern.compile("[\"']"));
		g.defineRule("text", new DeferredRuleTest.ARule(new Label(
				Type.explicit, "text")));
		String s = "'ned'";
		Options options = new Options();
		options.study(false);
		Matcher m = g.find(s, options);
		Match n = m.match();
		assertNotNull("found ned", n);
	}

	@Test
	public void description() {
		Grammar g = new Grammar(
				new String[] {
						//
						"foo = <parens> | <square> | <curly> | <angled>",//
						"parens = not after <escape> '(' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <foo> ]*+ ')'",//
						"square = not after <escape> '[' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <foo> ]*+ ']'",//
						"curly = not after <escape> '{' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <foo> ]*+ '}'",//
						"angled = not after <escape> '<' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <foo> ]*+ '>'",//
						"escape = /(?<!\\\\)(?:\\\\)++/r (odd)",//
				});
		g.defineCondition("odd", new Condition() {
			@Override
			public boolean passes(CharSequence subsequence) {
				return subsequence.length() % 2 == 1;
			}
		});
		String d = g.describe();
		for (String s : new String[] { "foo", "parens", "square", "curly",
				"angled", "escape" }) {
			Pattern pattern = Pattern.compile("^\\s*+" + s, Pattern.MULTILINE);
			assertTrue("contains rule " + s, pattern.matcher(d).find());
		}
	}

	@Test
	public void recursion() {
		String[] rules = {
				//
				"<element> = <single> | <double>",//
				"<single> = '<' <tag> [ <s> <attribute> ]* '/>'",//
				"<double> = '<' <tag> [ <s> <attribute> ]* '>' <element>* '</' 2 '>'",//
				"<attribute> = <tag> '=\"' <content>++ '\"'",//
				"<content> = <nq> | <esc>",//
				"<nq> = /[^\"]/",//
				"<esc> = /\\\\./",//
				"<tag> = /\\b\\w++\\b/",//
				"<s> = /\\s++/",//
		};
		Grammar g = new Grammar(rules);
		String s = "<test/>";
		Matcher m = g.matches(s);
		Match n = m.match();
		assertNotNull("parsed minimal document", n);
		s = "<test><foo/></test>";
		m = g.matches(s);
		n = m.match();
		assertNotNull("parsed minimal document", n);
		s = "<test><foo jolly=\"roger\"/></test>";
		m = g.matches(s);
		n = m.match();
		assertNotNull("parsed minimal document", n);
		s = "<test><foo jolly=\"roger\"/></ROO>";
		m = g.matches(s);
		n = m.match();
		assertNull("parsed minimal document", n);
	}
	
	@Test
	public void conditionOnRoot() {
		String[] rules = {
				//
				"emoticon = [ <lr> | <rl> ] !-/.?::/r (well_bounded)",//
				"      lr = <hat>? <lreyes> <nose>? <lrmouth>",//
				"      rl = <rlmouth> <nose>? <eyes> <hat>?",//
				"    eyes = [{een} /[:;=8]/ ]",//
				"  lreyes = [{een} <eyes> | 'B' ]",//
				"     hat = /[<>]/",//
				" lrmouth = [{mou} <mouth> | '>' ]",//
				" rlmouth = [{mou} <mouth> | '<' ]",//
				"   mouth = /[()\\[\\]dDpP\\/:{}\\\\@|]/ | /[()]{2}/",//
				"    nose = /[o*'^v\"-]/",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("well_bounded", new Condition() {
			@Override
			public boolean passes(Match n, CharSequence s) {
				boolean wellBounded = true;
				if (n.start() > 0
						&& Character.isLetterOrDigit(s.charAt(n.start()))) {
					wellBounded &= !Character.isLetterOrDigit(s.charAt(n
							.start() - 1));
				}
				if (wellBounded && n.end() < s.length()
						&& Character.isLetterOrDigit(s.charAt(n.end() - 1))) {
					wellBounded &= !Character.isLetterOrDigit(s.charAt(n
							.end()));
				}
				return wellBounded;
			}
		});
	}

}
