package dfh.grammar;

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * These tests make sure the error catching mechanisms in {@link Compiler} are
 * doing what they should.
 * <p>
 * <b>Creation date:</b> Mar 23, 2011
 * 
 * @author David Houghton
 * 
 */
public class MisCompilationTest {
	@Test
	public void redefinitionError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"<b> = (quux) (s) (baz)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover redefinition");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("found redefinition", e.getMessage()
					.indexOf("rule <b> redefined") > -1);
		}
	}

	@Test
	public void incompleteError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert
					.assertTrue("found missing definition", e.getMessage()
							.indexOf("could not satisfy all dependencies") > -1);
		}
	}

	@Test
	public void noRootError() {
		String[] rules = {
				//
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover the lack of a root rule");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("no root rule found", e.getMessage()
					.indexOf("no root rule found") > -1);
		}
	}

	@Test
	public void doubleAssignmentError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineTerminal("s", Pattern.compile("\\s++"));
			g.defineTerminal("s", Pattern.compile("\\s++"));
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("found redefinition", e.getMessage()
					.indexOf("already defined") > -1);
		}
	}

	@Test
	public void falseAssignmentError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineTerminal("x", Pattern.compile("\\s++"));
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("unknown terminal rule", e.getMessage()
					.indexOf("unknown terminal rule") > -1);
		}
	}

	@Test
	public void incompleteError2() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.find("foo bar");
			g.defineTerminal("x", Pattern.compile("\\s++"));
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert
					.assertTrue(
							"terminal rules remaining undefined",
							e.getMessage().indexOf(
									"terminal rules remaining undefined") > -1);
		}
	}

}
