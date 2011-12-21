package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
		Match a = n.choose("\"a\"");
		assertTrue("\"a\" has two labels", a.labels().size() == 2);
		Match b = n.choose("f");
	}

	@Test
	public void testB() {
		Match a = n.choose("\"b\"");
		assertTrue("\"b\" has three labels", a.labels().size() == 3);
	}

	@Test
	public void testC() {
		Match a = n.choose("\"c\"");
		assertTrue("\"c\" has two labels", a.labels().size() == 2);
	}

	@Test
	public void testD() {
		Match a = n.choose("d");
		assertTrue("d has two labels", a.labels().size() == 2);
	}

	@Test
	public void testE() {
		Match a = n.choose("e");
		assertTrue("e has three labels", a.labels().size() == 3);
	}

	@Test
	public void testF() {
		Match a = n.choose("f");
		assertTrue("f has two labels", a.labels().size() == 2);
	}

	@Test
	public void testFoo() {
		Match a = n.choose("foo");
		assertTrue("foo has three labels", a.labels().size() == 3);
	}
}
