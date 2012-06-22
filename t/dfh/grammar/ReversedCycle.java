package dfh.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

@SuppressWarnings("serial")
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

	@Test
	public void conditionTest1() {
		String[] rules = {
				//
				"ROOT = not after [ <bar> /\\s++/r ] <bar>",//
				"bar = /\\b/r <foo> /\\b/r (lt100)",//
				"foo = '1' | <foo> '0'",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineCondition("lt100", new IntegerCondition() {
				@Override
				public boolean passes(int i) {
					return i < 100;
				}
			});
			String s = "100 10 1";
			Matcher m = g.find(s);
			int count = 0;
			while (m.match() != null)
				count++;
			assertEquals("correct count using condition", 1, count);
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

	@Test
	public void conditionTest2() {
		String[] rules = {
				//
				"ROOT = not after [ <bar> /\\s++/r ] <bar>",//
				"bar = /\\b/r <foo> /\\b/r (lt100 gt0)",//
				"foo = '1' | <foo> '0'",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineCondition("lt100", new IntegerCondition() {
				@Override
				public boolean passes(int i) {
					return i < 100;
				}
			});
			g.defineCondition("gt0", new IntegerCondition() {
				@Override
				public boolean passes(int i) {
					return i > 0;
				}
			});
			String s = "100 10 1";
			Matcher m = g.find(s);
			int count = 0;
			while (m.match() != null)
				count++;
			assertEquals("correct count using condition", 1, count);
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

}
