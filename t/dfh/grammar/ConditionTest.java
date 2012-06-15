package dfh.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	public void leafTest1() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++\\b/ (less_than_100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
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
				"<a> = /\\b\\d++\\b/ (less_than_100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
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
		"<ROOT> = 'foo' (whole)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("whole", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
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
		"<ROOT> = 'foo' | 'bar' (whole)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("whole", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
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
				"<ROOT> = <a>*? (2)",//
				"<a> = 'a'",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
				return m.end() - m.start() == 2;
			}
		});
		String s = "aaaaaa a";
		Options opt = new Options();
		opt.longestMatch(false);
		Matcher m = g.find(s, opt);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found all the matches", count == 3);
	}

	@Test
	public void asteriskPTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>*+ (2)",//
				"<a> = 'a'",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
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
				"<ROOT> = <a>+? (2)",//
				"<a> = 'a'",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
				return m.end() - m.start() == 2;
			}
		});
		String s = "aaaaaa a";
		Options opt = new Options();
		opt.longestMatch(false);
		Matcher m = g.find(s, opt);
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals("found all the matches", 3, count);
	}

	@Test
	public void sequenceTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a'+ 'b' (2)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("2", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
				return m.end() - m.start() == 2;
			}
		});
		String s = "aaaab";
		Matcher m = g.find(s);
		Match n = m.match();
		assertTrue("found right match", n.start() == 3);
	}

	@Test
	public void assignmentTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		String s = "1 10 100";
		Matcher m = g.find(s);
		int count1 = 0;
		while (m.match() != null)
			count1++;
		g.assignCondition("ROOT", "less_than_100", new Condition() {

			@Override
			public boolean passes(Match n, Matcher m, CharSequence s) {
				int i = Integer.parseInt(s.subSequence(n.start(), n.end())
						.toString());
				return i < 100;
			}
		});
		m = g.find(s);
		int count2 = 0;
		while (m.match() != null)
			count2++;
		assertTrue("changed match count", count1 == 3 && count2 == 2);
	}

	@Test
	public void leafTest2() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++\\b/ (less_than_100)",//
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
	public void leafTest3() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++\\b/ (less_than_100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			@Override
			public boolean passes(CharSequence s) {
				int i = Integer.parseInt(s.toString());
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
	public void integerConditionTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++\\b/ (less_than_100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new IntegerCondition() {
			@Override
			public boolean passes(int i) {
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
	public void doubleConditionTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++(?:\\.\\d++)?\\b/ (less_than_100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new FloatingPointCondition() {
			@Override
			public boolean passes(double d) {
				return d < 100;
			}
		});
		String s = "9.9 100 1000";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("only found value lower than 100", count == 1);
	}

	@Test
	public void reversedConditionTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = ~- <b> 'foo' ~ <b>",//
				"<b> = /(?<!\\d)\\d++(?!\\d)/r (range_5_to_10)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("range_5_to_10", new IntegerCondition() {
			@Override
			public boolean passes(int i) {
				return i >= 5 && i <= 10;
			}
		});
		String s = "10foo10 4foo11";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 1);
	}

	@Test
	public void doubleReversedConditionTest() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ~- <b> 'foo'",//
				"<b> = ~- <a> 'bar'",//
				"<a> = /(?<!\\d)\\d++(?!\\d)/r (range_5_to_10)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("range_5_to_10", new IntegerCondition() {
			@Override
			public boolean passes(int i) {
				return i >= 5 && i <= 10;
			}
		});
		String s = "5barfoo 11barfoo";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals("found match", 1, count);
	}

	@Test
	public void conditionError1() throws IOException {
		String[] rules = {
		//
		"<ROOT> = 'foo' (con*dition)",//
		};
		try {
			new Grammar(rules);
			fail("should have throw exception");
		} catch (GrammarException e) {
		}
	}

	@Test
	public void conditionError2() throws IOException {
		String[] rules = {
		//
		"<ROOT> = 'foo' (con()dition)",//
		};
		try {
			new Grammar(rules);
			fail("should have throw exception");
		} catch (GrammarException e) {
		}
	}

	@Test
	public void conditionError3() throws IOException {
		String[] rules = {
		//
		"<ROOT> = 'foo' (con||dition)",//
		};
		try {
			new Grammar(rules);
			fail("should have throw exception");
		} catch (GrammarException e) {
		}
	}

}
