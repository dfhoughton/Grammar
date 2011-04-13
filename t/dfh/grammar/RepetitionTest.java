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
				"<ROOT> = <a>?",//
				"<a> = /a/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertTrue("found first instance", n.end() == 1);
	}

	@Test
	public void qmarkTestPossessive() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>?+",//
				"<a> = /a/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertTrue("found first instance", n.end() == 1);
	}

	@Test
	public void qmarkTestStingy() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>??",//
				"<a> = /a/",//
		};
		Grammar g = new Grammar(rules);
		Options opt = new Options();
		opt.longestTokenMatching(false);
		String s = "aa";
		Matcher m = g.lookingAt(s, opt);
		Match n = m.match();
		assertTrue("found first instance", n.end() == 0);
	}

	@Test
	public void asteriskTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>*",//
				"<a> = /a/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertTrue("found all asterisks", n.end() == 2);
	}

	@Test
	public void plusTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>+",//
				"<a> = /a/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertTrue("found all plusses", n.end() == 2);
	}

	@Test
	public void possessivenessTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <c> <d>",//
				"<c> = <a>++",//
				"<d> = <a> <b>",//
				"<a> = /a/",//
				"<b> = /b/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertNull("possessiveness caused match to fail", n);
	}

	@Test
	public void greedTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <c> <d>",//
				"<c> = <a>+",//
				"<d> = <a> <b>",//
				"<a> = /a/",//
				"<b> = /b/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertNotNull("greedy asterisk allowed backtracking", n);
	}

	@Test
	public void countTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>{3}",//
				"<a> = /a/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aaaaa";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertTrue("matched an exact number", n.end() == 3);
	}

	@Test
	public void noLowerLimitCountTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <c> <d>",//
				"<c> = <a>{,2}",//
				"<d> = <a> <b>",//
				"<a> = /a/",//
				"<b> = /b/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertNotNull("{,2} worked", n);
	}

	@Test
	public void subgroupTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = [ <c> | <d> ]{2} <d>",//
				"<c> = <a>{,2}",//
				"<d> = <a> <b>",//
				"<a> = /a/",//
				"<b> = /b/",//
		};
		Grammar g = new Grammar(rules);
		String s = "aabb";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertNotNull("[]{2} worked", n);
	}

	@Test
	public void stingyThenGreedy() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'ab'+? 'ab'+",//
		};
		Grammar g = new Grammar(rules);
		String s = "ababab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertTrue("stingy is short", n.children()[0].end() == 2);
	}

	@Test
	public void greedyThenStingy() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'ab'+ 'ab'+?",//
		};
		Grammar g = new Grammar(rules);
		String s = "ababab";
		Matcher m = g.lookingAt(s);
		Match n = m.match();
		assertTrue("stingy is short", n.children()[0].end() == 4);
	}

	@Test
	public void plusAsterisk() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>*+ <b>",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		String s = "b";
		Matcher m = g.matches(s);
		Match n = m.match();
		assertNotNull("plus asterisk matches zero-width", n);
	}

	@Test
	public void nullAsterisk() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>* <b>",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		String s = "b";
		Matcher m = g.matches(s);
		Match n = m.match();
		assertNotNull("asterisk matches zero-width", n);
	}

}
