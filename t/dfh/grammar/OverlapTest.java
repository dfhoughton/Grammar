package dfh.grammar;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Makes sure all overlapping patterns are being captured.
 * 
 * <b>Creation date:</b> May 16, 2011
 * 
 * @author David Houghton
 * 
 */
public class OverlapTest {

	@Test
	public void basicTest1() throws GrammarException, IOException {
		String[] rules1 = {
				//
				"<ROOT> = <NC>",//
				"<NC> = [<N> <s>]* <N>",//
				"<s> = /\\s++/r",//
				"<N> = 'a' | 'b'",//
		};
		Grammar g = new Grammar(rules1);
		String s = "a b";
		Options opt = new Options();
		opt.allowOverlap(true);
		opt.longestTokenMatching(false);
		Matcher m = g.find(s, opt);
		@SuppressWarnings("unused")
		Match n;
		int count = 0;
		while ((n = m.match()) != null) {
			count++;
			// System.out.println(s.substring(n.start(), n.end()));
		}
		assertTrue(count == 3);
	}
}
