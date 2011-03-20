package dfh.grammar;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RepetitionTest {

	@Test
	public void qmarkTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (a)?",//
				"(a) =a",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertTrue("found first instance", n.end() == 1);
	}

	@Test
	public void qmarkTestPossessive() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (a)?+",//
				"(a) =a",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertTrue("found first instance", n.end() == 1);
	}

	@Test
	public void qmarkTestStingy() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (a)??",//
				"(a) =a",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertTrue("found first instance", n.end() == 0);
	}
	
	@Test
	public void asteriskTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (a)*",//
				"(a) =a",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertTrue("found all asterisks", n.end() == 2);
	}
}
