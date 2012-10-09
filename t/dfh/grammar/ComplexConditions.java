package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the parsing and application of complex conditions such as
 * (greater_than_10 | less_than_0).
 * <p>
 * <b>Creation date:</b> Dec 8, 2011
 * 
 * @author David Houghton
 * 
 */
@SuppressWarnings("serial")
public class ComplexConditions {
	private static final IntegerCondition lt100 = new IntegerCondition() {
		@Override
		public boolean passes(int i) {
			return i < 100;
		}
	};
	private static final IntegerCondition gt10 = new IntegerCondition() {
		@Override
		public boolean passes(int i) {
			return i > 10;
		}
	};
	private static final IntegerCondition eq5 = new IntegerCondition() {
		@Override
		public boolean passes(int i) {
			return i == 5;
		}
	};

	@Test
	public void simpleConjunction() {
		String[] rules = {
		//
		"ROOT = /\\d++/ (lt100 gt10)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("lt100", lt100);
		g.defineCondition("gt10", gt10);
		assertNotNull("simple conjunction", g.matches("12").match());
	}

	@Test
	public void simpleDisjunction() {
		String[] rules = {
		//
		"ROOT = /\\d++/ (gt10 | eq5)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("eq5", eq5);
		g.defineCondition("gt10", gt10);
		assertNotNull("simple disjunction", g.matches("5").match());
	}

	@Test
	public void simpleXor() {
		String[] rules = {
		//
		"ROOT = /\\d++/ (gt10 ^ eq5)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("eq5", eq5);
		g.defineCondition("gt10", gt10);
		assertNotNull("simple xor", g.matches("5").match());
	}

	@Test
	public void simpleXor2() {
		String[] rules = {
		//
		"ROOT = /\\d++/ (eq5 ^ eq5)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("eq5", eq5);
		assertNull("simple xor false", g.matches("5").match());
	}

	@Test
	public void complexXor1() {
		String[] rules = {
		//
		"ROOT = /\\d++/ (eq5 ^ eq5 ^ lt100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("eq5", eq5);
		g.defineCondition("lt100", lt100);
		assertNull("complex xor acts like exists-one", g.matches("5").match());
	}

	@Test
	public void complexXor2() {
		String[] rules = {
		//
		"ROOT = /\\d++/ ((eq5 ^ eq5) ^ lt100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("eq5", eq5);
		g.defineCondition("lt100", lt100);
		assertNotNull("xor acts like logical xor when binary", g.matches("5")
				.match());
	}

	@Test
	public void simpleXorNeg() {
		String[] rules = {
		//
		"ROOT = /\\d++/ (eq5 ^ !eq5)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("eq5", eq5);
		assertNotNull("redundant xor", g.matches("5").match());
	}

	@Test
	public void group() {
		String[] rules = {
		//
		"ROOT = /\\d++/ (eq5 | (gt10 & lt100))",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("eq5", eq5);
		g.defineCondition("gt10", gt10);
		g.defineCondition("lt100", lt100);
		int count = 0;
		Matcher m = g.find("5 11");
		while (m.match() != null)
			count++;
		assertTrue("parsed group", count == 2);
	}

}
