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
				"<ROOT> = (joe)",//
				"(joe) =\\bjoe\\b",//
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
				"<ROOT> = (not_susie) (s)?+ (joe)",//
				"(joe) =\\b(?i:joe)\\b",//
				"(not_susie) =(?<!(?i:susie))",//
				"(s) =\\s++",//
		};
		Grammar g = new Grammar(rules);
		String s = "  mary joe susie joe ";
		Matcher m = g.find(s);
		int count = 0;
		Match n;
		while ((n = m.match()) != null) {
			System.out.println(n);
			System.out.println(s.substring(n.start(), n.end()));
			count++;
		}
		assertTrue("found joe", count == 1);
	}
}
