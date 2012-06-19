package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

public class RegexTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /joe/",//
		};
		Grammar g = new Grammar(rules);
		String s = "la la joe la la";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNotNull("found joe", n);
	}

	@Test
	public void nullTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /joe/",//
		};
		Grammar g = new Grammar(rules);
		String s = "la la la la";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNull("didn't find non-joe", n);
	}

	@Test
	public void repetitionTest1() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /joe/{2}",//
		};
		Grammar g = new Grammar(rules);
		String s = "la la joejoe la la";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNotNull("found double joe", n);
	}

	@Test
	public void repetitionTest2() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /joe/?",//
		};
		Grammar g = new Grammar(rules);
		String s = "";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNotNull("found empty joe", n);
	}
}
