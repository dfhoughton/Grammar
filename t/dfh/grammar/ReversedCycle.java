package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.Test;

public class ReversedCycle {

	@Test
	public void test() {
		String[] rules = {
				//
				"ROOT = !- <foo> <foo>",//
				"foo = 'a' | <foo> 'b'",//
		};
		try {
			Grammar g = new Grammar(rules);
			String s = "abab";
			Matcher m = g.find(s);
			int count = 0;
			while (m.match() != null)
				count++;
			assertTrue("", count == 1);
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

}
