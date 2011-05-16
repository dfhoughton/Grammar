package dfh.grammar;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Make sure we get exactly as many iterations as we expect.
 * <p>
 * <b>Creation date:</b> Mar 31, 2011
 * 
 * @author David Houghton
 * 
 */
public class IterationTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a'",//
		};
		Grammar g = new Grammar(rules);
		String s = "aaa";
		Matcher m = g.find(s);
		int count = 0;
		while (count < 10 && m.match() != null)
			count++;
		assertTrue(count == 3);
	}

	@Test
	public void overlapTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a'+",//
		};
		Grammar g = new Grammar(rules);
		String s = "aaa";
		Options opt = new Options();
		opt.longestTokenMatching(false);
		opt.allowOverlap(true);
		Matcher m = g.lookingAt(s, opt);
		int count = 0;
		while (count < 10 && m.match() != null)
			count++;
		assertTrue(count == 3);
	}

	@Test
	public void overlapTest2() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a'+",//
		};
		Grammar g = new Grammar(rules);
		String s = "aaa";
		Options opt = new Options();
		opt.allowOverlap(true);
		Matcher m = g.find(s, opt);
		int count = 0;
		while (count < 10 && m.match() != null)
			count++;
		assertTrue(count == 6);
	}

}
