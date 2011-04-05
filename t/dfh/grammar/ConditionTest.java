package dfh.grammar;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests to see whether conditions are working to screen matches.
 * <p>
 * <b>Creation date:</b> Apr 5, 2011
 * 
 * @author David Houghton
 * 
 */
@SuppressWarnings("serial")
public class ConditionTest {

	@Test
	public void leafTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++\\b/ {less_than_100}",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				int i = Integer.parseInt(s.subSequence(m.start(), m.end())
						.toString());
				return i < 100;
			}
		});
		String s = "99 100 1000";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("only found value lower than 100", count == 1);
	}

	@Test
	public void leafRenameTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>",//
				"<a> = /\\b\\d++\\b/ {less_than_100}",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				int i = Integer.parseInt(s.subSequence(m.start(), m.end())
						.toString());
				return i < 100;
			}
		});
		String s = "99 100 1000";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("only found value lower than 100", count == 1);
	}

	@Test
	public void literalTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'foo' {whole}",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("whole", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				return (m.start() == 0 || !Character.isLetterOrDigit(s.charAt(m
						.start() - 1)))
						&& (m.end() == s.length() || !Character
								.isLetterOrDigit(s.charAt(m.end())));
			}
		});
		String s = "foo foot foo";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found only whole words", count == 2);
	}

	@Test
	public void alternationTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'foo' | 'bar' {whole}",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("whole", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				return (m.start() == 0 || !Character.isLetterOrDigit(s.charAt(m
						.start() - 1)))
						&& (m.end() == s.length() || !Character
								.isLetterOrDigit(s.charAt(m.end())));
			}
		});
		String s = "foo foot bar";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found only whole words", count == 2);
	}

	@Test
	public void asteriskQTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>*? {2}",//
				"<a> = 'a'",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				return m.end() - m.start() == 2;
			}
		});
		String s = "aaaaaa a";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found all the matches", count == 3);
	}

	@Test
	public void asteriskPTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>*+ {2}",//
				"<a> = 'a'",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				return m.end() - m.start() == 2
						&& (m.start() == 0 || !Character.isLetterOrDigit(s
								.charAt(m.start() - 1)));
			}
		});
		String s = "aaaaaa aa";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found the one good match", count == 1);
	}

	@Test
	public void plusQTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>+? {2}",//
				"<a> = 'a'",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				return m.end() - m.start() == 2;
			}
		});
		String s = "aaaaaa a";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found all the matches", count == 3);
	}

	@Test
	public void sequenceTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a'+ 'b' {2}",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence s) {
				return m.end() - m.start() == 2;
			}
		});
		String s = "aaaab";
		Matcher m = g.find(s);
		Match n = m.match();
		assertTrue("found right match", n.start() == 3);
	}

}
