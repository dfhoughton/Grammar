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
		new Grammar("rule ::= 'the' 'cat'");
	}

	@Test
	public void compilationMaybe() {
		new Grammar("rule := 'the' 'cat'");
	}

	@Test
	public void maybe1() {
		Grammar g = new Grammar("rule := 'the' 'cat'");
		Matcher m = g.matches("the cat");
		Match n = m.match();
		assertNotNull(n);
		assertEquals("the cat", n.group());
	}

	@Test
	public void maybe2() {
		Grammar g = new Grammar("rule := 'the' 'cat'");
		Matcher m = g.matches("thecat");
		Match n = m.match();
		assertNotNull(n);
		assertEquals("thecat", n.group());
	}

	@Test
	public void required1() {
		Grammar g = new Grammar("rule ::= 'the' 'cat'");
		Matcher m = g.matches("the cat");
		Match n = m.match();
		assertNotNull(n);
		assertEquals("the cat", n.group());
	}

	@Test
	public void required2() {
		Grammar g = new Grammar("rule ::= 'the' 'cat'");
		Matcher m = g.matches("thecat");
		Match n = m.match();
		assertNull(n);
	}

	@Test
	public void reversal1() throws FileNotFoundException {
		String[] rules = { "lone_t_d ::= not after <t_c> <t_d>",//
				"t_c ::= 't' 'c'",//
				"t_d ::= 't' 'd'",//
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
		String[] rules = { "foo ::= not after <bar> <quux>",//
				"bar ::= 'the' 'cat'",//
				"quux ::= 'the' 'dog'",//
		};
		Grammar g = new Grammar(rules);
		String s = g.describe();
		assertTrue(s.indexOf(".s") == -1);
	}

}
