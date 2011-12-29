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
			assertTrue("correct count", count == 1);
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void conditionTest() {
		String[] rules = {
				//
				"ROOT = !- [ <foo> /\\s++/r ] <foo> /(?!\\w)/",//
				"foo = '1' | <foo> '0' (lt100)",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineCondition("lt100", new IntegerCondition() {
				@Override
				public boolean passes(int i) {
					return i < 100;
				}
			});
			System.out.println(g.describe());
			String s = "100 10 1";
			Matcher m = g.find(s);
			int count = 0;
			Match n;
			while ((n = m.match()) != null) {
				count++;
				System.out.printf("%s -- %s%n", n, n.group());
			}
			assertTrue("correct count using condition", count == 1);
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

}
