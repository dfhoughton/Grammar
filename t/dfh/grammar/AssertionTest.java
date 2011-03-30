package dfh.grammar;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Makes sure zero-width assertions work as advertised.
 * <p>
 * <b>Creation date:</b> Mar 28, 2011
 * 
 * @author David Houghton
 * 
 */
public class AssertionTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <joe>",//
				"<joe> = /\\bjoe\\b/",//
		};
		Grammar g = new Grammar(rules);
		String s = "  joe _joe_ ";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found joe", count == 1);
	}

	@Test
	public void backwardsTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <not_space> <joe>",//
				"<joe> = /joe/i",//
				"<not_space> = /(?<!\\s)/i",//
		};
		Grammar g = new Grammar(rules);
		String s = "  maryjoe susie joe ";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found joe", count == 1);
	}

	@Test
	public void forwardTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <joe> <s>?+ <is_sue>",//
				"<joe> = /\\bjoe\\b/i",//
				"<is_sue> = /(?=sue)/i",//
				"<s> = /\\s++/",//
		};
		Grammar g = new Grammar(rules);
		String s = " joe mary joe sue ";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found joe", count == 1);
	}
}
