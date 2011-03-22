package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

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

	@Test
	public void plusTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (a)+",//
				"(a) =a",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertTrue("found all plusses", n.end() == 2);
	}

	@Test
	public void possessivenessTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> <b>",//
				"<a> = (a)++",//
				"<b> = (a) (b)",//
				"(a) =a",//
				"(b) =b",//
		};
		Grammar g = new Grammar(rules);
		String s = "aab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertNull("possessiveness caused match to fail", n);
	}

	@Test
	public void greedTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> <b>",//
				"<a> = (a)+",//
				"<b> = (a) (b)",//
				"(a) =a",//
				"(b) =b",//
		};
		Grammar g = new Grammar(rules);
		String s = "aab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("greedy asterisk allowed backtracking", n);
	}

	@Test
	public void countTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (a){3}",//
				"(a) =a",//
		};
		Grammar g = new Grammar(rules);
		String s = "aaaaa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertTrue("matched an exact number", n.end() == 3);
	}

	@Test
	public void noLowerLimitCountTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> <b>",//
				"<a> = (a){,2}",//
				"<b> = (a) (b)",//
				"(a) =a",//
				"(b) =b",//
		};
		Grammar g = new Grammar(rules);
		String s = "aab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("{,2} worked", n);
	}

	@Test
	public void subgroupTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = [ <a> | <b> ]{2} <b>",//
				"<a> = (a){,2}",//
				"<b> = (a) (b)",//
				"(a) =a",//
				"(b) =b",//
		};
		Grammar g = new Grammar(rules);
		String s = "aabb";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("[]{2} worked", n);
	}

}
