package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CompilationTest {
	@Test
	public void compileBasic() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <foo> <s> <bar>",//
				"<b> = <quux> <s> <baz>",//
				"<s> = /\\s++/",//
				"<foo> = 'foo'",//
				"<bar> = 'bar'",//
				"<quux> = 'quux'",//
				"<baz> = 'baz'",//
		};
		try {
			new Grammar(rules);
			org.junit.Assert.assertTrue("compiled simple rules", true);
		} catch (Exception e) {
			fail("error thrown: " + e);
		}
	}

	@Test
	public void compileConditions() {
		String[] rules = {
				//
				"<ROOT> = <a> (less_than_20)", //
				"<a> = /\\b\\d{2}\\b/ (less_than_30)",//
		};
		try {
			new Grammar(rules);
			org.junit.Assert.assertTrue("compiled rules with condition", true);
		} catch (Exception e) {
			fail("error thrown: " + e);
		}
	}

	@Test
	public void compileConditionsAndComments() {
		String[] rules = {
				//
				"<ROOT> = <a> (less_than_20) # la di da", //
				"<a> = /\\b\\d{2}\\b/ (less_than_30) # whoa",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.assertTrue("compiled rules with condition", true);
		} catch (Exception e) {
			fail("error thrown: " + e);
		}
	}

	@Test
	public void singleLineCompilation() {
		Grammar g = new Grammar("ROOT = <foo>\nfoo= 'a'");
		assertNotNull("correct grammar", g.matches("a").match());
	}
}
