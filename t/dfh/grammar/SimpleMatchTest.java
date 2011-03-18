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
		assertNotNull("found 'foo bar'", n);
	}

}
