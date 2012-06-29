package dfh.grammar;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
		String[] rules = { "foo ::= not after <bar> <quux>",//
				"bar ::= 't' 'c'",//
				"quux ::= 't' 'd'",//
		};
		Grammar g = new Grammar(rules);
		PrintStream ps = new PrintStream(new FileOutputStream(new File("/tmp/debugging.txt")));
		Matcher m = g.find("t c t d t d", new Options().log(ps));
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
