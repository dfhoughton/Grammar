package dfh.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * For testing {@link Match#labels()}.
 * <p>
 * <b>Creation date:</b> Dec 16, 2011
 * 
 * @author David Houghton
 * 
 */
public class LabelTest {
	private static Match n;

	@BeforeClass
	public static void prepare() {
		String[] rules = {
				//
				"ROOT = <d> [{foo} <e>]++ <f>",//
				"d = 'a'",//
				"e = 'b'",//
				"f = 'c'",//
		};
		Grammar g = new Grammar(rules);
		String s = "abbbc";
		n = g.matches(s).match();
	}

	@Test
	public void test() {
		assertNotNull("found match", n);
	}

	@Test
	public void testA() {
		Match a = n.first("\"a\"");
		assertTrue("\"a\" has two labels", a.labels().size() == 2);
	}

	@Test
	public void testB() {
		Match a = n.first("\"b\"");
		assertTrue("\"b\" has three labels", a.labels().size() == 3);
	}

	@Test
	public void testC() {
		Match a = n.first("\"c\"");
		assertTrue("\"c\" has two labels", a.labels().size() == 2);
	}

	@Test
	public void testD() {
		Match a = n.first("d");
		assertTrue("d has two labels", a.labels().size() == 2);
	}

	@Test
	public void testE() {
		Match a = n.first("e");
		assertEquals("e has three labels", 3, a.labels().size());
	}

	@Test
	public void testF() {
		Match a = n.first("f");
		assertTrue("f has two labels", a.labels().size() == 2);
	}

	@Test
	public void testFoo() {
		Match a = n.first("foo");
		assertTrue("foo has three labels", a.labels().size() == 3);
	}

	@Test
	public void redundantRules() {
		String[] rules = {
				//
				"ROOT = <a>",//
				"a = <b>",//
				"b = 'c'",//
		};
		Grammar g = new Grammar(rules);
		Match m = g.matches("c").match();
		assertNotNull(m);
		assertNotNull(m.first("a"));
		assertNotNull(m.first("b"));
	}
}
