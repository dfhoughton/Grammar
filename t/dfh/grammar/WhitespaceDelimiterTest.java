package dfh.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;

public class WhitespaceDelimiterTest {

	@Test
	public void compilationRequires() {
		new Grammar("rule := 'the' 'cat'");
	}

	@Test
	public void compilationMaybe() {
		new Grammar("rule .= 'the' 'cat'");
	}

	@Test
	public void maybe1() {
		Grammar g = new Grammar("rule .= 'the' 'cat'");
		Matcher m = g.matches("the cat");
		Match n = m.match();
		assertNotNull(n);
		assertEquals("the cat", n.group());
	}

	@Test
	public void maybe2() {
		Grammar g = new Grammar("rule .= 'the' 'cat'");
		Matcher m = g.matches("thecat");
		Match n = m.match();
		assertNotNull(n);
		assertEquals("thecat", n.group());
	}

	@Test
	public void required1() {
		Grammar g = new Grammar("rule := 'the' 'cat'");
		Matcher m = g.matches("the cat");
		Match n = m.match();
		assertNotNull(n);
		assertEquals("the cat", n.group());
	}

	@Test
	public void required2() {
		Grammar g = new Grammar("rule := 'the' 'cat'");
		Matcher m = g.matches("thecat");
		Match n = m.match();
		assertNull(n);
	}

	@Test
	public void reversal1() throws FileNotFoundException {
		String[] rules = { "lone_t_d := not after <t_c> <t_d>",//
				"t_c := 't' 'c'",//
				"t_d := 't' 'd'",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("t c t d t d");
		Match n = m.match();
		assertNotNull(n);
		int count = 1;
		while ((n = m.match()) != null)
			count++;
		assertEquals(1, count);
	}

	@Test
	public void description1() {
		String[] rules = { "foo := not after <bar> <quux>",//
				"bar := 'the' 'cat'",//
				"quux := 'the' 'dog'",//
		};
		Grammar g = new Grammar(rules);
		String s = g.describe();
		assertTrue(s.indexOf(".s") == -1);
	}

	@SuppressWarnings("serial")
	@Test
	public void condition() {
		Grammar g = new Grammar("rule := 'a' /\\d++/ 'b' (lt10)");
		g.defineCondition("lt10", new Condition() {
			@Override
			public boolean passes(Match m, CharSequence c) {
				Match[] sequence = nonconditionalMatch(m).children();
				Match num = sequence[2];
				Integer i = Integer.parseInt(c.subSequence(num.start(),
						num.end()).toString());
				return i < 10;
			}
		});
		Matcher m = g.find(" a 100 b a 1 b ");
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(1, count);
	}

	@Test
	public void dot1() {
		Grammar g = new Grammar("rule := 'a' [ . 'b' ]+");
		String s = "a b b";
		Match n = g.matches(s).match();
		assertNotNull(n);
	}

	@Test
	public void dot2() {
		Grammar g = new Grammar("rule := 'a' [ . 'b' 'c' ]+");
		String s = "a b c b c";
		Match n = g.matches(s).match();
		assertNotNull(n);
	}

	@Test
	public void dot3() {
		Grammar g = new Grammar("rule := 'a' [ . 'b' . 'c' ]+");
		String s = "a b c b c";
		Match n = g.matches(s).match();
		assertNotNull(n);
	}

	@Test
	public void dotDescription() {
		Grammar g = new Grammar("rule := 'a' [ . 'b' 'c' ]+");
		String s = g.describe();
		assertTrue(s.indexOf('.') > -1);
	}

	@Test
	public void droppingMarginalSpace1() {
		Grammar g = new Grammar("rule .= 'a'? 'b'");
		Matcher m = g.find("a b", new Options().allowOverlap(true));
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(2, count);
	}

	@Test
	public void droppingMarginalSpace2() {
		Grammar g = new Grammar("rule := 'a'? 'b'");
		Matcher m = g.find("a b", new Options().allowOverlap(true));
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(2, count);
	}

	@Test
	public void droppingMarginalSpace3() {
		Grammar g = new Grammar("rule .= 'a' 'b'?");
		Matcher m = g.find("a b", new Options().allowOverlap(true));
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(2, count);
	}

	@Test
	public void droppingMarginalSpace4() {
		Grammar g = new Grammar("rule := 'a' 'b'?");
		Matcher m = g.find("a b", new Options().allowOverlap(true));
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(2, count);
	}
}
