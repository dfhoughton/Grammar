package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

public class LiteralTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = \"joe\"",//
		};
		Grammar g = new Grammar(rules);
		String s = "la la joe la la";
		Matcher m = g.find(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("found joe", n);
	}

	@Test
	public void nullTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = \"joe\"",//
		};
		Grammar g = new Grammar(rules);
		String s = "la la la la";
		Matcher m = g.find(s);
		Match n = m.match();
		System.out.println(n);
		assertNull("didn't find non-joe", n);
	}

	@Test
	public void repetitionTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = \"joe\"{2}",//
		};
		Grammar g = new Grammar(rules);
		String s = "la la joejoe la la";
		Matcher m = g.find(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("found double joe", n);
	}

	@Test
	public void singleQuoteTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'joe'{2}",//
		};
		Grammar g = new Grammar(rules);
		String s = "la la joejoe la la";
		Matcher m = g.find(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("single quotes worked", n);
	}
}
