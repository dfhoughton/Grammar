package dfh.grammar;

import org.junit.Test;

public class CompilationTest {
	@Test
	public void compile() {
		String[] rules = {
				//
				"<ROOT> = <a> (s) <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(s) = \\s++",//
				"(foo) = foo",//
				"(bar) = bar",//
				"(quux) = quux",//
				"(baz) = baz",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.assertTrue("compiled simple rules", true);
		} catch (Exception e) {
			org.junit.Assert.fail("error thrown: " + e);
		}
	}
}
