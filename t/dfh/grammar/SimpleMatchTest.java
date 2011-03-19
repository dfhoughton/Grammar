package dfh.grammar;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleMatchTest {

	private static Grammar g;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		g = new Grammar(rules);
	}

	@Test
	public void aTest() {
		String s = "foo bar";
		Node n = g.matches(s);
		System.out.println(n);
		assertNotNull("found 'foo bar'", n);
	}

	@Test
	public void bTest() {
		String s = "quux baz";
		Node n = g.matches(s);
		System.out.println(n);
		assertNotNull("found 'quux baz'", n);
	}

	@Test
	public void abTest() {
		String s = "quux baz  foo bar";
		int count = 0;
		for (int offset = 0; offset < s.length();) {
			Node n = g.find(s, offset);
			if (n == null)
				break;
			offset = n.end();
			System.out.println(n);
			count++;
		}
		assertTrue("found both matches in " + s, count == 2);
	}

	@Test
	public void failureTest() {
		String s = "quwerewr";
		Node n = g.matches(s);
		System.out.println(n);
		assertNull("recognized non-match for " + s, n);
	}
}
