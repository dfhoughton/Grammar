package dfh.grammar;

import static org.junit.Assert.*;

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
}