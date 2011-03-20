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
		Matcher m = g.matches(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("found 'foo bar'", n);
	}

	@Test
	public void bTest() {
		String s = "quux baz";
		Matcher m = g.matches(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("found 'quux baz'", n);
	}

	@Test
	public void abTest() {
		String s = "quux baz  foo bar";
		int count = 0;
		Matcher m = g.find(s);
		Match n;
		while (count < 3 && (n = m.match()) != null) {
			System.out.println(n);
			count++;
		}
		assertTrue("found both matches in " + s, count == 2);
	}

	@Test
	public void failureTest() {
		String s = "quwerewr";
		Matcher m = g.matches(s);
		Match n = m.match();
		System.out.println(n);
		assertNull("recognized non-match for " + s, n);
	}
}
