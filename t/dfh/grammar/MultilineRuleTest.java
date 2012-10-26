package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests new convention for multi-line rules.
 * <p>
 * 
 * @author David F. Houghton - Jun 18, 2012
 * 
 */
public class MultilineRuleTest {

	@Test
	public void test1() {
		Grammar g = new Grammar("rule = 'a' \\\n 'b'");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void test2() {
		Grammar g = new Grammar("rule = \\\n 'a' 'b'");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void test3() {
		Grammar g = new Grammar("rule = \\\n 'a' \\\n'b'");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void test4() {
		Grammar g = new Grammar("rule = 'a' 'b' \\\n");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void test5() {
		Grammar g = new Grammar("rule = 'a' \\\n'b' \\\n");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void test6() {
		Grammar g = new Grammar("rule = \\\n'a' 'b' \\\n");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void test7() {
		Grammar g = new Grammar("rule = \\\n'a' \\\n'b' \\\n");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void commentAfterBackslash() {
		Grammar g = new Grammar("rule = 'a' \\ # this should be ignored \n 'b'");
		Match m = g.matches("ab").match();
		assertNotNull(m);
	}

	@Test
	public void dataAfterBackslash() {
		try {
			new Grammar("rule = 'a' \\ 'this should not be ignored' \n 'b'");
			fail("should have thrown exception");
		} catch (GrammarException e) {
			assertTrue(e.getCause().getMessage()
					.startsWith("significant characters after \\"));
		}
	}
}
